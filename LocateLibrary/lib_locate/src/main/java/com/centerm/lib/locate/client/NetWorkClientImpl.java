package com.centerm.lib.locate.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.centerm.lib.locate.CTLocateManager;
import com.centerm.lib.locate.CTLocateOption;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.bean.CTNetWorkInfo;
import com.centerm.lib.locate.constant.CTLocateResp;
import com.centerm.lib.locate.database.CTDbHelper;
import com.centerm.lib.locate.database.CommonDao;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.util.FormatUtil;
import com.centerm.lib.locate.util.ShellUtils;

import com.centerm.lib.locate.util.CLogger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @desc gps定位实现类
 * @author tianyouyu
 * @date 2019/4/30 15:14
 */
public class NetWorkClientImpl implements LocateClientInf {
    private static final int TIMEER_TASK_EXCUTE = 1;
    private static final int LOCATE_CALLBACK = 2;
    private CLogger mCLogger = CLogger.getCLogger(NetWorkClientImpl.class);

    /**
     * 高德定位参数
     */
    private AMapLocationClient aMapLocationClient = null;
    private AMapLocationClientOption aMapLocationClientOption = null;

    private CTLocateOption locateOption;
    private CTGetLocationListener getLocationListener;
    private CTNetWorkInfo netWorkInfo;
    private CommonDao<CTNetWorkInfo> networkCommonDao;
    private Context context;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler;
    private boolean isLocating;

