package com.centerm.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.centerm.lib.locate.CTLocateManager;
import com.centerm.lib.locate.CTLocateOption;
import com.centerm.lib.locate.R;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.constant.CTLocateConstant;
import com.centerm.lib.locate.inf.CTCollectLocationListener;
import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.inf.CTGetMulLocationListener;

import java.util.List;

/**
 * @author tianyouyu
 * @desc
 * @date 2019/5/5 14:08
 */
public class MainActivity extends Activity {
    private TextView contentView;
    private StringBuffer stringBuffer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentView = (TextView) findViewById(R.id.content_text);
    }

    /**
     * gps定位测试
     * @param view
     */
    public void gpsTest(View view) {
        contentView.setText("开始GPS定位..");
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_GPS_LOCATE);
        option.setRequestTimeout(5);
        option.setUseLastUpdate(true);
        CTLocateManager.getInstance().getLocation(option, mLocationListener);
    }

    /**
     * gsp定位收集测试
     * @param view
     */
    public void gpsCollectTest(View view) {
        contentView.setText("开始GPS定位收集..");
        stringBuffer = new StringBuffer();
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_GPS_LOCATE);
        option.setInterval(2 * 60);
        option.setTotalTime(30 * 60);
        CTLocateManager.getInstance().setCollectLocationListener(mCollectLocationListener);
        CTLocateManager.getInstance().startCollectLocation(option);
    }

    /**
     * 网络定位测试
     * @param view
     */
    public void netWorkTest(View view) {
        contentView.setText("开始网络定位..");
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_NETWORK_LOCATE);
        CTLocateManager.getInstance().getLocation(option, mLocationListener);
    }

    /**
     * 基站定位测试
     * @param view
     */
    public void stationTest(View view) {
        contentView.setText("开始基站定位..");
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
        option.setBaseMaxCount(5);
        option.setUseLastUpdate(true);
        CTLocateManager.getInstance().getLocation(option, mLocationListener);
    }

    /**
     * wifi定位测试
     * @param view
     */
    public void wifiTest(View view) {
        contentView.setText("开始wifi定位..");
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_WIFI_LOCATE);
        CTLocateManager.getInstance().getLocation(option, mLocationListener);
    }

    /**
     * 基站定位收集测试
     * @param view
     */
    public void stationCollectTest(View view) {
        contentView.setText("开始基站定位收集..");
        stringBuffer = new StringBuffer();
        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_BASE_STATATION_LOCATE);
        option.setInterval(5);
        option.setTotalTime(30);
        option.setBaseMaxCount(5);
        option.setFilterInvalidSignal(false);
        CTLocateManager.getInstance().setCollectLocationListener(mCollectLocationListener);
        CTLocateManager.getInstance().startCollectLocation(option);
    }

    public void mulLocateTest(View view) {
        contentView.setText("开始多种类型定位..");
        CTLocateManager.setFormatInfoVersion(CTLocateConstant.FORMAT_VERSION_EPAY);

        CTLocateOption option = new CTLocateOption(CTLocateConstant.TYPE_ALL_LOCATE);
        option.setBaseMaxCount(8);
        option.setUseGpsFirst(true);
        option.setRequestTimeout(120);
        option.setClearOldInfo(true);
        option.setInitMode(true);
        option.setForceCollect(true);
        CTLocateManager.getInstance().getMulLocation(option, mGetMulLocationListener);
    }

    public void stopLocation(View view) {
        contentView.setText("停止定位..");
        CTLocateManager.getInstance().stopMulLocation();
    }

    private CTGetLocationListener mLocationListener = new CTGetLocationListener() {
        @Override
        public void onFind(CTLocateInfo ctLocateInfo) {
            if (ctLocateInfo == null) {
                contentView.setText("获取位置异常!!");
                return;
            }

            contentView.setText(ctLocateInfo.toString());
        }
    };

    private CTCollectLocationListener mCollectLocationListener = new CTCollectLocationListener() {
        @Override
        public void onTick(int currCount, CTLocateInfo ctLocateInfo) {
            if (ctLocateInfo == null) {
                contentView.setText("第" + currCount + "次获取位置异常!!");
                return;
            }
            stringBuffer.append("第" + currCount + "次收集结果：" + ctLocateInfo.toString() + "\n");
            contentView.setText(stringBuffer.toString());
        }

        @Override
        public void onFinish(int totalCount, CTLocateInfo ctLocateInfo) {
            if (ctLocateInfo == null) {
                contentView.setText("最终收集结果异常");
                return;
            }
            stringBuffer.append("最终收集结果：" + ctLocateInfo.toString() + "\n");
            contentView.setText(stringBuffer.toString());
        }
    };

    private CTGetMulLocationListener mGetMulLocationListener = new CTGetMulLocationListener() {
        @Override
        public void onFind(List<CTLocateInfo> ctLocateInfoList) {
            if (ctLocateInfoList ==null || ctLocateInfoList.isEmpty()) {
                contentView.setText("获取位置异常!!");
                return;
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append("获取多种类型定位结果：\n");
            for (CTLocateInfo locateInfo: ctLocateInfoList) {
                buffer.append(locateInfo.formatInfo() + "\n");
            }
            contentView.setText(buffer.toString());
        }
    };

}
