package com.greatwall.util.command;

import java.io.Serializable;

public class Request implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Object            tag;
    private Object            data;

    public Request()
    {
    }

    public Request(Object tag, Object data)
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
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }


}
