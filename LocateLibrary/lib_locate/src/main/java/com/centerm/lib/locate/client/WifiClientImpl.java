package com.centerm.lib.locate.client;

import android.content.Context;

import com.centerm.lib.locate.CTLocateOption;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.bean.CTWifiInfo;
import com.centerm.lib.locate.constant.CTLocateResp;
import com.centerm.lib.locate.database.CommonDao;
import com.centerm.lib.locate.database.CTDbHelper;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.util.FormatUtil;
import com.centerm.lib.locate.util.WifiUtil;

import com.centerm.lib.locate.util.CLogger;

import java.util.List;

/**
 * @desc gps定位实现类
 * @author tianyouyu
 * @date 2019/4/30 15:14
 */
public class WifiClientImpl implements LocateClientInf {
    private CLogger mCLogger = CLogger.getCLogger(WifiClientImpl.class);

    private CTLocateOption locateOption;
    private CTGetLocationListener getLocationListener;
    private CTWifiInfo wifiInfo;
    private CommonDao<CTWifiInfo> wifiCommonDao;
    private Context context;

    public WifiClientImpl(Context context) {
        this.context = context;
        wifiCommonDao = new CommonDao<>(CTWifiInfo.class, CTDbHelper.getInstance());
    }

    @Override
    public void getLocation(CTLocateOption locateOption, CTGetLocationListener getLocationListener) {
        if (locateOption == null) {
            mCLogger.warn("getLocation >> option不能为空");
            return;
        }
        this.locateOption = locateOption;
        this.getLocationListener = getLocationListener;
        //检测wifi是否连接
        if (!WifiUtil.isWifiEnabled(context)) {
            locateResult(CTLocateResp.WIFI_NOT_CONNECT, null, null, null);
            return;
        }
        String ip = WifiUtil.getWifiIp(context);
        String ssid = WifiUtil.getSSID(context);
        String mac = WifiUtil.getWifiMac(context);
        locateResult(CTLocateResp.SUCCESS, ip, ssid, mac);
        //保存到数据库
        saveLocation(wifiInfo);
    }

    @Override
    public CTLocateInfo getLastLocation() {
        //从缓存中获取
        if (wifiInfo != null) {
            return wifiInfo;
        }

        //通过数据库获取
        List<CTWifiInfo> wifiInfos = wifiCommonDao.query();
        if (wifiInfos == null || wifiInfos.isEmpty()) {
            return null;
        }
        wifiInfo = wifiInfos.get(0);
        return wifiInfo;
    }

    @Override
    public boolean saveLocation(CTLocateInfo locateInfo) {
        if (locateInfo == null || !(locateInfo instanceof  CTWifiInfo)) {
            return false;
        }
        wifiCommonDao.deleteBySQL("DELETE FROM tb_wifi_locate");
        boolean result = wifiCommonDao.save((CTWifiInfo) locateInfo);
        if (result) {
            wifiInfo = (CTWifiInfo) locateInfo;
        }
        return result;
    }

    @Override
    public void clearLocation() {
        mCLogger.debug("清除wifi定位数据");
        wifiCommonDao.deleteBySQL("DELETE FROM tb_wifi_locate");
    }

    @Override
    public void stopLocation() {

    }

    private void locateResult(CTLocateResp resp, String ip, String ssid, String mac) {
        wrapperLocation(resp, ip, ssid, mac);
        if (this.getLocationListener == null) {
            return;
        }
        mCLogger.info( "wifi locate result>> " + wifiInfo);
        getLocationListener.onFind(wifiInfo);
    }

    private CTWifiInfo wrapperLocation(CTLocateResp resp, String ip, String ssid, String mac) {
        boolean isImmediately = locateOption == null ? false : !locateOption.isUseLastUpdate();
        wifiInfo = new CTWifiInfo();
        wifiInfo.setImmediately(isImmediately);
        wifiInfo.setErrorCode(resp.getCode());
        wifiInfo.setErrorMsg(resp.getMsg());
        wifiInfo.setLastUpdate(FormatUtil.getFormatDate());
        if (resp == CTLocateResp.SUCCESS) {
            wifiInfo.setIp(ip);
            wifiInfo.setSsid(ssid);
            wifiInfo.setMac(mac);
        }
        return wifiInfo;
    }

}
