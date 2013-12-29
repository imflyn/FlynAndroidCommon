/* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */
package com.flyn.mvc.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Title ThreadPool
 * @Description ThreadPool是command的线程池
 */
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
