package com.centerm.lib.locate.bean;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.centerm.lib.locate.constant.CTLocateConstant.TYPE_WIFI_LOCATE;

/**
 * @desc wifi定位信息
 * @author tianyouyu
 * @date 2019/4/29 14:36
 */
@DatabaseTable(tableName = "tb_wifi_locate")
public class CTWifiInfo extends CTLocateInfo {

    @DatabaseField
    private String ssid; //连接标识

    @DatabaseField
    private String ip; //ip地址

    @DatabaseField
    private String mac; //mac地址

    public CTWifiInfo() {
        this.locateType = TYPE_WIFI_LOCATE;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String formatInfo() {
        if (errorCode != 0 || TextUtils.isEmpty(mac)) {
            return super.formatInfo();
        } else {
            return mac;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (errorCode == 0) {
            builder.append("wifi定位成功：");
            builder.append("ip=" + ip);
            builder.append(", ssid=" + ssid);
            builder.append(", mac=" + mac);
        } else {
            builder.append("wifi定位失败：");
            builder.append(errorMsg);
        }
        return builder.toString();
    }
}
