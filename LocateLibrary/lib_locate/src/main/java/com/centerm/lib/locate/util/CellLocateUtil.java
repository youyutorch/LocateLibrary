package com.centerm.lib.locate.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.centerm.lib.locate.bean.CTCellInfo;
import com.centerm.lib.locate.constant.CTLocateConstant;

import com.centerm.lib.locate.util.CLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by daizy
 * date 2018/9/14.
 */
public class CellLocateUtil {
    private static CLogger logger = CLogger.getCLogger(CellLocateUtil.class);
    //网络模式
    public static final int GSM_ONLY = 1;
    public static final int WCDMA_ONLY = 2;
    public static final int CDMA_ONLY = 5;
    public static final int EVDO_ONLY = 6;
    public static final int LTE_GSM_CDMA_AUTO = 10;
    public static final int LTE_ONLY = 11;

    public static String getModeName(int mode) {
        String name = "通用";
        switch (mode) {
            case GSM_ONLY:
                name = "2G";
                break;
            case WCDMA_ONLY:
                name = "3G";
                break;
            case CDMA_ONLY:
                name = "2G";
                break;
            case EVDO_ONLY:
                name = "3G";
                break;
            case LTE_GSM_CDMA_AUTO:
                name = "通用";
                break;
            case LTE_ONLY:
                name = "4G";
                break;
            default:
                name = "通用";
                break;
        }
        return name;
    }

    @SuppressLint("MissingPermission")
    public static String getImsi(Context context, String defaultImsi) {
        String ret = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            ret = telephonyManager.getSubscriberId();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (!TextUtils.isEmpty(ret)){
            return ret;
        } else {
            return defaultImsi;
        }
    }

    public static List<CTCellInfo> getCellInfoList(Context context) {
        return getCellInfoList(context, 0, true);
    }

