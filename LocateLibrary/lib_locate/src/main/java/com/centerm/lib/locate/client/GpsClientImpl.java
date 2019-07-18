package com.centerm.lib.locate.client;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import com.centerm.lib.locate.database.CTDbHelper;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.bean.CTGpsInfo;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.CTLocateOption;
import com.centerm.lib.locate.constant.CTLocateResp;
import com.centerm.lib.locate.database.CommonDao;
import com.centerm.lib.locate.util.CLogger;
import com.centerm.lib.locate.util.FormatUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @desc gps定位实现类
 * @author tianyouyu
 * @date 2019/4/30 15:14
 */
public class GpsClientImpl implements LocateClientInf {
    private CLogger mCLogger = CLogger.getCLogger(GpsClientImpl.class);
    private static volatile boolean isLocating;
    private static GpsClientImpl mInstance;

    private CTLocateOption locateOption;
    private CTGetLocationListener getLocationListener;
    private CTGpsInfo gpsInfo;
    private CommonDao<CTGpsInfo> gpsInfoCommonDao;
    private Context context;
    private LocationManager mLocationManager;
    private CountDownTimer mTimer;

    private GpsClientImpl(Context context) {
        this.context = context;
        gpsInfoCommonDao = new CommonDao<>(CTGpsInfo.class, CTDbHelper.getInstance());
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public synchronized static GpsClientImpl getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GpsClientImpl(context);
        }
        return mInstance;
    }

    /**
     * 判断当前gps是否正在定位
     * @return
     */
    public static boolean isLocating() {
        return isLocating;
    }

    @Override
    public void getLocation(final CTLocateOption locateOption, CTGetLocationListener getLocationListener) {
        if (locateOption == null) {
            mCLogger.warn("getLocation >> option不能为空");
            return;
        }
        isLocating = true;
        cancel();
        mLocationManager.removeUpdates(mLocationListener);
        this.gpsInfo = null;
        this.locateOption = locateOption;
        this.getLocationListener = getLocationListener;
        //实时获取经纬度
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);
        //打开超时控制
        long timeout = locateOption.getRequestTimeout() == 0 ? 60 : locateOption.getRequestTimeout();
        mTimer = new CountDownTimer(timeout * 1000, timeout * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCLogger.debug("onTick");
            }

            @Override
            public void onFinish() {
                mCLogger.debug("onFinish");
                mLocationManager.removeUpdates(mLocationListener);
                if (locateOption.isUseLastUpdate()) {
                    getLastLocation();
                    if (gpsInfo != null) {
                        locateResult(gpsInfo);
                        return;
                    }
                }
                locateResult(CTLocateResp.TIMEOUT, null);
            }
        };
        mTimer.start();
    }

    @Override
    public CTLocateInfo getLastLocation() {
        //从缓存中获取
        if (gpsInfo != null) {
            return gpsInfo;
        }
        //通过接口获取上次位置
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            wrapperLocation(CTLocateResp.SUCCESS, location);
            return gpsInfo;
        }
        //通过数据库获取
        List<CTGpsInfo> gpsInfos = gpsInfoCommonDao.query();
        if (gpsInfos == null || gpsInfos.isEmpty()) {
            return null;
        }
        gpsInfo = gpsInfos.get(0);
        return gpsInfo;
    }

    @Override
    public boolean saveLocation(CTLocateInfo locateInfo) {
        if (locateInfo == null || !(locateInfo instanceof  CTGpsInfo)) {
            return false;
        }
        gpsInfoCommonDao.deleteBySQL("DELETE FROM tb_gps_locate");
        boolean result = gpsInfoCommonDao.save((CTGpsInfo) locateInfo);
        if (result) {
            gpsInfo = (CTGpsInfo) locateInfo;
        }
        return result;
    }

    @Override
    public void clearLocation() {
        mCLogger.debug("清除GPS定位数据");
        gpsInfoCommonDao.deleteBySQL("DELETE FROM tb_gps_locate");
    }

    @Override
    public void stopLocation() {
        mLocationManager.removeUpdates(mLocationListener);
        cancel();
        isLocating = false;
    }

    private void cancel() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void locateResult(CTLocateResp resp, Location location) {
        if (!isLocating) {
            mCLogger.debug("not locating, cancel callback...");
            return;
        }
        CTGpsInfo gpsInfo = wrapperLocation(resp, location);
        if (resp == CTLocateResp.SUCCESS) {
            //保存到数据库
            boolean result = saveLocation(gpsInfo);
            mCLogger.debug("保存数据" + result + " >> " + gpsInfo);
        }
        locateResult(gpsInfo);
        isLocating = false;
    }

    private void locateResult(CTGpsInfo gpsInfo) {
        mCLogger.info("gps locate result>> " + gpsInfo);
        if (this.getLocationListener == null) {
            return;
        }
        getLocationListener.onFind(gpsInfo);
    }

    private CTGpsInfo wrapperLocation(CTLocateResp resp, Location location) {
        boolean isImmediately = locateOption == null ? false : !locateOption.isUseLastUpdate();
        gpsInfo = new CTGpsInfo();
        gpsInfo.setImmediately(isImmediately);
        gpsInfo.setErrorCode(resp.getCode());
        gpsInfo.setErrorMsg(resp.getMsg());
        gpsInfo.setLastUpdate(FormatUtil.getFormatDate());
        if (resp == CTLocateResp.SUCCESS) {
            gpsInfo.setLatitude(location.getLatitude());
            gpsInfo.setLongitude(location.getLongitude());
        }
        return gpsInfo;
    }



    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                return;
            }
            mCLogger.debug("onLocationChanged-->longitude=" + location.getLongitude() + ", latitude=" + location.getLatitude());
            locateResult(CTLocateResp.SUCCESS, location);
            mLocationManager.removeUpdates(mLocationListener);
            cancel();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
