package com.greatwall.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;

public abstract class WeakAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{

    private List<WeakReference<Object>> mObjReferences          = null;
    private Handler                     mHandler                = new Handler();
    private boolean                     mIsWithoutOnPostExecute = false;

    public WeakAsyncTask(Object... objs)
    {
        this.mObjReferences = new ArrayList<WeakReference<Object>>(objs.length);
        for (Object obj : objs)
        {
            addToWeakReference(obj);
        }
    }

    public final void addToWeakReference(Object obj)
    {
        if (obj == null)
            throw new NullPointerException();
        this.mObjReferences.add(new WeakReference<Object>(obj));
    }

    protected void onPreExecute(Object[] objs)
    {
    }

    protected abstract Result doInBackgroundImpl(Params... paramArrayOfParams) throws Exception;

    protected void onProgressUpdate(Object[] objs, Progress[] values)
    {
    }

    protected void onCancelled(Object[] objs)
    {
    }

    protected void onPostExecute(Object[] objs, Result result)
    {
    }

    protected void onException(Object[] objs, Exception e)
    {
    }

    @Override
    protected final void onPreExecute()
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancel(true);
        else
            onPreExecute(objs);
    }

    @Override
    protected final Result doInBackground(Params... params)
    {
        try
        {
            return doInBackgroundImpl(params);
        } catch (final Exception e)
        {
            this.mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (WeakAsyncTask.this.isCancelled())
                        return;
                    Object[] objs = WeakAsyncTask.this.getObjects();
                    if (objs != null)
                        WeakAsyncTask.this.onException(objs, e);
                }
            });
            this.mIsWithoutOnPostExecute = true;
        }
        return null;
    }

    @Override
    protected final void onProgressUpdate(Progress... values)
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancel(true);
        else
            onProgressUpdate(objs, values);
    }

    @Override
    protected final void onCancelled()
    {
        Object[] objs = getObjects();
        if (objs != null)
            onCancelled(objs);
    }

    @Override
    protected final void onPostExecute(Result result)
    {
        if (this.mIsWithoutOnPostExecute)
            return;
        Object[] objs = getObjects();
        if (objs != null)
            onPostExecute(objs, result);
    }

    private Object[] getObjects()
    {
        Object[] objs = new Object[this.mObjReferences.size()];
        Iterator<WeakReference<Object>> objIterator = this.mObjReferences.iterator();
        for (int i = 0; i < objs.length; i++)
        {
            objs[i] = objIterator.next().get();
            if (objs[i] == null)
                return null;
        }
        return objs;
    }
}
