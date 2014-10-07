package com.flyn.service;

import android.app.Application;
import android.os.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * 应用全局数据类
 */
public class AppContext extends Application
{
    private static AppContext appContext = null;

    /**
     * Where data load was requested.
     */
    private boolean serviceStarted;
    /**
     * Future for loading process.
     */
    private Future<Void> loadFuture;
    /**
     * Thread to execute tasks in background..
     */
    private ExecutorService backgroundExecutor;
    /**
     * Handler to execute runnable in UI thread.
     */
    private Handler handler;

    private boolean isRunning = false;

    /**
     * 获得单一实例
     */
    public static AppContext getInstance()
    {
        return appContext;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        doOncreate();
        setRunning(true);
    }

    private void doOncreate()
    {
        appContext = this;
        backgroundExecutor = Executors.newSingleThreadExecutor(new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable runnable)
            {
                Thread thread = new Thread(runnable, "Background executor service");
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        });

    }

    /**
     * Starts data loading in background if not started yet.
     *
     * @return
     */
    public void onServiceStarted()
    {
        if (serviceStarted)
        {
            return;
        }
        serviceStarted = true;
        loadFuture = backgroundExecutor.submit(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                try
                {
                } finally
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // Throw exceptions in UI thread if any.
                            try
                            {
                                loadFuture.get();
                            } catch (InterruptedException e)
                            {
                                throw new RuntimeException(e);
                            } catch (ExecutionException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
                return null;
            }
        });
    }

    /**
     * Service have been destroyed.
     */
    public void onServiceDestroy()
    {
        runInBackground(new Runnable()
        {
            @Override
            public void run()
            {

            }
        });
    }

    /**
     * Submits request to be executed in background.
     *
     * @param runnable
     */
    public void runInBackground(final Runnable runnable)
    {
        backgroundExecutor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    runnable.run();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Submits request to be executed in UI thread.
     *
     * @param runnable
     */
    public void runOnUiThread(final Runnable runnable)
    {
        handler.post(runnable);
    }

    /**
     * Submits request to be executed in UI thread.
     *
     * @param runnable
     * @param delayMillis
     */
    public void runOnUiThreadDelay(final Runnable runnable, long delayMillis)
    {
        handler.postDelayed(runnable, delayMillis);
    }

    public final boolean isRunning()
    {
        return isRunning;
    }

    public final void setRunning(boolean isRunning)
    {
        this.isRunning = isRunning;
    }

}
