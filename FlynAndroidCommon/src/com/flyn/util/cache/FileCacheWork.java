package com.flyn.util.cache;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.os.AsyncTask;

/**
 * @Description 缓存的工作类
 */
@SuppressWarnings("rawtypes")
public class FileCacheWork<ResponseObject extends Object>
{
    private FileCache                            mTAFileCache;
    private boolean                              mExitTasksEarly         = false;
    protected boolean                            mPauseWork              = false;
    private final Object                         mPauseWorkLock          = new Object();
    protected static final int                   MESSAGE_CLEAR           = 0;
    protected static final int                   MESSAGE_INIT_DISK_CACHE = 1;
    protected static final int                   MESSAGE_FLUSH           = 2;
    protected static final int                   MESSAGE_CLOSE           = 3;
    private HashMap<String, CacheEntity>         mCacheEntityHashMap     = new HashMap<String, CacheEntity>();
    private CacheCallBackHandler<ResponseObject> mCallBackHandler;
    private ProcessDataHandler                   mProcessDataHandler;

    /**
     * 从缓存加载数据
     * 
     * @param data
     *            缓存的标识
     * @param responseObject
     *            对缓存结果响应的类
     */
    @SuppressWarnings("unchecked")
    public void loadFormCache(Object data, ResponseObject responseObject)
    {
        CacheEntity cacheEntity;
        String string = String.valueOf(responseObject);
        if (!mCacheEntityHashMap.containsKey(string))
        {
            cacheEntity = new CacheEntity();
            cacheEntity.setT(responseObject);
            mCacheEntityHashMap.put(string, cacheEntity);
        } else
        {
            cacheEntity = mCacheEntityHashMap.get(string);
        }
        if (data == null)
        {
            ;
        }
        byte[] buffer = null;

        if (mTAFileCache != null)
        {
            buffer = mTAFileCache.getBufferFromMemCache(String.valueOf(data));
        }
        if (buffer != null)
        {
            // 如果返回不为空
            if (mCallBackHandler != null)
            {
                mCallBackHandler.onSuccess(responseObject, data, buffer);

            }

        } else if (cancelPotentialWork(data, cacheEntity))
        {
            final BufferWorkerTask task = new BufferWorkerTask(cacheEntity);
            final AsyncEntity asyncEntity = new AsyncEntity(task);
            if (mCallBackHandler != null)
            {
                mCallBackHandler.onStart(responseObject, data);
            }
            cacheEntity.setAsyncEntity(asyncEntity);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
        }
    }

