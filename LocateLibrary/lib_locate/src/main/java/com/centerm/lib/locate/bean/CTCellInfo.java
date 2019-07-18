package com.centerm.lib.locate.bean;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.centerm.lib.locate.constant.CTLocateConstant.TYPE_BASE_STATATION_LOCATE;

/**
 * @desc 基站定位信息
 * @author tianyouyu
 * @date 2019/4/29 14:36
 */
@DatabaseTable(tableName = "tb_base_station_locate")
public class CTCellInfo extends CTLocateInfo implements Comparable<CTCellInfo>{
    public final static int NETWORK_TYPE_CDMA = 1;
    public final static int NETWORK_TYPE_GSM = 2;
    public final static int NETWORK_TYPE_WCDMA = 3;
    public final static int NETWORK_TYPE_LTE = 4;

    @DatabaseField
    private String operator; //运营商

    @DatabaseField
    private String imsi; //手机卡序列号，唯一标识

    @DatabaseField
    private int networkType; //网络类型

    @DatabaseField
    private String mcc; //移动国家代码

    @DatabaseField
    private String mnc; //移动网络号

    /**
     * 根据sid,nid,bid这三个基站数据可确定对应的经纬度信息
     */
    @DatabaseField
    private int sid; //系统识别码, LTE类型为tac， GSM和WCDMA类型为lac， CDMA为sid

    @DatabaseField
    private int nid; //网络识别码, LTE类型为ci， GSM和WCDMA类型为cid， CDMA为nid

    @DatabaseField
    private int bid; //信元标识, LTE类型为pci， GSM和WCDMA类型为psc， CDMA为bid

    @DatabaseField
    private int dbm; //信号分贝，手机一般为负数

    @DatabaseField
    private int asu; //信号单元

    @DatabaseField
    private int level; //信号格数

    public CTCellInfo() {
        this.locateType = TYPE_BASE_STATATION_LOCATE;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public int getNetworkType() {
        return networkType;
    }

    public void setNetworkType(int networkType) {
        this.networkType = networkType;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public int getAsu() {
        return asu;
    }

    public void setAsu(int asu) {
        this.asu = asu;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (errorCode == 0) {
            builder.append("{CellInfo:");
            builder.append("update=" + lastUpdate);
            builder.append(", imsi=" + imsi);
            builder.append(", type=" + networkType);
            builder.append(", mcc=" + mcc);
            builder.append(", mnc=" + mnc);
            if (networkType == NETWORK_TYPE_CDMA) {
                builder.append(", sid=" + sid);
                builder.append(", nid=" + nid);
                builder.append(", bid=" + bid);
            } else {
                builder.append(", tac=" + sid);
                builder.append(", ci=" + nid);
                builder.append(", pci=" + bid);
            }
            builder.append(", dbm=" + dbm);
            builder.append(", asu=" + asu);
            builder.append(", level=" + level);
            builder.append("}");
        }
        return builder.toString();
    }

    @Override
    public int compareTo(@NonNull CTCellInfo another) {
        //优先根据更新时间来排序
        if (!TextUtils.isEmpty(lastUpdate) && !TextUtils.isEmpty(another.getLastUpdate())) {
            int result = lastUpdate.compareTo(another.getLastUpdate());
            if (result != 0) {
                return result;
            }
        }

        //根据信号强弱来进行排序
        if (this.level > another.getLevel()) {
            return 1;
        } else if (this.level < another.getLevel()) {
            return -1;
        }

        if (this.asu > another.getAsu()) {
            return 1;
        } else if (this.asu < another.getAsu()) {
            return -1;
        }

        if (this.dbm > another.getDbm()) {
            return 1;
        } else if (this.dbm < another.getDbm()) {
            return -1;
        }
        return 0;
    }
}
