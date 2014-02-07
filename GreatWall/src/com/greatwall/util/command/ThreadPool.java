package com.greatwall.util.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool
{
    private ExecutorService   executor;
    private volatile boolean  started = false;
    private static ThreadPool instance;

    private ThreadPool()
    {

    }

    public static ThreadPool getInstance()
    {
        if (instance == null)
        {
            instance = new ThreadPool();

        }
        return instance;
    }

    public void start()
    {
        if (!started)
        {
            executor = Executors.newCachedThreadPool(new ThreadFactory()
            {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r)
                {
                    return new Thread(r, "CommondTask #" + mCount.getAndIncrement());
                }
            });

            started = true;
        }
    }

    public void execute(Runnable runnable)
    {
        executor.execute(runnable);
    }

    public void shutdown()
    {
        if (started)
        {
            executor.shutdown();
            executor = null;
            started = false;
        }
    }
}