    /**
     * 设置文件缓存
     * 
     * @param fileCache
     */
    public void setFileCache(FileCache fileCache)
    {
        this.mTAFileCache = fileCache;
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * 获取缓存的回调对象
     * 
     * @return 如果没有设置，返回为null
     */
    public CacheCallBackHandler<ResponseObject> getCallBackHandler()
    {
        return mCallBackHandler;
    }

    public void setCallBackHandler(CacheCallBackHandler<ResponseObject> callBackHandler)
    {
        this.mCallBackHandler = callBackHandler;
    }

    public void setProcessDataHandler(ProcessDataHandler processDataHandler)
    {
        this.mProcessDataHandler = processDataHandler;
    }

    public ProcessDataHandler getProcessDataHandler()
    {
        return mProcessDataHandler;
    }

    /**
     * 是否退出以前的任务，如果为设置为true则退出以前的Task
     * 
     * @param exitTasksEarly
     */
    public void setExitTasksEarly(boolean exitTasksEarly)
    {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * 取消任何挂起的连接到提供给object工作。
     * 
     * @param object
     */
    @SuppressWarnings("unchecked")
    public void cancelWork(ResponseObject responseObject)
    {

        CacheEntity cacheEntity;
        String string = String.valueOf(responseObject);
        if (!mCacheEntityHashMap.containsKey(string))
        {
            cacheEntity = new CacheEntity();
            cacheEntity.setT(responseObject);
            mCacheEntityHashMap.put(string, cacheEntity);
        } else
        {
            cacheEntity = mCacheEntityHashMap.get(string);
        }
        final BufferWorkerTask bufferWorkerTask = getBufferWorkerTask(cacheEntity);
        if (bufferWorkerTask != null)
        {
            bufferWorkerTask.cancel(true);
        }
    }

    public boolean cancelPotentialWork(Object data, CacheEntity cacheEntity)
    {
        final BufferWorkerTask responseWorkerTask = getBufferWorkerTask(cacheEntity);

        if (responseWorkerTask != null)
        {
            final Object bitmapData = responseWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data))
            {
                responseWorkerTask.cancel(true);
            } else
            {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private BufferWorkerTask getBufferWorkerTask(CacheEntity cacheEntity)
    {
        if (cacheEntity != null)
        {
            final AsyncEntity asyncEntity = cacheEntity.getAsyncEntity();
            if (asyncEntity != null)
            {
                return (asyncEntity.getBufferWorkerTask());
            }
        }
        return null;
    }

    public void setPauseWork(boolean pauseWork)
    {
        synchronized (mPauseWorkLock)
        {
            mPauseWork = pauseWork;
            if (!mPauseWork)
            {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected void initDiskCacheInternal()
    {
        if (mTAFileCache != null)
        {
            mTAFileCache.initDiskCache();
        }
    }

    protected void clearCacheInternal()
    {
        if (mTAFileCache != null)
        {
            mTAFileCache.clearCache();
        }
    }

    protected void flushCacheInternal()
    {
        if (mTAFileCache != null)
        {
            mTAFileCache.flush();
        }
    }

    protected void closeCacheInternal()
    {
        if (mTAFileCache != null)
        {
            mTAFileCache.close();
            mTAFileCache = null;
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void>
    {
        public CacheAsyncTask()
        {
        }

        @Override
        protected Void doInBackground(Object... params)
        {
            switch ((Integer) params[0])
            {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    /**
     * 位图的惟一标识符来存储清除内存和磁盘缓存都TAFileCache与此相关 对象。注意,这包括磁盘访问,所以这是不应当的 上执行的主要/ UI线程。
     */
    public void clearCache()
    {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    /**
     * 磁盘缓存初始化TAFileCache与此相关的对象。注意, 这包括磁盘访问,所以这应该不会被执行的主要/ UI 线程。
     */
    public void initCache()
    {
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * 磁盘缓存刷新TAFileCache与此相关的对象。注意, 这包括磁盘访问,所以这应该不会被执行的主要/ UI 线程。
     */
    public void flushCache()
    {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    /**
     * 关闭磁盘缓存与此相关TAFileCache对象。注意,这包括磁盘访问,所以这应该不会被执行的主要/ UI线程。
     */
    public void closeCache()
    {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    public class BufferWorkerTask extends AsyncTask<Object, Void, byte[]>
    {
        private Object                           data;
        private final WeakReference<CacheEntity> cacheEntityReference;

        public BufferWorkerTask(CacheEntity cacheEntity)
        {
            this.cacheEntityReference = new WeakReference<CacheEntity>(cacheEntity);
        }

        /**
         * Background processing.
         */
        @Override
        protected byte[] doInBackground(Object... params)
        {
            /*
             * if (BuildConfig.DEBUG) { Log.d(TAG,
             * "doInBackground - starting work"); }
             */
            data = params[0];
            final String dataString = String.valueOf(data);
            byte[] buffer = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock)
            {
                while (mPauseWork && !isCancelled())
                {
                    try
                    {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e)
                    {
                    }
                }
            }
            if (mTAFileCache != null && !isCancelled() && getAttachedCacheEntity() != null && !mExitTasksEarly)
            {
                buffer = mTAFileCache.getBufferFromDiskCache(dataString);
            }
            if (buffer == null && !isCancelled() && getAttachedCacheEntity() != null && !mExitTasksEarly)
            {
                if (mProcessDataHandler != null)
                {
                    buffer = mProcessDataHandler.processData(params[0]);
                }
            }
            if (buffer != null && mTAFileCache != null)
            {
                mTAFileCache.addBufferToCache(dataString, buffer);
            }
            return buffer;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(byte[] buffer)
        {
            // if cancel was called on this task or the "exit early" flag is set
            // then we're done
            if (isCancelled() || mExitTasksEarly)
            {
                buffer = null;
            }
            final CacheEntity cacheEntity = getAttachedCacheEntity();
            if (mCallBackHandler != null && cacheEntity != null)
            {
                mCallBackHandler.onSuccess((ResponseObject) cacheEntity.getT(), data, buffer);
            }

        }

        private CacheEntity getAttachedCacheEntity()
        {
            final CacheEntity cacheEntity = cacheEntityReference.get();
            final BufferWorkerTask bufferWorkerTask = getBufferWorkerTask(cacheEntity);

            if (this == bufferWorkerTask)
            {
                return cacheEntity;
            }

            return null;
        }

        @Override
        protected void onCancelled(byte[] inputStream)
        {
            super.onCancelled(inputStream);
            synchronized (mPauseWorkLock)
            {
                mPauseWorkLock.notifyAll();
            }
        }
    }
}
