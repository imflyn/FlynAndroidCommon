package com.talkingoa.android.app.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class OptionalExecutorTask<Params, Progress, Result>
{
    private static final int                     CORE_POOL_SIZE       = 5;
    private static final int                     MAXIMUM_POOL_SIZE    = 128;
    private static final int                     KEEP_ALIVE           = 1;
    private static final ThreadFactory           sThreadFactory       = new ThreadFactory()
                                                                      {
                                                                          private final AtomicInteger mCount = new AtomicInteger(1);

                                                                          @Override
                                                                        public Thread newThread(Runnable r)
                                                                          {
                                                                              return new Thread(r, "AsyncTask #" + this.mCount.getAndIncrement());
                                                                          }
                                                                      };

    private static final BlockingQueue<Runnable> sPoolWorkQueue       = new LinkedBlockingQueue<Runnable>(10);

    public static final Executor                 THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    // private static final int MESSAGE_POST_RESULT = 1;
    // private static final int MESSAGE_POST_PROGRESS = 2;
    private static final InternalHandler         sHandler             = new InternalHandler();

    // private static volatile Executor sDefaultExecutor = new
    // ThreadPoolExecutor(0, 2147483647, 0L, TimeUnit.MILLISECONDS, new
    // SynchronousQueue());
    private static volatile Executor             sDefaultExecutor     = THREAD_POOL_EXECUTOR;
    private final WorkerRunnable<Params, Result> mWorker;
    private final FutureTask<Result>             mFuture;
    private volatile Status                      mStatus              = Status.PENDING;

    private final AtomicBoolean                  mTaskInvoked         = new AtomicBoolean();

    public static void init()
    {
        sHandler.getLooper();
    }

    public static void setDefaultExecutor(Executor exec)
    {
        sDefaultExecutor = exec;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OptionalExecutorTask()
    {
        this.mWorker = new WorkerRunnable<Params, Result>()
        {
            @Override
            public Result call() throws Exception
            {
                OptionalExecutorTask.this.mTaskInvoked.set(true);

                android.os.Process.setThreadPriority(10);
                return postResult(doInBackground(mParams));
            }
        };
        this.mFuture = new FutureTask(this.mWorker)
        {
            @Override
            protected void done()
            {
                try
                {
                    Result result = (Result) get();

                    postResultIfNotInvoked(result);
                } catch (InterruptedException e)
                {
                    Log.w("AsyncTask", e);
                } catch (ExecutionException e)
                {
                    throw new RuntimeException("An error occured while executing doInBackground()", e.getCause());
                } catch (CancellationException localCancellationException)
                {
                    OptionalExecutorTask.this.postResultIfNotInvoked(null);
                } catch (Throwable t)
                {
                    throw new RuntimeException("An error occured while executing doInBackground()", t);
                }
            }
        };
    }

    private void postResultIfNotInvoked(Result result)
    {
        boolean wasTaskInvoked = this.mTaskInvoked.get();
        if (!wasTaskInvoked)
            postResult(result);
    }

    private Result postResult(Result result)
    {
        Message message = sHandler.obtainMessage(1, new AsyncTaskResult(this, new Object[] { result }));
        message.sendToTarget();
        return result;
    }

    public final Status getStatus()
    {
        return this.mStatus;
    }

    protected abstract Result doInBackground(Params[] paramArrayOfParams);

    protected void onPreExecute()
    {
    }

    protected void onPostExecute(Result result)
    {
    }

    protected void onProgressUpdate(Progress[] values)
    {
    }

    protected void onCancelled(Result result)
    {
        onCancelled();
    }

    protected void onCancelled()
    {
    }

    public final boolean isCancelled()
    {
        return this.mFuture.isCancelled();
    }

    public final boolean cancel(boolean mayInterruptIfRunning)
    {
        return this.mFuture.cancel(mayInterruptIfRunning);
    }

    public final Result get() throws InterruptedException, ExecutionException
    {
        return this.mFuture.get();
    }

    public final Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return this.mFuture.get(timeout, unit);
    }

    public final OptionalExecutorTask<Params, Progress, Result> execute(Params[] params)
    {
        return executeOnExecutor(sDefaultExecutor, params);
    }

    public final OptionalExecutorTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params[] params)
    {
        if (this.mStatus != Status.PENDING)
        {
            switch (this.mStatus.ordinal())
            {
                case 2:
                    throw new IllegalStateException("Cannot execute task: the task is already running.");
                case 3:
                    throw new IllegalStateException("Cannot execute task: the task has already been executed (a task can be executed only once)");
            }

        }

        this.mStatus = Status.RUNNING;

        onPreExecute();

        this.mWorker.mParams = params;
        exec.execute(this.mFuture);

        return this;
    }

    public static void execute(Runnable runnable)
    {
        sDefaultExecutor.execute(runnable);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected final void publishProgress(Progress[] values)
    {
        if (!isCancelled())
            sHandler.obtainMessage(2, new AsyncTaskResult(this, values)).sendToTarget();
    }

    private void finish(Result result)
    {
        if (isCancelled())
            onCancelled(result);
        else
        {
            onPostExecute(result);
        }
        this.mStatus = Status.FINISHED;
    }

    private static class AsyncTaskResult<Data>
    {
        @SuppressWarnings("rawtypes")
        final OptionalExecutorTask mTask;
        final Data[]               mData;

        @SuppressWarnings("rawtypes")
        AsyncTaskResult(OptionalExecutorTask task, Data[] data)
        {
            this.mTask = task;
            this.mData = data;
        }
    }

    private static class InternalHandler extends Handler
    {
        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public void handleMessage(Message msg)
        {
            OptionalExecutorTask.AsyncTaskResult result = (OptionalExecutorTask.AsyncTaskResult) msg.obj;
            switch (msg.what)
            {
                case 1:
                    result.mTask.finish(result.mData[0]);
                    break;
                case 2:
                    result.mTask.onProgressUpdate(result.mData);
            }
        }
    }

    public static enum Status
    {
        PENDING,

        RUNNING,

        FINISHED;
    }

    private static abstract class WorkerRunnable<Params, Result> implements Callable<Result>
    {
        Params[] mParams;
    }
}
