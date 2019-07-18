package com.centerm.lib.locate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.centerm.lib.locate.CTLocateManager;
import com.centerm.lib.locate.constant.Config;
import com.centerm.lib.locate.util.CLogger;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public class CTDbHelper extends OrmLiteSqliteOpenHelper {
    private CLogger logger = CLogger.getCLogger(this.getClass());
    private static CTDbHelper mCTDbHelper;

    // 数据库名
    private final static String DB_NAME = Config.DB_NAME;

    private Map<String, Dao> daos = new HashMap<>();

    public CTDbHelper(Context ctx) {
        super(ctx.getApplicationContext(), DB_NAME, null, Config.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        List<Class> clses = new ArrayList<>();
        PosDBTable mPosDBTable = new PosDBTable();
        clses.addAll(mPosDBTable.registerDBClass());
        try {
            for (Class cls : clses) {
                TableUtils.createTable(connectionSource, cls);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mPosDBTable.onCreate(this, database, connectionSource);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        PosDBTable mPosDBTable = new PosDBTable();
        //注意：要兼容不同版本的数据库
        if (oldVersion != Config.DB_VERSION) {
            logger.debug("^_^ 基础框架数据库版本号提升：oldVersion = " + oldVersion + " newVersion = " +
                    Config.DB_VERSION + "" + " " + "^_^");
            //版本变更了才调用实际的处理
            mPosDBTable.onDBUpdate(this, database, connectionSource, oldVersion, Config.DB_VERSION);
        }
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        String className = clazz.getSimpleName();
        Dao dao = null;
        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    public synchronized void removeDao(Class clazz) {
        String className = clazz.getSimpleName();
        if (daos.containsKey(className)) {
            daos.remove(className);
        }
    }

    @Override
    public void close() {
        super.close();
        daos.clear();
    }

    public static CTDbHelper getInstance() {
        if (mCTDbHelper == null) {
            mCTDbHelper = new CTDbHelper(CTLocateManager.getContext());
        }
        return mCTDbHelper;
    }

    public static void releaseInstance() {
        if (mCTDbHelper != null) {
            mCTDbHelper.close();
            mCTDbHelper = null;
        }
    }
}