    /**
     * 获取基站信息
     * @param context
     * @param minLevel 最低信号格数
     * @param filterInvalidSignal 是否过滤无效信号
     * @return
     */
    public static List<CTCellInfo> getCellInfoList(Context context, int minLevel, boolean filterInvalidSignal) {
        logger.debug("getCellInfoList>> context=" + context + ", minLevel=" + minLevel + ", filterInvalidSignal=" + filterInvalidSignal);

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        @SuppressLint("MissingPermission")
        List<CellInfo> cellInfoList = manager.getAllCellInfo();
        if (cellInfoList == null) {
            return null;
        }
        logger.debug("cellInfoList: " + cellInfoList.size());
        List<CTCellInfo> listMyCellInfo = new ArrayList<>();

        String imsi = getImsi(context, CTLocateConstant.EMPTY_IMSI);
        String operator = getSimOperatorName(context);
        String mcc = "460";
        if (!TextUtils.isEmpty(manager.getNetworkOperator()) && manager.getNetworkOperator().length() >= 3) {
            mcc = manager.getNetworkOperator().substring(0, 3);
        }
        for (int i = 0; i < cellInfoList.size(); i++) {
            CTCellInfo myCellInfo = new CTCellInfo();
            myCellInfo.setImsi(imsi);
            myCellInfo.setMcc(mcc);
            myCellInfo.setOperator(operator);
            myCellInfo.setLastUpdate(FormatUtil.getFormatDate());

            CellInfo cellInfo = cellInfoList.get(i);
            if (cellInfo instanceof CellInfoCdma) {
                CellIdentityCdma cellIdentityCdma = ((CellInfoCdma) cellInfo).getCellIdentity();
                CellSignalStrengthCdma cellSsCdma = ((CellInfoCdma) cellInfo).getCellSignalStrength();

                myCellInfo.setNetworkType(CTCellInfo.NETWORK_TYPE_CDMA);
                myCellInfo.setMnc(getValidMnc(-1, imsi));
                myCellInfo.setSid(cellIdentityCdma.getSystemId());
                myCellInfo.setNid(cellIdentityCdma.getNetworkId());
                myCellInfo.setBid(cellIdentityCdma.getBasestationId());
                myCellInfo.setDbm(cellSsCdma.getDbm());
                myCellInfo.setAsu(cellSsCdma.getAsuLevel());
                myCellInfo.setLevel(cellSsCdma.getLevel());

            } else if (cellInfo instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();
                CellSignalStrengthGsm cellSsGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();

                myCellInfo.setNetworkType(CTCellInfo.NETWORK_TYPE_GSM);
                myCellInfo.setMnc(getValidMnc(cellIdentityGsm.getMnc(), imsi));
                myCellInfo.setSid(cellIdentityGsm.getLac());
                myCellInfo.setNid(cellIdentityGsm.getCid());
                myCellInfo.setBid(cellIdentityGsm.getPsc());
                myCellInfo.setDbm(cellSsGsm.getDbm());
                myCellInfo.setAsu(cellSsGsm.getAsuLevel());
                myCellInfo.setLevel(cellSsGsm.getLevel());

            } else if (cellInfo instanceof CellInfoLte) {
                CellIdentityLte cellIdentityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                CellSignalStrengthLte cellSsLte = ((CellInfoLte) cellInfo).getCellSignalStrength();

                myCellInfo.setNetworkType(CTCellInfo.NETWORK_TYPE_LTE);
                myCellInfo.setMnc(getValidMnc(cellIdentityLte.getMnc(), imsi));
                myCellInfo.setSid(cellIdentityLte.getTac());
                myCellInfo.setNid(cellIdentityLte.getCi());
                myCellInfo.setBid(cellIdentityLte.getPci());
                myCellInfo.setDbm(cellSsLte.getDbm());
                myCellInfo.setAsu(cellSsLte.getAsuLevel());
                myCellInfo.setLevel(cellSsLte.getLevel());

            } else if (cellInfo instanceof CellInfoWcdma) {
                CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) cellInfo).getCellIdentity();
                CellSignalStrengthWcdma cellSsWcdma = ((CellInfoWcdma) cellInfo).getCellSignalStrength();

                myCellInfo.setNetworkType(CTCellInfo.NETWORK_TYPE_WCDMA);
                myCellInfo.setMnc(getValidMnc(cellIdentityWcdma.getMnc(), imsi));
                myCellInfo.setSid(cellIdentityWcdma.getLac());
                myCellInfo.setNid(cellIdentityWcdma.getCid());
                myCellInfo.setBid(cellIdentityWcdma.getPsc());
                myCellInfo.setDbm(cellSsWcdma.getDbm());
                myCellInfo.setAsu(cellSsWcdma.getAsuLevel());
                myCellInfo.setLevel(cellSsWcdma.getLevel());
            }
            //过滤不合法基站
            if (myCellInfo.getSid() < 0 || myCellInfo.getNid() < 0 || myCellInfo.getBid() < 0
                    || (myCellInfo.getSid() == Integer.MAX_VALUE && myCellInfo.getNid() == Integer.MAX_VALUE && myCellInfo.getBid() == Integer.MAX_VALUE)) {
                logger.debug("filter illegal cellinfo:" + myCellInfo);
                continue;
            }
            if (myCellInfo.getNetworkType() != CTCellInfo.NETWORK_TYPE_CDMA &&
                    (myCellInfo.getMnc() == null || myCellInfo.getMnc().equals(String.valueOf(Integer.MAX_VALUE)))) {
                logger.debug("filter illegal cellinfo:" + myCellInfo);
                continue;
            }

            //过滤重复基站
            if (findCellId(myCellInfo, listMyCellInfo, filterInvalidSignal) >= 0) {
                logger.debug("filter exist cellinfo:" + myCellInfo);
                continue;
            }

            //过滤无效基站
            if (filterInvalidSignal && myCellInfo.getNetworkType() != CTCellInfo.NETWORK_TYPE_CDMA &&
                    myCellInfo.getSid() == Integer.MAX_VALUE && myCellInfo.getNid() == Integer.MAX_VALUE) {
                logger.debug("filter invalid cellinfo:" + myCellInfo);
                continue;
            }
            if (myCellInfo.getLevel() < minLevel) {
                logger.debug("filter low level cellinfo:" + myCellInfo);
                continue;
            }

            //just for test
//            myCellInfo.setNid(myCellInfo.getNid() + new Random().nextInt(10));
//            myCellInfo.setBid(myCellInfo.getBid() + new Random().nextInt(10));

