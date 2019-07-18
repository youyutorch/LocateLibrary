package com.centerm.lib.locate.bean;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * @desc 定位信息基类
 * @author tianyouyu
 * @date 2019/4/29 13:42
 */
public class CTLocateInfo implements Serializable {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    protected int locateType; //定位类型

    @DatabaseField
    protected String lastUpdate; //上次更新时间,格式为yyyyMMDD-HHmmss

    @DatabaseField
    protected boolean isImmediately; //是否获取实时位置

    @DatabaseField
    protected int errorCode; //查询响应码 0为查询成功，其余为查询失败

    @DatabaseField
    protected String errorMsg; //查询失败时的失败信息


    public CTLocateInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocateType() {
        return locateType;
    }

    public void setLocateType(int locateType) {
        this.locateType = locateType;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isImmediately() {
        return isImmediately;
    }

    public void setImmediately(boolean immediately) {
        isImmediately = immediately;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * 将定位信息按一定的格式返回,格式可以自定义
     * @return
     */
    public String formatInfo() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (errorCode == 0) {
            builder.append("定位成功：");
            builder.append("type=" + locateType);
        } else {
            builder.append("定位失败：");
            builder.append(errorCode);
            builder.append(errorMsg);
        }
        return builder.toString();
    }
}
