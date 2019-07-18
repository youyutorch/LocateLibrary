package com.centerm.lib.locate.debug;

import android.app.Application;
import android.util.Log;

import com.centerm.lib.locate.CTLocateManager;

import com.centerm.lib.locate.util.CLogger;

/**
 * @author tianyouyu
 * @desc
 * @date 2019/5/5 15:26
 */
public class LibLocateApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CTLocateManager.init(this);
        CTLocateManager.setDebugMode(true);
    }

}
