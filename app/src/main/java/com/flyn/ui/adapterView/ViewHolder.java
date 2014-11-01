package com.flyn.ui.adapterView;

import android.view.View;

public class ViewHolder
{
    protected View[] mParams = null;
    protected Object mTag = null;

    public ViewHolder()
    {
    }

    public ViewHolder(View[] params)
    {
        this.mParams = params;
    }

    public View[] getParams()
    {
        return this.mParams;
    }

    public void setParams(View[] params)
    {
        this.mParams = params;
    }

    public Object getTag()
    {
        return this.mTag;
    }

    public void setTag(Object tag)
    {
        this.mTag = tag;
    }
}