package com.centerm.lib.locate.client;

import android.content.Context;

import com.centerm.lib.locate.CTLocateManager;
import com.centerm.lib.locate.CTLocateOption;
import com.centerm.lib.locate.bean.CTGpsInfo;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.constant.CTLocateConstant;
import com.centerm.lib.locate.constant.CTLocateResp;
import com.centerm.lib.locate.inf.CTCollectLocationListener;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.inf.CTGetMulLocationListener;

import com.centerm.lib.locate.util.CLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc 定位管理类，用于多个定位同时使用的情况
 * @author tianyouyu
 * @date 2019/5/8 11:53
 */
public class LocateClientManager {
    private CLogger logger = CLogger.getCLogger(this.getClass());
    private CTLocateOption locateOption;
    private Context context;
    private CTLocateManager ctLocateManager;
    private CTGetMulLocationListener mulLocationListener;
    private List<CTLocateInfo> mLocateInfoList;
    private Map<Integer, LocateClientInf> mLocationClientMap;
    private boolean isLocating; //是否正在定位
    private int finishCount; //已完成定位个数
    private int totalCount; //总定位个数

    public LocateClientManager(Context context, CTLocateManager manager) {
        this.context = context;
        this.mLocateInfoList = new ArrayList<>();
        this.mLocationClientMap = new HashMap<>();
        this.ctLocateManager = manager;
    }

    /**
     * 一次性获取多种类型定位结果，目前只支持ALL TYPE
     * @param listener
     */
    public void getMulLocation(CTLocateOption option, CTGetMulLocationListener listener) {
        this.locateOption = option;
        this.mulLocationListener = listener;
        this.finishCount = 0;
        this.totalCount = 3;
        mLocationClientMap.clear();
        mLocateInfoList.clear();
        ctLocateManager.setCollectLocationListener(null);
        if (locateOption == null || locateOption.getLocateType() != CTLocateConstant.TYPE_ALL_LOCATE) {
            addLocateInfo(CTLocateResp.PARAM_ERROR, null);
            locateResult();
            return;
        }
        isLocating = true;
        getLocation(CTLocateConstant.TYPE_WIFI_LOCATE);
        if (locateOption.isUseGpsFirst()) {
            getGpsLocation();
        } else {
            getLocation(CTLocateConstant.TYPE_NETWORK_LOCATE);
        }
        getBaseStationLocation(option);

    }

    public void stopMulLocation() {
        if (!isLocating) {
            return;
        }
        if (locateOption.isInitMode()) {
            ctLocateManager.stopCollectLocation();
        }
        stopLocation(CTLocateConstant.TYPE_NETWORK_LOCATE);
        stopLocation(CTLocateConstant.TYPE_GPS_LOCATE);
        isLocating = false;
        mLocateInfoList.clear();
    }

    private void stopLocation(int type) {
        if (mLocationClientMap.isEmpty()) {
            return;
        }
        LocateClientInf locateClientInf = mLocationClientMap.get(type);
        if (locateClientInf != null) {
            locateClientInf.stopLocation();
        }
    }

