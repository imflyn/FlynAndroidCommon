package com.flyn.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public abstract class GenericSQLiteOpenHelper extends SQLiteOpenHelper
{
    private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final static ReadLock readLock = lock.readLock();
    private final static WriteLock writeLock = lock.writeLock();
    private SQLiteDatabase db;

    public GenericSQLiteOpenHelper(Context context, String dbName, int version)
    {
        super(context, dbName, null, version);
    }

    public List<Map<String, String>> rawQuery(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
    {
        List<Map<String, String>> returnVal = new LinkedList<Map<String, String>>();
        Cursor cursor = null;
        try
        {
            lock.readLock().lock();
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            int columnCount = cursor.getColumnCount();
            while (true)
            {
                if (!cursor.moveToNext())
                {
                    List<Map<String, String>> localList = returnVal;
                    return localList;
                }
                Map<String, String> row = new HashMap<String, String>();
                for (int i = 0; i < columnCount; i++)
                {
                    String name = cursor.getColumnName(i);
                    String value = cursor.getString(i);
                    row.put(name, value);
                }
                returnVal.add(row);
            }
        } finally
        {
            try
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            } finally
            {
                if ((db != null) && (closeDB))
                {
                    db.close();
                }
            }
            readLock.unlock();
        }
    }

    public Map<String, String> rawQueryForFirstRow(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
    {
        Cursor cursor = null;
        try
        {
            readLock.lock();
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            if (!cursor.moveToNext())
            {
                return null;
            }
            int columnCount = cursor.getColumnCount();
            Map<String, String> row = new HashMap<String, String>();
            for (int i = 0; i < columnCount; i++)
            {
                String name = cursor.getColumnName(i);
                String value = cursor.getString(i);
                row.put(name, value);
            }
            Map<String, String> localMap = row;
            return localMap;
        } finally
        {
            try
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            } finally
            {
                if ((db != null) && (closeDB))
                {
                    db.close();
                }
            }
            readLock.unlock();
        }
    }

    public void execSQL(String sql, boolean closeDB) throws SQLException
    {
        this.execSQL(sql, null, closeDB);
    }

    public void execSQL(String sql, Object[] bindArgs, boolean closeDB) throws SQLException
    {
        try
        {
            writeLock.lock();
            db = getWritableDatabase();
            if (null != bindArgs)
            {
                db.execSQL(sql, bindArgs);
            } else
            {
                db.execSQL(sql);
            }
        } finally
        {
            if ((db != null) && (closeDB))
            {
                db.close();
            }
            writeLock.unlock();
        }
    }

    public void execSQLByTransaction(String sql, boolean closeDB) throws SQLException
    {
        this.execSQLByTransaction(sql, null, closeDB);
    }

    public void execSQLByTransaction(String sql, Object[] bindArgs, boolean closeDB) throws SQLException
    {
        try
        {
            writeLock.lock();
            db = getWritableDatabase();
            db.beginTransaction();
            if (null != bindArgs)
            {
                db.execSQL(sql, bindArgs);
            } else
            {
                db.execSQL(sql);
            }
            db.setTransactionSuccessful();
        } finally
        {
            if ((db != null) && (closeDB))
            {
                db.endTransaction();
                db.close();
            }
            writeLock.unlock();
        }
    }

    public void execSQLListByTransaction(List<String> sqlList, List<Object[]> bindArgsList, boolean closeDB) throws SQLException
    {
        try
        {
            writeLock.lock();
            db = getWritableDatabase();
            db.beginTransaction();
            Object[] bindArgs;
            for (int i = 0, len = sqlList.size(); i < len; i++)
            {
                bindArgs = bindArgsList.get(i);
                if (null != bindArgs)
                {
                    db.execSQL(sqlList.get(i), bindArgs);
                } else
                {
                    db.execSQL(sqlList.get(i));
                }
            }
            db.setTransactionSuccessful();
        } finally
        {
            if ((db != null) && (closeDB))
            {
                db.endTransaction();
                db.close();
            }
            writeLock.unlock();
        }
    }
}
