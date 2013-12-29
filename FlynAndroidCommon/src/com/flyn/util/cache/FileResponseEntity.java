
package com.flyn.util.cache;

import java.io.Serializable;

import android.os.AsyncTask;

public class FileResponseEntity  implements Serializable
{
    private static final long  serialVersionUID = 5525015855679979479L;
    private AsyncTask<?, ?, ?> task;
    private Object             object;

    public AsyncTask<?, ?, ?> getTask()
    {
        return task;
    }

    public void setTask(AsyncTask<?, ?, ?> task)
    {
        this.task = task;
    }

    public Object getObject()
    {
        return object;
    }

    public void setObject(Object object)
    {
        this.object = object;
    }

}
