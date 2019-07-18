package com.centerm.lib.locate.database;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author linwenhui
 * @date 2016/10/27.
 */
public class CommonDao<T extends Object> {
    private CTDbHelper CTDbHelper;
    private Dao commonDao;

    public CommonDao(Class<T> clz, CTDbHelper CTDbHelper) {
        this.CTDbHelper = CTDbHelper;
        try {
            commonDao = CTDbHelper.getDao(clz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean save(T model) {
        try {
            return commonDao.create(model) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean save(List<T> models) {
        try {
            return commonDao.create(models) == models.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(T model) {
        try {
            return commonDao.delete(model) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(List<T> models) {
        try {
            return commonDao.delete(models) == models.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param sql 删除表数据的sql语句
     * @return
     */
    public boolean deleteBySQL(String sql) {
        SQLiteDatabase db = CTDbHelper.getWritableDatabase();
        try {
            db.execSQL(sql);
            return true;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 同deleteBySQL(String sql)
     *
     * @param sqls
     * @return
     */
    public boolean deleteBySQL(List<String> sqls) {
        SQLiteDatabase db = CTDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (String sql : sqls)
                db.execSQL(sql);
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param where 删除数据的where条件，如id='1'
     * @return
     */
    public boolean deleteByWhere(String where) {
        SQLiteDatabase db = CTDbHelper.getWritableDatabase();
        String sql;
        if (TextUtils.isEmpty(where)) {
            sql = "DELETE FROM " + commonDao.getTableName();
        } else {
            sql = "DELETE FROM " + commonDao.getTableName() + " WHERE " + where;
        }
        try {
            db.execSQL(sql);
            return true;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteById(Object id) {
        try {
            return commonDao.deleteById(id) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByIds(Collection<? extends Object> ids) {
        try {
            return commonDao.deleteIds(ids) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<T> query() {
        try {
            return commonDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T queryForId(Object id) {
        try {
            return (T) commonDao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 条件以AND方式进行连接查询
     *
     * @param conditions
     * @return
     */
    public List<T> queryByMap(Map<String, String> conditions) {
        return queryByMap(conditions, 1);
    }

    /**
     * @param conditions
     * @param option     连接符 1：AND，2：OR
     * @return
     */
    public List<T> queryByMap(Map<String, String> conditions, int option) {
        Where where = commonDao.queryBuilder().where();
        if (conditions != null) {
            int size = conditions.size(), i = 0;
            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                try {
                    where.eq(entry.getKey(), entry.getValue());
                    if (i++ < size - 1) {
                        if (option == 1)
                            where.and();
                        else if (option == 2)
                            where.or();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            return where.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public QueryBuilder queryBuilder() {
        return commonDao.queryBuilder();
    }

    public boolean update(T model) {
        try {
            return commonDao.update(model) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改model的主键为id
     *
     * @param model
     * @param id
     * @return
     */
    public boolean updateId(T model, Object id) {
        try {
            return commonDao.updateId(model, id) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param conditions 更新字段
     * @param id
     * @return
     */
    public boolean updateByMap(Map<String, String> conditions, Object id) {
        UpdateBuilder updateBuilder = commonDao.updateBuilder();
        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            try {
                updateBuilder.updateColumnValue(entry.getKey(), entry.getValue());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            updateBuilder.where().idEq(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            return updateBuilder.update() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UpdateBuilder updateBuilder() {
        return commonDao.updateBuilder();
    }

    public long countOf() {
        try {
            return commonDao.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long countOf(PreparedQuery<T> preparedQuery) {
        try {
            return commonDao.countOf(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean refresh(T model){
        try {
            return commonDao.refresh(model) == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
