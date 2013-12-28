package com.flyn.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class GenericSQLiteOpenHelper extends SQLiteOpenHelper
{
    public GenericSQLiteOpenHelper(Context context, String dbName, int version)
    {
        super(context, dbName, null, version);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Map<String, String>> rawQuery(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
    {
        List returnVal = new LinkedList();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            int columnCount = cursor.getColumnCount();
            while (true)
            {
                if (!cursor.moveToNext())
                {
                    List localList1 = returnVal;
                    return localList1;
                }
                Map row = new HashMap();
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
                    cursor.close();
            } finally
            {
                if ((db != null) && (closeDB))
                    db.close();
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, String> rawQueryForFirstRow(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            if (!cursor.moveToNext())
                return null;
            int columnCount = cursor.getColumnCount();
            Map row = new HashMap();
            for (int i = 0; i < columnCount; i++)
            {
                String name = cursor.getColumnName(i);
                String value = cursor.getString(i);
                row.put(name, value);
            }
            Map localMap1 = row;
            return localMap1;
        } finally
        {
            try
            {
                if (cursor != null)
                    cursor.close();
            } finally
            {
                if ((db != null) && (closeDB))
                    db.close();
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<String> rawQueryForFirstField(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
    {
        List returnVal = new LinkedList();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            while (true)
            {
                if (!cursor.moveToNext())
                {
                    List localList1 = returnVal;
                    return localList1;
                }
                String value = cursor.getString(0);
                returnVal.add(value);
            }
        } finally
        {
            try
            {
                if (cursor != null)
                    cursor.close();
            } finally
            {
                if ((db != null) && (closeDB))
                    db.close();
            }
        }
    }

    public void execSQL(String sql, boolean closeDB) throws SQLException
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.execSQL(sql);
        } finally
        {
            if ((db != null) && (closeDB))
                db.close();
        }
    }

    public void execSQL(String sql, Object[] bindArgs, boolean closeDB) throws SQLException
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.execSQL(sql, bindArgs);
        } finally
        {
            if ((db != null) && (closeDB))
                db.close();
        }
    }
    
    public void execSQLByTransaction(String sql, boolean closeDB) throws SQLException
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.beginTransaction();
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }
        finally
        {
            
            if ((db != null) && (closeDB))
            {
                db.endTransaction();
                db.close();
            }
        }
    }
    
    public void execSQLByTransaction(String sql, Object[] bindArgs, boolean closeDB) throws SQLException
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.beginTransaction();
            db.execSQL(sql, bindArgs);
            db.setTransactionSuccessful();
        }
        finally
        {
            
            if ((db != null) && (closeDB))
            {
                db.endTransaction();
                db.close();
            }
        }
    }
}
