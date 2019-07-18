package com.centerm.lib.locate.util;

import android.util.Log;

/**
 * @desc 日志帮助类
 * @author tianyouyu
 * @date 2019/6/19 18:58
 */
public class CLogger {
    /**
     * 打印级别，从低到高
     */
    public static final int LEVEL_DEBUG = 0;
    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_WARN = 2;
    public static final int LEVEL_ERROR = 3;
    public static final int LEVEL_NONE = 4;
    private static final String DEFAULT_TAG = "CCLogger";

    private static int sLevel = LEVEL_INFO;

    private String tag;

    public static CLogger getCLogger(Class clz) {
        String tag = clz == null ? DEFAULT_TAG : clz.getName();
        return new CLogger(tag);
    }

    public static void setLevel(int level) {
        if (level >= LEVEL_DEBUG && level <= LEVEL_NONE) {
            sLevel = level;
        }
    }

    private CLogger(String tag) {
        this.tag = tag;
    }

    public void debug(String msg) {
        if (sLevel > LEVEL_DEBUG) {
            return;
        }
        Log.d(tag, msg);
    }

    public void info(String msg) {
        if (sLevel > LEVEL_INFO) {
            return;
        }
        Log.i(tag, msg);
    }

    public void warn(String msg) {
        if (sLevel > LEVEL_WARN) {
            return;
        }
        Log.w(tag, msg);
    }

    public void error(String msg) {
        if (sLevel > LEVEL_ERROR) {
            return;
        }
        Log.e(tag, msg);
    }

}
