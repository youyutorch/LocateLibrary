package com.centerm.lib.locate.database;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

import java.util.List;

/**
 * Created by yuhc on 2017/7/31.
 * 项目中用到的数据库
 */

public interface IProjectDBTable {

    /**
     * 注册项目中用到的数据表
     * @return  数据表对应的ORM类
     */
    List<Class> registerDBClass();

    /**
     * 数据库版本升级时，项目需要进行的操作
     * @param database  数据库
     * @param connectionSource  数据源
     * @param oldVersion    旧版本号
     * @param newVersion    新版本号
     */
    void onDBUpdate(CTDbHelper CTDbHelper, SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int
            newVersion);

    /**
     * 数据库创建时的处理
     * @param database  数据库
     * @param connectionSource  数据源
     */
    void onCreate(CTDbHelper CTDbHelper, SQLiteDatabase database, ConnectionSource connectionSource);
}
