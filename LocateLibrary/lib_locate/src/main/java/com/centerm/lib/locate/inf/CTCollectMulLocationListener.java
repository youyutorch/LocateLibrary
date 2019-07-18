package com.centerm.lib.locate.inf;

import com.centerm.lib.locate.bean.CTLocateInfo;

import java.util.List;

/**
 * @desc 收集定位监听器(多种定位类型同时使用)
 * @author tianyouyu
 * @date 2019/4/30 14:40
 */
public interface CTCollectMulLocationListener {
    /**
     * 每次定位返回的信息
     * @param currCount
     * @param ctLocateInfoList
     */
    void onTick(int currCount, List<? extends CTLocateInfo> ctLocateInfoList);

    /**
     * 收集结束时返回的最近更新信息
     * @param totalCount
     * @param ctLocateInfoList
     */
    void onFinish(int totalCount, List<? extends CTLocateInfo> ctLocateInfoList);
}
