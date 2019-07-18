package com.centerm.lib.locate.client;

import com.centerm.lib.locate.inf.CTGetLocationListener;
import com.centerm.lib.locate.bean.CTLocateInfo;
import com.centerm.lib.locate.CTLocateOption;

import java.util.List;

/**
 * @desc 定位接口类
 * @author tianyouyu
 * @date 2019/4/30 15:03
 */
public interface LocateClientInf {
    void getLocation(CTLocateOption locateOption, CTGetLocationListener getLocationListener);

    CTLocateInfo getLastLocation();

    boolean saveLocation(CTLocateInfo locateInfo);

    void clearLocation();

    void stopLocation();
}
