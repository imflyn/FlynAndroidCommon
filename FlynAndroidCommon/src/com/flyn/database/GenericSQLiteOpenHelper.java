package com.flyn.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class GenericSQLiteOpenHelper extends SQLiteOpenHelper
{
	private SQLiteDatabase db;
	private final Lock lock;

	public GenericSQLiteOpenHelper(Context context, String dbName, int version)
	{
		super(context, dbName, null, version);
		lock = new ReentrantLock();
	}

	public List<Map<String, String>> rawQuery(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
	{
		List<Map<String, String>> returnVal = new LinkedList<Map<String, String>>();
		Cursor cursor = null;
		try
		{
			lock.lock();
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
					cursor.close();
			} finally
			{
				if ((db != null) && (closeDB))
					db.close();
			}
			lock.unlock();
		}
	}

	public Map<String, String> rawQueryForFirstRow(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
	{
		Cursor cursor = null;
		try
		{
			lock.lock();
			db = getReadableDatabase();
			cursor = db.rawQuery(sql, selectionArgs);
			if (!cursor.moveToNext())
				return null;
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
					cursor.close();
			} finally
			{
				if ((db != null) && (closeDB))
					db.close();
			}
			lock.unlock();
		}
	}

	public List<String> rawQueryForFirstField(String sql, String[] selectionArgs, boolean closeDB) throws SQLException
	{
		List<String> returnVal = new LinkedList<String>();
		Cursor cursor = null;
		try
		{
			lock.lock();
			db = getReadableDatabase();
			cursor = db.rawQuery(sql, selectionArgs);
			while (true)
			{
				if (!cursor.moveToNext())
				{
					List<String> localList = returnVal;
					return localList;
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
			lock.unlock();
		}
	}

	public void execSQL(String sql, boolean closeDB) throws SQLException
	{
		try
		{
			lock.lock();
			db = getWritableDatabase();
			db.execSQL(sql);
		} finally
		{
			if ((db != null) && (closeDB))
				db.close();
			lock.unlock();
		}
	}

	public void execSQL(String sql, Object[] bindArgs, boolean closeDB) throws SQLException
	{
		try
		{
			lock.lock();
			db = getWritableDatabase();
			db.execSQL(sql, bindArgs);
		} finally
		{
			if ((db != null) && (closeDB))
				db.close();
			lock.unlock();
		}
	}

	public void execSQLByTransaction(String sql, boolean closeDB) throws SQLException
	{
		try
		{
			lock.lock();
			db = getWritableDatabase();
			db.beginTransaction();
			db.execSQL(sql);
			db.setTransactionSuccessful();
		} finally
		{

			if ((db != null) && (closeDB))
			{
				db.endTransaction();
				db.close();
			}
			lock.unlock();
		}
	}

	public void execSQLByTransaction(String sql, Object[] bindArgs, boolean closeDB) throws SQLException
	{
		try
		{
			lock.lock();
			db = getWritableDatabase();
			db.beginTransaction();
			db.execSQL(sql, bindArgs);
			db.setTransactionSuccessful();
		} finally
		{

			if ((db != null) && (closeDB))
			{
				db.endTransaction();
				db.close();
			}
			lock.unlock();
		}
	}

	public void execSQLListByTransaction(List<String> sqlList, List<Object[]> bindArgsList, boolean closeDB) throws SQLException
	{

		try
		{
			lock.lock();
			db = getWritableDatabase();
			db.beginTransaction();
			for (int i = 0, len = sqlList.size(); i < len; i++)
			{
				db.execSQL(sqlList.get(i), bindArgsList.get(i));
			}
			db.setTransactionSuccessful();
		} finally
		{
			if ((db != null) && (closeDB))
			{
				db.endTransaction();
				db.close();
			}
			lock.unlock();
		}
	}
}
