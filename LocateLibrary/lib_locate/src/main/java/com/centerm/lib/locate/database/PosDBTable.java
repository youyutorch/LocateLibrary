package com.centerm.lib.locate.database;

import android.database.sqlite.SQLiteDatabase;

import com.centerm.lib.locate.bean.CTCellInfo;
import com.centerm.lib.locate.bean.CTGpsInfo;
import com.centerm.lib.locate.bean.CTNetWorkInfo;
import com.centerm.lib.locate.bean.CTWifiInfo;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhc on 2017/7/31.
 *
 */

public class PosDBTable implements IProjectDBTable {
    private List<Class> clses = new ArrayList<>();

    @Override
    public List<Class> registerDBClass() {
        clses.add(CTNetWorkInfo.class);
        clses.add(CTCellInfo.class);
        clses.add(CTGpsInfo.class);
        clses.add(CTWifiInfo.class);
        return clses;
    }

    @Override
    public void onDBUpdate(CTDbHelper CTDbHelper, SQLiteDatabase database, ConnectionSource connectionSource, int
            oldVersion, int newVersion) {
        try {
            registerDBClass();
            for (Class cls : clses) {
                TableUtils.createTableIfNotExists(connectionSource, cls);
            }
//            //数据库版本提升至7
//            if (oldVersion < 7){
//                //SQLite不支持同时添加多列，所以只能一个一个加
//                CTDbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
//                        "reverseFieldInfo TEXT DEFAULT '';");
//                CTDbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
//                        "unicom_scna_type TEXT DEFAULT '';");
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(CTDbHelper CTDbHelper, SQLiteDatabase database, ConnectionSource connectionSource) {

    }
}
