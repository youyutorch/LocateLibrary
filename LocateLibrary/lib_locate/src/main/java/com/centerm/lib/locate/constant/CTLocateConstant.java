package com.centerm.lib.locate.constant;

/**
 * @author tianyouyu
 * @desc
 * @date 2019/4/30 17:04
 */
public class CTLocateConstant {
    /**
     * 通过高德、百度等三方通道提供的网络定位，可获取经纬度等信息
     */
    public static final int TYPE_NETWORK_LOCATE = 0;

    /**
     * 通过wifi定位，获取wifi相关信息，包括mac、ip等
     */
    public static final int TYPE_WIFI_LOCATE = 1;

    /**
     * 通过GPS定位，获取经纬度信息
     */
    public static final int TYPE_GPS_LOCATE = 2;

    /**
     * 通过基站定位，获取mcc，mnc等相关信息
     */
    public static final int TYPE_BASE_STATATION_LOCATE = 3;

    /**
     * 获取上述所有定位信息
     */
    public static final int TYPE_ALL_LOCATE = 4;

    /**
     * 空的imsi信息
     */
    public static final String EMPTY_IMSI = "-1";

    public static final int FORMAT_VERSION_EPOS = 0;

    public static final int FORMAT_VERSION_EPAY = 1;

}
