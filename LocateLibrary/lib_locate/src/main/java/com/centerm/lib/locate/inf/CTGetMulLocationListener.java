package com.centerm.lib.locate.inf;

import com.centerm.lib.locate.bean.CTLocateInfo;

import java.util.List;

/**
 * @desc 获取定位信息监听器
 * @author tianyouyu
 * @date 2019/4/30 14:46
 */
public interface CTGetMulLocationListener {
    void onFind(List<CTLocateInfo> ctLocateInfoList);
}
