package com.greatwall.app;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;

import com.greatwall.app.manager.AppManager;
import com.greatwall.sharedpreferences.SharedPreferenceFactory;

public class Application extends android.app.Application
{
  
    private static Application    mContext;
    private volatile boolean      mRunning        = false;
    private ArrayList<AppManager> mAppManagerList = new ArrayList<AppManager>();
    private Handler               backgroundHandler;
    private ExecutorService       backgroundExecutor;
      
    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = this;
        init();
    }

    private void init()
    {
        this.backgroundHandler = new Handler();
        this.backgroundExecutor = Executors.newCachedThreadPool(new ThreadFactory()
        {
            private AtomicInteger atomicInteger = new AtomicInteger();

            @Override
            public Thread newThread(Runnable runnable)
            {
                Thread thread = new Thread(runnable, "Background executor service #" + atomicInteger.getAndIncrement());
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });

    }

    public static Application getInstance()
    {
        if (mContext == null)
            throw new IllegalStateException();
        return mContext;
    }

    public boolean isRunning()
    {
        return mRunning;
    }

    public void running()
    {
        this.mRunning = true;
    }

    public void addManager(AppManager appManager)
    {
        this.mAppManagerList.add(appManager);
    }

    public void clear()
    {
        for (int i = 0, len = mAppManagerList.size(); i < len; i++)
        {
            mAppManagerList.get(i).onClear();
        }
        SharedPreferenceFactory.clear();
    }

    public void close()
    {
        mRunning = false;
        clear();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void runInBackground(final Runnable runnable)
    {
        backgroundExecutor.submit(runnable);
    }

    public Handler getHandler()
    {
        return backgroundHandler;
    }

    public void runOnUiThread(final Runnable runnable)
    {
        backgroundHandler.post(runnable);
    }

    public void runOnUiThreadDelay(final Runnable runnable, long delayMillis)
    {
        backgroundHandler.postDelayed(runnable, delayMillis);
    }
}
