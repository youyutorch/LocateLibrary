package com.centerm.lib.locate.constant;

/**
 * @desc 定位响应枚举类
 * @author tianyouyu
 * @date 2019/4/30 15:22
 */
public enum CTLocateResp {
    SUCCESS(0, "定位成功"),
    TIMEOUT(1, "定位超时"),
    LOCATING(2, "正在定位中"),
    GPS_NO_LOCATE(3, "未打开定位"),
    AMAP_ERROR(4, "高德定位错误"),
    NETWORK_ERROR(5, "网络异常"),
    NO_BASE_STATION(6, "未获取到基站信息"),
    WIFI_NOT_CONNECT(7, "Wifi未连接"),
    PARAM_ERROR(8, "参数错误"),
    ERROR_OTHER(100, "其他错误");

    private int code;
    private String msg;

    CTLocateResp(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
