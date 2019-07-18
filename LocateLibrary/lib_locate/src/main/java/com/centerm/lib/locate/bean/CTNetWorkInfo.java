package com.centerm.lib.locate.bean;

import com.centerm.lib.locate.util.FormatUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.centerm.lib.locate.constant.CTLocateConstant.TYPE_NETWORK_LOCATE;

/**
 * @desc 网络定位信息
 * @author tianyouyu
 * @date 2019/4/29 14:44
 */
@DatabaseTable(tableName = "tb_network_locate")
public class CTNetWorkInfo extends CTLocateInfo {
    public static final int CHANNEL_AMAP = 0;
    public static final int CHANNEL_BAIDU = 1;
    public static final int CHANNEL_OTHER = 2;

    @DatabaseField
    private double longitude;

    @DatabaseField
    private double latitude;

    @DatabaseField
    private int channel; //定位渠道，0-高德，1-百度，2-其他

    @DatabaseField
    private int netLocateType;

    @DatabaseField
    private float accuracy;

    @DatabaseField
    private String country;

    @DatabaseField
    private String province;

    @DatabaseField
    private String city;

    @DatabaseField
    private String cityCode;

    @DatabaseField
    private String adCode;

    @DatabaseField
    private String address;

    @DatabaseField
    private String poiName;

    @DatabaseField
    private String locateTime;

    public CTNetWorkInfo() {
        this.netLocateType = TYPE_NETWORK_LOCATE;
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

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getNetLocateType() {
        return netLocateType;
    }

    public void setNetLocateType(int netLocateType) {
        this.netLocateType = netLocateType;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getLocateTime() {
        return locateTime;
    }

    public void setLocateTime(String locateTime) {
        this.locateTime = locateTime;
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
            builder.append("网络定位成功：");
            builder.append("long=" + longitude);
            builder.append(", lat=" + latitude);
        } else {
            builder.append("网络定位失败：");
            builder.append(errorCode);
            builder.append(errorMsg);
        }
        return builder.toString();
    }
}
