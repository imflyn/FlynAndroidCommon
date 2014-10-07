package com.greatwall.util.command;

import java.lang.ref.WeakReference;

public class Request
{
    private Object tag;
    private Object data;
    private boolean mIsCancel;

    public Request()
    {

    }

    public Request(Object data)
    {
        this.data = new WeakReference<Object>(data);
    }

    public Request(Object tag, Object data)
    {
        this.tag = tag;
        this.data = new WeakReference<Object>(data);
    }

    public Object getTag()
    {
        return tag;
    }

    public void setTag(Object tag)
    {
        this.tag = tag;
    }

    public Object getData()
    {
        return this.data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public void cancel()
    {
        this.mIsCancel = true;
    }

    public final boolean isCanceled()
    {
        return mIsCancel;
    }

}
