package com.centerm.lib.locate.bean;

import com.centerm.lib.locate.constant.CTLocateConstant;
import com.centerm.lib.locate.util.FormatUtil;

import java.text.Format;
import java.util.List;

/**
 * @desc 基站定位信息
 * @author tianyouyu
 * @date 2019/5/5 16:20
 */
public class CTBaseStationInfo extends CTLocateInfo {
    //要获取的最大基站个数
    private int baseMaxCount = Integer.MAX_VALUE;

    //最小的信号强度，0到5格
    private int minSignalStrength;

    private List<CTCellInfo> cellInfos;

    public CTBaseStationInfo() {
        this.locateType = CTLocateConstant.TYPE_BASE_STATATION_LOCATE;
    }

    public List<CTCellInfo> getCellInfos() {
        return cellInfos;
    }

    public void setCellInfos(List<CTCellInfo> cellInfos) {
        this.cellInfos = cellInfos;
    }

    public int getBaseMaxCount() {
        return baseMaxCount;
    }

    public void setBaseMaxCount(int baseMaxCount) {
        this.baseMaxCount = baseMaxCount;
    }

    public int getMinSignalStrength() {
        return minSignalStrength;
    }

    public void setMinSignalStrength(int minSignalStrength) {
        this.minSignalStrength = minSignalStrength;
    }

    public int getBaseStationCount() {
        if (cellInfos == null || cellInfos.isEmpty()) {
            return 0;
        }
        return cellInfos.size();
    }

    @Override
    public String formatInfo() {
        if (errorCode != 0 || cellInfos == null || cellInfos.isEmpty()) {
            return super.formatInfo();
        } else {
            return FormatUtil.getFormatStation(cellInfos);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (errorCode == 0) {
            builder.append("基站定位成功：");
            builder.append("size=" + cellInfos.size());
            builder.append(", stations=" + cellInfos);
        } else {
            builder.append("基站定位失败：");
            builder.append(errorMsg);
        }
        return builder.toString();
    }
}
