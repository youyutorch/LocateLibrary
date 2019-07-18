package com.centerm.lib.locate.bean;

import com.centerm.lib.locate.util.FormatUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.centerm.lib.locate.constant.CTLocateConstant.TYPE_GPS_LOCATE;

/**
 * @desc GPS定位信息
 * @author tianyouyu
 * @date 2019/4/29 14:36
 */
@DatabaseTable(tableName = "tb_gps_locate")
public class CTGpsInfo extends CTLocateInfo {

    @DatabaseField
    private double longitude; //经度

    @DatabaseField
    private double latitude; //纬度

    public CTGpsInfo() {
        this.locateType = TYPE_GPS_LOCATE;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public String formatInfo() {
        if (errorCode != 0) {
            return super.formatInfo();
        } else {
            String lon = FormatUtil.getFormatLongitude(longitude);
            String lan = FormatUtil.getFormatLatitude(latitude);
            return (lon + "," + lan);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (errorCode == 0) {
            builder.append("GPS定位成功：");
            builder.append("long=" + longitude);
            builder.append(", lat=" + latitude);
        } else {
            builder.append("GPS定位失败：");
            builder.append(errorMsg);
        }
        return builder.toString();
    }
}
