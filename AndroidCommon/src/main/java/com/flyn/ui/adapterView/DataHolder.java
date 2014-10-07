package com.flyn.ui.adapterView;

import android.content.Context;
import android.view.View;

import java.lang.ref.SoftReference;

public abstract class DataHolder
{
    ExecuteConfig mExecuteConfig = new ExecuteConfig();
    private Object mData = null;
    private SoftReference<?>[] mAsyncData = null;

    public DataHolder(Object data, int asyncDataCount)
    {
        this.mData = data;
        this.mAsyncData = new SoftReference[asyncDataCount];
    }

    public abstract View onCreateView(Context paramContext, int paramInt, Object paramObject);

    public abstract void onUpdateView(Context paramContext, int paramInt, View paramView, Object paramObject);

    public abstract void onAsyncDataExecuted(Context paramContext, int paramInt1, View paramView, Object paramObject, int paramInt2);

    public int getType()
    {
        return 0;
    }

    public Object getData()
    {
        return this.mData;
    }

    public Object getAsyncData(int index)
    {
        Object asyncData = this.mAsyncData[index] == null ? null : this.mAsyncData[index].get();
        if (asyncData == null)
        {
            this.mExecuteConfig.mShouldExecute = true;
        }
        return asyncData;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void setAsyncData(int index, Object asyncData)
    {
        this.mAsyncData[index] = new SoftReference(asyncData);
    }

    public int getAsyncDataCount()
    {
        return this.mAsyncData.length;
    }

    class ExecuteConfig
    {
        boolean mShouldExecute = false;
        boolean mIsExecuting = false;
        int mGroupPosition = -1;
        int mPosition = -1;

        ExecuteConfig()
        {
        }
    }
}