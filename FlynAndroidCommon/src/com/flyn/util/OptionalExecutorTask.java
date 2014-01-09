package com.flyn.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class OptionalExecutorTask<Params, Progress, Result>
{

    private static final InternalHandler         sHandler         = new InternalHandler();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static volatile Executor             sDefaultExecutor = new ThreadPoolExecutor(0, 2147483647, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue(), new ThreadFactory()
                                                                  {

                                                                      @Override
                                                                      public Thread newThread(Runnable r)
                                                                      {
                                                                          return new Thread(r, "OptionalExecutorTask #" + r.hashCode());
                                                                      }

                                                                  });
    private final WorkerRunnable<Params, Result> mWorker;
    private final FutureTask<Result>             mFuture;
    private volatile Status                      mStatus          = Status.PENDING;

    private final AtomicBoolean                  mTaskInvoked     = new AtomicBoolean();

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
        this.mWorker = new WorkerRunnable()
        {
            @Override
            public Result call() throws Exception
            {
                OptionalExecutorTask.this.mTaskInvoked.set(true);

                android.os.Process.setThreadPriority(10);
                return OptionalExecutorTask.this.postResult(OptionalExecutorTask.this.doInBackground((Params[]) this.mParams));
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

                    OptionalExecutorTask.this.postResultIfNotInvoked(result);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            switch (mStatus.ordinal())
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

    @SuppressWarnings("unchecked")
    private static class InternalHandler extends Handler
    {

        @Override
        public void handleMessage(Message msg)
        {

            @SuppressWarnings("rawtypes")
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