            listMyCellInfo.add(myCellInfo);
        }
        logger.debug("final cellInfoList: " + listMyCellInfo.size());
        logger.debug("final cellInfoList: " + listMyCellInfo);

        return listMyCellInfo;
    }

    /**
     * 判断当前基站是否已存在
     * @param ctCellInfo
     * @param orignalCellInfos
     * @param isFilterInvalid
     * @return 若存在返回对应的Id,否则返回-1
     */
    public static int findCellId(CTCellInfo ctCellInfo, List<CTCellInfo> orignalCellInfos, boolean isFilterInvalid) {
        if (ctCellInfo == null || orignalCellInfos == null || orignalCellInfos.isEmpty()) {
            return -1;
        }

        for (CTCellInfo oriCellInfo : orignalCellInfos) {
            if (ctCellInfo.getNetworkType() == CTCellInfo.NETWORK_TYPE_CDMA) {
                if (ctCellInfo.getBid() == oriCellInfo.getBid()) {
                    return oriCellInfo.getId();
                }
            } else {
                if (isFilterInvalid) {
                    if (ctCellInfo.getNid() == oriCellInfo.getNid()) {
                        return oriCellInfo.getId();
                    }
                } else {
                    if (ctCellInfo.getNid() == oriCellInfo.getNid() && ctCellInfo.getSid() == oriCellInfo.getSid()) {
                        return oriCellInfo.getId();
                    }
                }
            }
        }
        return -1;
    }

    private static String getValidMnc(int mnc, String imsi) {
        if (mnc > 0 && mnc != Integer.MAX_VALUE) {
            return String.valueOf(mnc);
        }
        if (!TextUtils.isEmpty(imsi) && imsi.length() >= 5) {
            return imsi.substring(3, 5);
        }
        return null;
    }

    /**
     * 切换网络模式
     *
     * @param context
     * @param type
     */
    public static void changeNetworkType(Context context, int type) {
        Intent intent = new Intent("com.centerm.network.CHANGE_PRERRED");
        intent.setPackage("com.centerm.frame");
        intent.putExtra("mode", type);
        context.startService(intent);
    }

    /**
     * 获取当前网络对应的运营商
     *
     * @param context
     * @return
     */
    public static String getSimOperatorName(Context context) {
        TelephonyManager service = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assert service != null;
        String name = service.getSimOperator();
        String networkTypeName;
        switch (name) {
            case "46000":
            case "46002":
            case "46007":
                networkTypeName = "移动";
                break;
            case "46001":
            case "46006":
                networkTypeName = "联通";
                break;
            case "46003":
            case "46011":
                networkTypeName = "电信";
                break;
            default:
                networkTypeName = "未知";
                break;
        }
        return networkTypeName;
    }

    /**
     * 移动:GSM,LTE
     * 联通:GSM,WCDMA,LTE
     * 电信:CDMA,EVDO,LTE
     */
    public static List<Integer> getSimNetType(Context context) {
        TelephonyManager service = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assert service != null;
        String name = service.getSimOperator();
        List<Integer> list = new ArrayList<>();
        switch (name) {
            case "46000":
            case "46002":
            case "46007":
                list.add(GSM_ONLY);
                list.add(LTE_ONLY);
                list.add(LTE_GSM_CDMA_AUTO);
                break;
            case "46001":
            case "46006":
                list.add(GSM_ONLY);
                list.add(WCDMA_ONLY);
                list.add(LTE_ONLY);
                list.add(LTE_GSM_CDMA_AUTO);
                break;
            case "46003":
            case "46011":
                list.add(CDMA_ONLY);
                list.add(EVDO_ONLY);
                list.add(LTE_ONLY);
                list.add(LTE_GSM_CDMA_AUTO);
                break;
            default:
                list.add(GSM_ONLY);
                list.add(WCDMA_ONLY);
                list.add(CDMA_ONLY);
                list.add(EVDO_ONLY);
                list.add(LTE_ONLY);
                list.add(LTE_GSM_CDMA_AUTO);
                break;
        }
        return list;
    }

    public static String getNetworkTypeName(Context context) {
        TelephonyManager service = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkTypeName = "";
        switch (service.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                networkTypeName = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                networkTypeName = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                networkTypeName = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                networkTypeName = "EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                networkTypeName = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                networkTypeName = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                networkTypeName = "EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                networkTypeName = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                networkTypeName = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                networkTypeName = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                networkTypeName = "HSPAP";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                networkTypeName = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                networkTypeName = "IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                networkTypeName = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                networkTypeName = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                networkTypeName = "UNKNOWN";
                break;
            default:
                networkTypeName = "NONE";
                break;
        }
        return networkTypeName;
    }
}
