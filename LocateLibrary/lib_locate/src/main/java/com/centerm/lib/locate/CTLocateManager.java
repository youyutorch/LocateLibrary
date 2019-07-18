package com.centerm.lib.locate;

import android.content.Context;
import android.os.CountDownTimer;

import com.centerm.lib.locate.bean.CTBaseStationInfo;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.client.LocateClientFactory;
import com.centerm.lib.locate.client.LocateClientInf;
import com.centerm.lib.locate.client.LocateClientManager;
import com.centerm.lib.locate.constant.CTLocateConstant;
import com.centerm.lib.locate.inf.CTCollectLocationListener;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.inf.CTGetMulLocationListener;
import com.centerm.lib.locate.util.CLogger;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @desc 定位入口类
 * @author tianyouyu
 * @date 2019/4/29 16:10
 */
public class CTLocateManager {
    private CLogger mCLogger = CLogger.getCLogger(CTLocateManager.class);
    private static Context sContext;

    private static CTLocateManager sCtLocateManager;
    private static ThreadPoolExecutor sExecutor;
    private static int sFormatVersion = CTLocateConstant.FORMAT_VERSION_EPOS;

    private CTCollectLocationListener mCollectLocationListener;
    private CTLocateOption mLocateOption;
    private CountDownTimer mTimer;
    private int mCurrCount;
    private LocateClientManager mLocateClientManager;



    public static void init(Context context) {
        sContext = context;
        sExecutor = new ThreadPoolExecutor(5, 30, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        getInstance();
    }

    public static Context getContext() {
        return sContext;
    }

    public static ThreadPoolExecutor getExecutor() {
        return sExecutor;
    }

    public static void setDebugMode(boolean isDebugMode) {
        if (isDebugMode) {
            CLogger.setLevel(CLogger.LEVEL_DEBUG);
        } else {
            CLogger.setLevel(CLogger.LEVEL_INFO);
        }
    }

    /**
     * 设置位置信息格式化的模式，目前包括EPOS版本和EPAY版本
     * @param version
     */
    public static void setFormatInfoVersion(int version) {
        if (version == CTLocateConstant.FORMAT_VERSION_EPAY) {
            sFormatVersion = version;
        } else {
            sFormatVersion = CTLocateConstant.FORMAT_VERSION_EPOS;
        }
    }

    public static int getFormatInfoVersion() {
        return sFormatVersion;
    }


    public synchronized static CTLocateManager getInstance() {
        if (sCtLocateManager == null) {
            sCtLocateManager = new CTLocateManager();
        }
        return sCtLocateManager;
    }

    private CTLocateManager() {

    }

    /**
     * 开始收集定位，并将有效信息保存在数据库中
     * @return
     */
    public boolean startCollectLocation(CTLocateOption locateOption) {
        if (locateOption == null || locateOption.getInterval() <= 0 || locateOption.getTotalTime() <= 0) {
            return false;
        }
        LocateClientInf locateClientInf = LocateClientFactory.getLocateClient(locateOption.getLocateType(), sContext);
        if (locateClientInf == null) {
            return false;
        }

        if (locateOption.isClearOldInfo()) {
            locateClientInf.clearLocation();
        }

        this.mLocateOption = locateOption;
        mCurrCount = 0;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        long interval = locateOption.getInterval() * 1000;
        long millsInFuture = locateOption.getTotalTime() * 1000;
        mTimer = new CountDownTimer(millsInFuture, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCLogger.debug("onTick");
                mLocateOption.setUseLastUpdate(false);
                mCurrCount++;
                getLocation(mLocateOption, mCollectGetLocationListener);
            }

            @Override
            public void onFinish() {
                mCLogger.debug("onFinish");
                mLocateOption.setUseLastUpdate(true);
                mCurrCount++;
                getLocation(mLocateOption, mCollectGetLocationListener);
                mTimer = null;
            }
        };
        mTimer.start();
        return true;
    }

    /**
     * 停止收集定位
     */
    public void stopCollectLocation() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 设置收集定位监听器
     * @param collectLocationListener
     */
    public void setCollectLocationListener(CTCollectLocationListener collectLocationListener) {
        this.mCollectLocationListener = collectLocationListener;
    }

    /**
     * 获取定位信息,先去实时获取，获取不到的话返回最近更新的定位信息
     * @param locateOption
     * @param getLocationListener
     */
    public void getLocation(CTLocateOption locateOption,  CTGetLocationListener getLocationListener) {
        if (locateOption == null) {
            return;
        }
        LocateClientInf locateClientInf = LocateClientFactory.getLocateClient(locateOption.getLocateType(), sContext);
        if (locateClientInf != null) {
            locateClientInf.getLocation(locateOption, getLocationListener);
        }
    }

    /**
     * 获取最近更新的定位信息
     * @return
     */
    public CTLocateInfo getLastLocation(int type) {
        LocateClientInf locateClientInf = LocateClientFactory.getLocateClient(type, sContext);
        if (locateClientInf == null) {
            return null;
        }
        return locateClientInf.getLastLocation();
    }

    /**
     * 清除定位信息
     * @param type
     */
    public void clearLocation(int type) {
        LocateClientInf locateClientInf = LocateClientFactory.getLocateClient(type, sContext);
        if (locateClientInf != null) {
            locateClientInf.clearLocation();
        }
    }

    /**
     * 清除所有定位信息
     */
    public void clearAllLocation() {
        LocateClientManager clientManager = new LocateClientManager(sContext, this);
        clientManager.clearAllLocation();
    }


    /**
     * 获取多种类型的定位信息
     * @param locateOption
     * @param getMulLocationListener
     */
    public void getMulLocation(CTLocateOption locateOption, CTGetMulLocationListener getMulLocationListener) {
        mLocateClientManager = new LocateClientManager(sContext, this);
        mLocateClientManager.getMulLocation(locateOption, getMulLocationListener);
    }

    /**
     * 取消多种类型定位
     */
    public void stopMulLocation() {
        if (mLocateClientManager != null) {
            mLocateClientManager.stopMulLocation();
        }
    }

    /**
     * 收集位置时，每次获取位置返回的信息
     */
    private CTGetLocationListener mCollectGetLocationListener = new CTGetLocationListener() {
        @Override
        public void onFind(CTLocateInfo ctLocateInfo) {
            if (mCollectLocationListener == null) {
                return;
            }
            if (mLocateOption.isUseLastUpdate()) {
                mCollectLocationListener.onFinish(mCurrCount, ctLocateInfo);
            } else {
                //若是收集基站信息，则判断是否达到指定收集个数
                if (mLocateOption.getLocateType() == CTLocateConstant.TYPE_BASE_STATATION_LOCATE && mLocateOption.getMinCollectCount() > 0) {
                    int count = ((CTBaseStationInfo) ctLocateInfo).getBaseStationCount();
                    if (count >= mLocateOption.getMinCollectCount()) {
                        mCLogger.debug("第" + mCurrCount + "次已收集到指定基站个数");
                        stopCollectLocation();
                        mCollectLocationListener.onFinish(mCurrCount, ctLocateInfo);
                    } else {
                        mCLogger.warn("第" + mCurrCount + "次收集基站,未到达要求个数>>" + mLocateOption.getMinCollectCount() + ", 已收集" + count + "个");
                    }
                } else {
                    mCollectLocationListener.onTick(mCurrCount, ctLocateInfo);
                }
            }
        }
    };

}
