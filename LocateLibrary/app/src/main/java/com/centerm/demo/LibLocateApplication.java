package com.centerm.demo;

import android.app.Application;

import com.centerm.lib.locate.CTLocateManager;

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
