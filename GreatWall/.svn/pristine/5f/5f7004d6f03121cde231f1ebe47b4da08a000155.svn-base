package com.greatwall.util.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool
{
    private ExecutorService     mExecutor;
    private volatile boolean    mStarted = false;
    private static ThreadPool   instance;

    private static final Object LOCK     = new Object();

    private ThreadPool()
    {

    }

    public static ThreadPool getInstance()
    {
        synchronized (LOCK)
        {
            if (instance == null)
            {
                synchronized (LOCK)
                {
                    instance = new ThreadPool();
                }
            }
        }

        return instance;
    }

    public void start()
    {
        if (!this.mStarted)
        {
            this.mExecutor = Executors.newCachedThreadPool(new ThreadFactory()
            {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, "CommandTask #" + this.mCount.getAndIncrement());
                }
            });

            this.mStarted = true;
        }
    }

    public void execute(Runnable runnable)
    {
        this.mExecutor.execute(runnable);
    }

    public void shutdown()
    {
        if (this.mStarted)
        {
            this.mExecutor.shutdown();
            this.mExecutor = null;
            this.mStarted = false;
        }
    }
}
