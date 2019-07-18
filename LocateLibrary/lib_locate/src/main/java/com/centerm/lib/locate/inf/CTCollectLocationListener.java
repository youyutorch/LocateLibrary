package com.centerm.lib.locate.inf;

import com.centerm.lib.locate.bean.CTLocateInfo;

import java.util.List;

/**
 * @desc 收集定位监听器
 * @author tianyouyu
 * @date 2019/4/30 14:40
 */
public interface CTCollectLocationListener {
    /**
     * 每次定位返回的信息
     * @param currCount
     * @param ctLocateInfo
     */
    void onTick(int currCount, CTLocateInfo ctLocateInfo);

    /**
     * 收集结束时返回的最近更新信息
     * @param totalCount
     * @param ctLocateInfo
     */
    void onFinish(int totalCount, CTLocateInfo ctLocateInfo);
}