    public void clearAllLocation() {
        ctLocateManager.clearLocation(CTLocateConstant.TYPE_WIFI_LOCATE);
        ctLocateManager.clearLocation(CTLocateConstant.TYPE_GPS_LOCATE);
        ctLocateManager.clearLocation(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
        ctLocateManager.clearLocation(CTLocateConstant.TYPE_NETWORK_LOCATE);
    }

    private void getGpsLocation() {
        if (locateOption.isInitMode() || !locateOption.isUseLastUpdate()) {
            getLocation(CTLocateConstant.TYPE_GPS_LOCATE);
        } else {
            //不是初始化上送，直接从缓存中获取
            LocateClientInf locateClientInf = LocateClientFactory.getLocateClient(CTLocateConstant.TYPE_GPS_LOCATE, context);
            CTLocateInfo locateInfo = locateClientInf.getLastLocation();
            logger.debug("gps getLastLocation>> " + locateInfo);
            finishCount++;
            if (locateInfo == null) {
                addLocateInfo(CTLocateResp.ERROR_OTHER, null);
            } else {
                addLocateInfo(CTLocateResp.SUCCESS, locateInfo);
            }
        }
    }

    private void getBaseStationLocation(CTLocateOption option) {
        //根据是否是初始定位模式，做相应的处理
        if (!option.isInitMode()) {
            getLocation(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
        } else {
            //先收集，后获取
            CTLocateOption collectOption = new CTLocateOption(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
            collectOption.setClearOldInfo(option.isClearOldInfo());
            collectOption.setFilterInvalidSignal(false);
            collectOption.setInterval(5);
            collectOption.setTotalTime(option.getRequestTimeout());
            ctLocateManager.startCollectLocation(collectOption);
            ctLocateManager.setCollectLocationListener(mCollectLocationListener);
        }
    }

    private void getLocation(int type) {
        LocateClientInf locateClientInf = LocateClientFactory.getLocateClient(type, context);
        if (locateClientInf == null) {
            return;
        }
        mLocationClientMap.put(type, locateClientInf);
        CTLocateOption option = new CTLocateOption(type);
        option.setGetOnce(true);
        if (type == CTLocateConstant.TYPE_BASE_STATATION_LOCATE && locateOption.isInitMode()) {
            option.setUseLastUpdate(true);
        } else {
            option.setUseLastUpdate(locateOption.isUseLastUpdate());
        }
        option.setRequestTimeout(locateOption.getRequestTimeout());
        option.setNeedAddress(locateOption.isNeedAddress());
        option.setCacheEnable(locateOption.isCacheEnable());
        option.setMinSignalStrength(locateOption.getMinSignalStrength());
        option.setBaseMaxCount(locateOption.getBaseMaxCount());
        option.setFilterInvalidSignal(locateOption.isFilterInvalidSignal());
        //开始定位
        locateClientInf.getLocation(option, mLocationListener);
    }



    private void addLocateInfo(CTLocateResp resp, CTLocateInfo locateInfo) {
        if (resp != CTLocateResp.SUCCESS) {
            locateInfo = new CTLocateInfo();
            locateInfo.setLocateType(CTLocateConstant.TYPE_ALL_LOCATE);
            locateInfo.setErrorCode(resp.getCode());
            locateInfo.setErrorMsg(resp.getMsg());
        }

        mLocateInfoList.add(locateInfo);
    }

    private void locateResult() {
        if (mulLocationListener == null) {
            return;
        }
        mulLocationListener.onFind(mLocateInfoList);
        finishCount = 0;
        totalCount = 0;
        isLocating = false;
    }

    private synchronized void findLocateInfo(CTLocateInfo ctLocateInfo) {
        if (!isLocating) {
            logger.debug("not locating, cancel callback...");
            return;
        }
        finishCount++;
        if (ctLocateInfo == null) {
            addLocateInfo(CTLocateResp.ERROR_OTHER, null);
        } else {
            addLocateInfo(CTLocateResp.SUCCESS, ctLocateInfo);
        }

        //初始定位模式且不是强制收集，需在此获取基站信息
        if (locateOption.isInitMode() && !locateOption.isForceCollect()) {
            if (ctLocateInfo.getLocateType() == CTLocateConstant.TYPE_NETWORK_LOCATE ||
                    ctLocateInfo.getLocateType() == CTLocateConstant.TYPE_GPS_LOCATE) {
                //结束收集
                ctLocateManager.stopCollectLocation();
                getLocation(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
            }
        }

        if (finishCount >= totalCount) {
            locateResult();
        }
    }

    private CTGetLocationListener mLocationListener = new CTGetLocationListener() {
        @Override
        public void onFind(CTLocateInfo ctLocateInfo) {
            findLocateInfo(ctLocateInfo);
        }
    };

    private CTCollectLocationListener mCollectLocationListener = new CTCollectLocationListener() {
        @Override
        public void onTick(int currCount, CTLocateInfo ctLocateInfo) {

        }

        @Override
        public void onFinish(int totalCount, CTLocateInfo ctLocateInfo) {
            logger.debug("collect onFinish>>");
            if (locateOption.isInitMode() && locateOption.isForceCollect()) {
                findLocateInfo(ctLocateInfo);
            }
        }
    };
}