    @SuppressLint("HandlerLeak")
    public NetWorkClientImpl(Context context) {
        this.context = context;
        networkCommonDao = new CommonDao<>(CTNetWorkInfo.class, CTDbHelper.getInstance());

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(TIMEER_TASK_EXCUTE);
            }
        };

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TIMEER_TASK_EXCUTE:
                        locateTimeOut();
                        break;
                    case LOCATE_CALLBACK:
                        locateCallback(netWorkInfo);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void getLocation(final CTLocateOption locateOption, CTGetLocationListener getLocationListener) {
        if (locateOption == null) {
            mCLogger.warn("getLocation >> option不能为空");
            return;
        }
        cancel();
        this.locateOption = locateOption;
        this.getLocationListener = getLocationListener;
        isLocating = true;
        //打开超时控制
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, locateOption.getRequestTimeout() * 1000);

        CTLocateManager.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //检查能否连接公网
                if (!isNetworkEnable()) {
                    if (locateOption.isUseLastUpdate()) {
                        getLastLocation();
                        if (netWorkInfo != null) {
                            mHandler.sendEmptyMessage(LOCATE_CALLBACK);
                            return;
                        }
                    }
                    locateResult(CTLocateResp.NETWORK_ERROR, null);
                    return;
                }
                //todo 暂使用高德定位，后续需兼容百度定位
                startAmapLocate(locateOption);
            }
        });


    }

    /**
     * 开始高德定位
     */
    private void startAmapLocate(CTLocateOption locateOption) {
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
        }
        aMapLocationClient = new AMapLocationClient(context);
        aMapLocationClientOption = getAmapOption(locateOption);
        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        aMapLocationClient.setLocationListener(mAMapLocationListener);

        aMapLocationClient.startLocation();
    }

    /**
     * 高德地图设置默认参数
     *
     * @return
     */
    private AMapLocationClientOption getAmapOption(CTLocateOption locateOption) {
        AMapLocationClientOption option = new AMapLocationClientOption();
        //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        AMapLocationClientOption.AMapLocationMode mode = AMapLocationClientOption.AMapLocationMode.Battery_Saving;
        if (locateOption.getLocateMode() == 2) {
            mode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy;
        }
        option.setLocationMode(mode);
        //可选，设置网络请求超时时间
        long timeOut = locateOption.getRequestTimeout();
        option.setHttpTimeOut(timeOut * 1000);
        //可选，设置是否返回逆地理地址信息。默认是true
        option.setNeedAddress(locateOption.isNeedAddress());
        //可选，设置是否使用缓存定位，默认为true
        option.setLocationCacheEnable(locateOption.isCacheEnable());
        //是否使用单次定位
        option.setOnceLocation(true);

        //可选，设置定位间隔
        option.setInterval(1000);
        //可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        option.setGpsFirst(false);

        //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        option.setOnceLocationLatest(false);
        //可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
        //可选，设置是否使用传感器。默认是false
        option.setSensorEnable(false);
        //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        option.setWifiScan(true);
        //可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        option.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);
        return option;
    }

    private boolean isNetworkEnable() {
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd("ping -c 1 www.baidu.com", false);
        mCLogger.debug("exec result: " + commandResult.result + " - " + commandResult.successMsg + " - " + commandResult.errorMsg);
        if (commandResult != null && commandResult.result == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public CTLocateInfo getLastLocation() {
        //从缓存中获取
        if (netWorkInfo != null) {
            return netWorkInfo;
        }
        //通过数据库获取
        List<CTNetWorkInfo> netWorkInfos = networkCommonDao.query();
        if (netWorkInfos == null || netWorkInfos.isEmpty()) {
            return null;
        }
        netWorkInfo = netWorkInfos.get(0);
        return netWorkInfo;
    }

    @Override
    public boolean saveLocation(CTLocateInfo locateInfo) {
        if (locateInfo == null || !(locateInfo instanceof CTNetWorkInfo)) {
            return false;
        }
        networkCommonDao.deleteBySQL("DELETE FROM tb_network_locate");
        boolean result = networkCommonDao.save((CTNetWorkInfo) locateInfo);
        if (result) {
            netWorkInfo = (CTNetWorkInfo) locateInfo;
        }
        return result;
    }

    @Override
    public void clearLocation() {
        mCLogger.debug("清除公网定位数据");
        networkCommonDao.deleteBySQL("DELETE FROM tb_network_locate");
    }

    @Override
    public void stopLocation() {
        cancel();
        isLocating = false;
    }

    private void locateTimeOut() {
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
            aMapLocationClient = null;
        }

        if (locateOption.isUseLastUpdate()) {
            getLastLocation();
            if (netWorkInfo != null) {
                locateCallback(netWorkInfo);
                return;
            }
        }
        wrapperLocation(CTLocateResp.TIMEOUT, null);
        locateCallback(netWorkInfo);
    }

    private void cancel() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            this.netWorkInfo = null;
        }
    }

    private void locateResult(CTLocateResp resp, AMapLocation aMapLocation) {
        if (!isLocating) {
            mCLogger.debug("not locating, cancel callback...");
            return;
        }
        wrapperLocation(resp, aMapLocation);
        if (resp == CTLocateResp.SUCCESS) {
            //保存到数据库
            boolean result = saveLocation(netWorkInfo);
            mCLogger.debug("保存网络数据" + result + " >> " + netWorkInfo);
        }
        mHandler.sendEmptyMessage(LOCATE_CALLBACK);
        isLocating = false;
    }

    private void locateCallback(CTNetWorkInfo netWorkInfo) {
        if (this.getLocationListener != null) {
            getLocationListener.onFind(netWorkInfo);
        }
        cancel();
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
            aMapLocationClient = null;
        }
    }

    private CTNetWorkInfo wrapperLocation(CTLocateResp resp, AMapLocation aMapLocation) {
        boolean isImmediately = locateOption == null ? false : !locateOption.isUseLastUpdate();
        netWorkInfo = new CTNetWorkInfo();
        netWorkInfo.setImmediately(isImmediately);
        netWorkInfo.setErrorCode(resp.getCode());
        netWorkInfo.setLastUpdate(FormatUtil.getFormatDate());
        if (aMapLocation != null) {
            netWorkInfo.setErrorMsg(aMapLocation.getErrorCode() + aMapLocation.getErrorInfo());
        } else {
            netWorkInfo.setErrorMsg(resp.getMsg());
        }

        if (resp == CTLocateResp.SUCCESS) {
            netWorkInfo.setLongitude(aMapLocation.getLongitude());
            netWorkInfo.setLatitude(aMapLocation.getLatitude());
            if (locateOption.isNeedAddress()) {
                netWorkInfo.setAccuracy(aMapLocation.getAccuracy());
                netWorkInfo.setCountry(aMapLocation.getCountry());
                netWorkInfo.setProvince(aMapLocation.getProvince());
                netWorkInfo.setCity(aMapLocation.getCity());
                netWorkInfo.setCityCode(aMapLocation.getCityCode());
                netWorkInfo.setAdCode(aMapLocation.getAdCode());
                netWorkInfo.setAddress(aMapLocation.getAddress());
                netWorkInfo.setPoiName(aMapLocation.getPoiName());
            }
        }
        return netWorkInfo;
    }


    private AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            mCLogger.debug("onLocationChanged-->longitude=" + aMapLocation.getLongitude() + ", latitude=" + aMapLocation.getLatitude());
            if (aMapLocation.getErrorCode() == 0) {
                locateResult(CTLocateResp.SUCCESS, aMapLocation);
            } else {
                if (locateOption.isUseLastUpdate()) {
                    getLastLocation();
                    if (netWorkInfo != null) {
                        mHandler.sendEmptyMessage(LOCATE_CALLBACK);
                        return;
                    }
                }
                locateResult(CTLocateResp.AMAP_ERROR, aMapLocation);
            }

        }
    };

}
