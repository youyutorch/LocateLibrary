package com.centerm.lib.locate.client;

import android.content.Context;

import com.centerm.lib.locate.constant.CTLocateConstant;

/**
 * @desc 定位实现类 实例化
 * @author tianyouyu
 * @date 2019/4/30 16:59
 */
public class LocateClientFactory {
    private GpsClientImpl gpsClient;

    /**
     * 根据定位类型，实例化相对应的实现类
     * @param type
     * @return
     */
    public static LocateClientInf getLocateClient(int type, Context context) {
        LocateClientInf instance = null;
        switch (type) {
            case CTLocateConstant.TYPE_NETWORK_LOCATE:
                instance = new NetWorkClientImpl(context);
                break;
            case CTLocateConstant.TYPE_WIFI_LOCATE:
                instance = new WifiClientImpl(context);
                break;
            case CTLocateConstant.TYPE_GPS_LOCATE:
                instance = GpsClientImpl.getInstance(context);
                break;
            case CTLocateConstant.TYPE_BASE_STATATION_LOCATE:
                instance = new BaseStationClientImpl(context);
                break;
            default:
                break;
        }

        return instance;
    }
}
