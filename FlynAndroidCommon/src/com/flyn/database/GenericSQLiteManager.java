package com.flyn.database;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

public class GenericSQLiteManager
{

    private static ExecutorService      multiTaskExecutor = new ThreadPoolExecutor(1, 5, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory()
                                                          {
                                                              private AtomicInteger atomicInteger = new AtomicInteger();

                                                              @Override
                                                              public Thread newThread(Runnable runnable)
                                                              {
                                                                  return new Thread(runnable, "SQLiteTask #" + atomicInteger.get());
                                                              }
                                                          });

    private static GenericSQLiteManager genericSQLiteManager;

    protected static GenericSQLiteManager getInstance()
    {
        if (null == genericSQLiteManager)
        {
            synchronized (GenericSQLiteManager.class)
            {
                if (null == genericSQLiteManager)
                {
                    genericSQLiteManager = new GenericSQLiteManager();
                }
            }
        }
        return genericSQLiteManager;
    }

    public GenericSQLiteOpenHelper getGenericSQLiteOpenHelper(Class<? extends GenericSQLiteOpenHelper> clazz)
    {
        GenericSQLiteOpenHelper genericSQLiteOpenHelper;
        try
        {
            Constructor<? extends GenericSQLiteOpenHelper> constructor = clazz.getConstructor(Context.class);
            genericSQLiteOpenHelper = constructor.newInstance();
        } catch (Throwable e)
        {
            throw new IllegalArgumentException("can not instantiate class:" + clazz, e);
        }
        return genericSQLiteOpenHelper;
    }

    private Future<?> execute(Callable<?> callable)
    {
        return multiTaskExecutor.submit(callable);
    }

    private void execute(Runnable runnable)
    {
        multiTaskExecutor.execute(runnable);
    }

    @SuppressWarnings("unchecked")
    public void query(final Class<? extends GenericSQLiteOpenHelper> cls, final String sql, final String[] sqlArgus, final GenericSQLiteListener genericSQLiteListener)
    {
        Callable<List<Map<String, String>>> callable = new Callable<List<Map<String, String>>>()
        {
            @Override
            public List<Map<String, String>> call() throws Exception
            {
                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                try
                {
                    GenericSQLiteOpenHelper genericSQLiteOpenHelper = getGenericSQLiteOpenHelper(cls);
                    result.addAll(genericSQLiteOpenHelper.rawQuery(sql, sqlArgus, true));
                } catch (Exception e)
                {
                    e.printStackTrace();
                    genericSQLiteListener.onFailure(e);
                }
                return result;
            }
        };
        genericSQLiteListener.onQuerySuccess((List<Map<String, String>>) execute(callable));

    }

    public void query(Class<? extends GenericSQLiteOpenHelper> cls, String sql, GenericSQLiteListener genericSQLiteListener)
    {
        query(cls, sql, null, genericSQLiteListener);
    }

    public void executeSQL(final Class<? extends GenericSQLiteOpenHelper> cls, final String sql, final Object[] sqlArgus, final GenericSQLiteListener genericSQLiteListener)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GenericSQLiteOpenHelper genericSQLiteOpenHelper = getGenericSQLiteOpenHelper(cls);
                    genericSQLiteOpenHelper.execSQL(sql, sqlArgus, true);
                    genericSQLiteListener.onExecuteSuccess();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    genericSQLiteListener.onFailure(e);
                }
            }
        };
        execute(runnable);
    }

    public void executeSQLByTransaction(Class<? extends GenericSQLiteOpenHelper> cls, String sql, GenericSQLiteListener genericSQLiteListener)
    {
        executeSQL(cls, sql, null, genericSQLiteListener);
    }

    public void executeSQLByTransaction(final Class<? extends GenericSQLiteOpenHelper> cls, final String sql, final Object[] sqlArgus, final GenericSQLiteListener genericSQLiteListener)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    GenericSQLiteOpenHelper genericSQLiteOpenHelper = getGenericSQLiteOpenHelper(cls);
                    genericSQLiteOpenHelper.execSQLByTransaction(sql, sqlArgus, true);
                    genericSQLiteListener.onExecuteSuccess();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    genericSQLiteListener.onFailure(e);
                }
            }
        };
        execute(runnable);

    }

    public void executeSQL(Class<? extends GenericSQLiteOpenHelper> cls, String sql, GenericSQLiteListener genericSQLiteListener)
    {
        executeSQL(cls, sql, null, genericSQLiteListener);
    }

    abstract class GenericSQLiteListener
    {
        public abstract void onExecuteSuccess();

        public abstract void onQuerySuccess(List<Map<String, String>> object);

        public abstract void onFailure(Exception e);
    }
}
