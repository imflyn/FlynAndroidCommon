
package com.flyn.util.cache;

import java.lang.ref.WeakReference;

import com.flyn.util.cache.FileCacheWork.BufferWorkerTask;

@SuppressWarnings("rawtypes")
public class AsyncEntity
{
    private final WeakReference<BufferWorkerTask> bufferWorkerTaskReference;

    public AsyncEntity(BufferWorkerTask inpputWorkerTask)
    {
        bufferWorkerTaskReference = new WeakReference<BufferWorkerTask>(inpputWorkerTask);
    }

    public BufferWorkerTask getBufferWorkerTask()
    {
        return bufferWorkerTaskReference.get();
    }
}