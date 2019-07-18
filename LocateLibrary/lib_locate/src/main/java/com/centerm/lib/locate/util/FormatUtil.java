package com.centerm.lib.locate.util;

import android.text.TextUtils;

import com.centerm.lib.locate.BuildConfig;
import com.centerm.lib.locate.CTLocateManager;
import com.centerm.lib.locate.bean.CTCellInfo;
import com.centerm.lib.locate.constant.CTLocateConstant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 格式化工具类
 * Created by torch on 2018/3/27.
 */

public class FormatUtil {
    private static final String TAG = "FormatUtil";
    public static final String DATE_PATTERN = "yyyyMMdd-HHmmss";

    /**
     * 填充指定的String字符串到指定长度
     * @param src
     * @param fillStr
     * @param fixLength
     * @return
     */
    public static String fillToFixLength(String src, String fillStr, int fixLength) {
        return fillToFixLength(src, fillStr, fixLength, true);
    }

    public static String fillToFixLength(String src, String fillStr, int fixLength, boolean fillEnd) {
        if (TextUtils.isEmpty(fillStr)) {
            return src;
        }

        int srcLength = 0;
        if (!TextUtils.isEmpty(src)) {
            srcLength = src.length();
        }

        if (fixLength <= srcLength) {
            return src.substring(0, fixLength);
        }

        StringBuffer buffer = new StringBuffer();
        if (!TextUtils.isEmpty(src) && fillEnd) {
            buffer.append(src);
        }

        for (int i = 0; i < fixLength - srcLength; i = i + fillStr.length()) {
            buffer.append(fillStr);
        }

        if (!TextUtils.isEmpty(src) && !fillEnd) {
            buffer.append(src);
        }
        return buffer.toString();
    }

    /**
     * 获取格式化的经度值,add by tianyouyu 2018/10/23 15:33
     * 格式：3位整数+1位小数点+6位小数
     */
    public static String getFormatLongitude(double longitude) {
        String[] splits = String.valueOf(longitude).split("\\.");
        if (splits.length == 0) {
            //无小数部分
            String longStr = fillToFixLength(String.valueOf(longitude), "0", 3, false);
            return longStr + ".000000";
        }
        //整数部分
        String positive = fillToFixLength(splits[0], "0", 3, false);
        //小数部分
        String decimal = "";
        if (splits.length >= 2) {
            decimal = fillToFixLength(splits[1], "0", 6);
        } else {
            decimal = "000000";
        }
        return positive + "." + decimal;
    }

    /**
     * 获取格式化的纬度值,add by tianyouyu 2018/10/23 15:33
     * 格式：1位正负号+2位整数+1位小数点+6位小数
     */
    public static String getFormatLatitude(double latitude) {
        String[] splits = String.valueOf(latitude).split("\\.");
        if (splits.length == 0) {
            //无小数部分
            String symStr = (latitude >= 0) ? "+" : "-";
            String lanStr = fillToFixLength(String.valueOf(latitude), "0", 2, false);
            return symStr + lanStr + ".000000";
        }
        //符号
        String symbol = (latitude >= 0) ? "+" : "-";
        //整数部分
        String positive = fillToFixLength(splits[0], "0", 2, false);
        //小数部分
        String decimal = "";
        if (splits.length >= 2) {
            decimal = fillToFixLength(splits[1], "0", 6);
        } else {
            decimal = "000000";
        }
        return symbol + positive + "." + decimal;
    }

    public static String getFormatStation(List<CTCellInfo> cellInfoList) {
        if (cellInfoList == null || cellInfoList.isEmpty()) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (CTCellInfo cellInfo : cellInfoList) {
            String type = cellInfo.getNetworkType() == CTCellInfo.NETWORK_TYPE_CDMA ? "1" : "0";
            //卡部版本增加此标识，固话版本暂没有添加，add by tianyouyu 2019/6/15 16:46
            if (CTLocateManager.getFormatInfoVersion() == CTLocateConstant.FORMAT_VERSION_EPOS) {
                buffer.append(type + "-");
            }
            buffer.append(cellInfo.getMcc() + "-");
            if (cellInfo.getNetworkType() == CTCellInfo.NETWORK_TYPE_CDMA) {
                buffer.append(cellInfo.getSid() + "-");
                buffer.append(cellInfo.getNid() + "-");
                buffer.append(cellInfo.getBid());
            } else {
                buffer.append(cellInfo.getMnc() + "-");
                buffer.append(cellInfo.getSid() + "-");
                buffer.append(cellInfo.getNid());
            }
            buffer.append(",");
        }
        buffer.deleteCharAt(buffer.length() - 1);

        return buffer.toString();
    }

    /**
     * 获取指定时间的格式化字符串（格式为yyyyMMdd-HHmmss）
     *
     * @param date 日期
     * @return
     */
    public static String getFormatDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(date);
    }

    public static String getFormatDate() {
        return getFormatDate(null);
    }
}
