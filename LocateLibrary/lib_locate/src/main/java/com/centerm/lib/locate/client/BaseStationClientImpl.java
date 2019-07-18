package com.centerm.lib.locate.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.centerm.lib.locate.CTLocateManager;
import com.centerm.lib.locate.CTLocateOption;
import com.centerm.lib.locate.bean.CTBaseStationInfo;
import com.centerm.lib.locate.bean.CTCellInfo;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.constant.CTLocateConstant;
import com.centerm.lib.locate.constant.CTLocateResp;
import com.centerm.lib.locate.database.CommonDao;
import com.centerm.lib.locate.database.CTDbHelper;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.util.CLogger;
import com.centerm.lib.locate.util.CellLocateUtil;
import com.centerm.lib.locate.util.FormatUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @desc gps定位实现类
 * @author tianyouyu
 * @date 2019/4/30 15:14
 */
public class BaseStationClientImpl implements LocateClientInf {
    private static final int TIMEER_TASK_EXCUTE = 1;
    private static final int LOCATE_CALLBACK = 2;
    private CLogger mCLogger = CLogger.getCLogger(BaseStationClientImpl.class);

    private CTLocateOption locateOption;
    private CTGetLocationListener getLocationListener;
    private CTBaseStationInfo baseStationInfo;
    private CommonDao<CTCellInfo> cellInfoCommonDao;
    private Context context;
    private Timer mTimer;

    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(TIMEER_TASK_EXCUTE);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMEER_TASK_EXCUTE:
                    locateTimeout();
                    break;
                case LOCATE_CALLBACK:
                    locateCallback(baseStationInfo);
                    break;
                default:
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    public BaseStationClientImpl(Context context) {
        this.context = context;
        cellInfoCommonDao = new CommonDao<>(CTCellInfo.class, CTDbHelper.getInstance());
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
        CTLocateManager.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //实时获取基站信息
                List<CTCellInfo> cellInfos = CellLocateUtil.getCellInfoList(context, locateOption.getMinSignalStrength(), locateOption.isFilterInvalidSignal());
                if (cellInfos == null || cellInfos.isEmpty()) {
                    if (locateOption.isUseLastUpdate()) {
                        getLastLocation();
                        if (baseStationInfo != null) {
                            mHandler.sendEmptyMessage(LOCATE_CALLBACK);
                            return;
                        }
                    }
                    locateResult(CTLocateResp.NO_BASE_STATION, null);
                    return;
                }
                locateResult(CTLocateResp.SUCCESS, cellInfos);
            }
        });
        //打开超时控制
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, locateOption.getRequestTimeout() * 1000);
    }

    @Override
    public CTLocateInfo getLastLocation() {
        //从缓存中获取
        if (baseStationInfo != null) {
            return baseStationInfo;
        }

        //通过数据库获取
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("imsi", CellLocateUtil.getImsi(context, CTLocateConstant.EMPTY_IMSI));
        List<CTCellInfo> ctCellInfos = cellInfoCommonDao.queryByMap(queryMap);
        if (ctCellInfos == null || ctCellInfos.isEmpty()) {
            return null;
        }
        baseStationInfo = new CTBaseStationInfo();
        baseStationInfo.setCellInfos(ctCellInfos);
        return baseStationInfo;
    }

    @Override
    public boolean saveLocation(CTLocateInfo locateInfo) {
        if (locateInfo == null || !(locateInfo instanceof  CTBaseStationInfo)) {
            return false;
        }
        List<CTCellInfo> cellInfos = ((CTBaseStationInfo) locateInfo).getCellInfos();
        return updateCTCellInfos(cellInfos, locateOption.isFilterInvalidSignal());
    }

    @Override
    public void clearLocation() {
        mCLogger.debug("清除基站定位数据");
        cellInfoCommonDao.deleteBySQL("DELETE FROM tb_base_station_locate");
    }

    @Override
    public void stopLocation() {

    }

    private boolean updateCTCellInfos(List<CTCellInfo> cellInfoList, boolean filterInvalidSignal) {
        if (cellInfoList == null || cellInfoList.isEmpty()) {
            return false;
        }
        List<CTCellInfo> orignalCellInfos = cellInfoCommonDao.query();
        if (orignalCellInfos == null || orignalCellInfos.isEmpty()) {
            return cellInfoCommonDao.save(cellInfoList);
        }
        boolean result = true;
        List<CTCellInfo> addCellInfos = new ArrayList<>();
        for (CTCellInfo cellInfo : cellInfoList) {
            //是否已存在
            int cellId = CellLocateUtil.findCellId(cellInfo, orignalCellInfos, filterInvalidSignal);
            if (cellId > 0) {
                cellInfo.setId(cellId);
                result = cellInfoCommonDao.update(cellInfo);
            } else {
                addCellInfos.add(cellInfo);
            }

            if (!result) {
                mCLogger.warn("更新基站数据失败：" + cellInfo);
                return false;
            }
        }

        if (!addCellInfos.isEmpty()) {
            result = cellInfoCommonDao.save(addCellInfos);
            mCLogger.debug("保存基站数据到数据库" + result + ": 新增" + addCellInfos.size() + "条");
        }
        return result;
    }

    private void locateTimeout() {
        if (locateOption.isUseLastUpdate()) {
            getLastLocation();
            if (baseStationInfo != null) {
                locateCallback(baseStationInfo);
                return;
            }
        }
        wrapperLocation(CTLocateResp.TIMEOUT, null);
        locateCallback(baseStationInfo);
    }

    private void cancel() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            this.baseStationInfo = null;
        }
    }

    private void locateResult(CTLocateResp resp, List<CTCellInfo> cellInfoList) {
        wrapperLocation(resp, cellInfoList);
        if (resp == CTLocateResp.SUCCESS) {
            //保存到数据库
            boolean result = saveLocation(baseStationInfo);
            int len = (baseStationInfo == null || baseStationInfo.getCellInfos() == null) ? 0 : baseStationInfo.getCellInfos().size();
            mCLogger.debug("保存基站数据：" + result + ", 共" + len + "条");

            if (locateOption.isUseLastUpdate() || locateOption.getMinCollectCount() > 0) {
                Map<String, String> queryMap = new HashMap<>();
                queryMap.put("imsi", CellLocateUtil.getImsi(context, CTLocateConstant.EMPTY_IMSI));
                List<CTCellInfo> ctCellInfos = cellInfoCommonDao.queryByMap(queryMap);
                baseStationInfo.setCellInfos(ctCellInfos);
            }
        }
        mHandler.sendEmptyMessage(LOCATE_CALLBACK);
    }

    private void locateCallback(CTBaseStationInfo baseStationInfo) {
        mCLogger.info("station locate result>> " + baseStationInfo);
        if (this.getLocationListener != null) {
            getLocationListener.onFind(getBaseCountInfo(baseStationInfo));
        }
        cancel();
    }

    /**
     * 返回要获取的最大个数
     * @param baseStationInfo
     * @return
     */
    private CTBaseStationInfo getBaseCountInfo(CTBaseStationInfo baseStationInfo) {
        if (baseStationInfo == null || baseStationInfo.getCellInfos() == null || baseStationInfo.getCellInfos().isEmpty()) {
            return baseStationInfo;
        }
        List<CTCellInfo> orignalCellInfos = baseStationInfo.getCellInfos();
        //判断是否需要过滤
        if (locateOption.getBaseMaxCount() == 0 || orignalCellInfos.size() <= locateOption.getBaseMaxCount()) {
            return baseStationInfo;
        }
        //降序排列
        Collections.sort(orignalCellInfos, Collections.<CTCellInfo>reverseOrder());
        mCLogger.error("排序后>>" + orignalCellInfos);
        List<CTCellInfo> cellInfos = new ArrayList<>();
        for (int i = 0; i < orignalCellInfos.size(); i++) {
            CTCellInfo cellInfo = orignalCellInfos.get(i);
            //过滤无用基站
            if (cellInfo.getSid() == Integer.MAX_VALUE || cellInfo.getNid() == Integer.MAX_VALUE || cellInfo.getSid() == Integer.MAX_VALUE) {
                continue;
            }
            //过滤弱信号基站
            if (cellInfo.getLevel() <= 2) {
                continue;
            }
            cellInfos.add(cellInfo);

            if (cellInfos.size() >= locateOption.getBaseMaxCount()) {
                break;
            }
        }
        baseStationInfo.setCellInfos(cellInfos);
        return baseStationInfo;
    }

    private CTBaseStationInfo wrapperLocation(CTLocateResp resp, List<CTCellInfo> cellInfoList) {
        boolean isImmediately = locateOption == null ? false : !locateOption.isUseLastUpdate();
        baseStationInfo = new CTBaseStationInfo();
        baseStationInfo.setImmediately(isImmediately);
        baseStationInfo.setErrorCode(resp.getCode());
        baseStationInfo.setErrorMsg(resp.getMsg());
        baseStationInfo.setLastUpdate(FormatUtil.getFormatDate());
        if (locateOption != null) {
            baseStationInfo.setMinSignalStrength(locateOption.getMinSignalStrength());
            baseStationInfo.setBaseMaxCount(locateOption.getBaseMaxCount());
        }
        if (resp == CTLocateResp.SUCCESS) {
            baseStationInfo.setCellInfos(cellInfoList);
        }
        return baseStationInfo;
    }

}
