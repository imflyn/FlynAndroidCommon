package com.greatwall.util.command;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public class Response implements Serializable
{

    private static final long serialVersionUID = 1L;
    private Object tag;
    private Object data;

    public Response()
    {

    }

    public Response(Object data)
    {
        this.data = data;
    }

    public Response(Object tag, Object data)
    {
        this.tag = tag;
        this.data = data;
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
        this.data = new WeakReference<Object>(data);
    }

}
