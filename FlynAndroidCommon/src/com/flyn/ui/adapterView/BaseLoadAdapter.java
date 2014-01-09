package com.flyn.ui.adapterView;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.flyn.util.WeakAsyncTask;

public abstract class BaseLoadAdapter extends GenericAdapter
{
    boolean              mIsLoading   = false;

    boolean              mIsLoaded    = false;

    boolean              mIsException = false;

    Object               mParam       = null;

    private LoadCallback mCallback    = null;

    public BaseLoadAdapter(Context context, int viewTypeCount)
    {
        super(context, viewTypeCount);
    }

    public BaseLoadAdapter(Context context, LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public BaseLoadAdapter(Context context, LoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        this.mCallback = callback;
    }

    public LoadCallback getLoadCallback()
    {
        return this.mCallback;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean load()
    {
        if (this.mIsLoading)
            return false;
        this.mIsLoading = true;
        new WeakAsyncTask(new Object[] { this })
        {
            @Override
            protected void onPreExecute(Object[] objs)
            {
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                adapter.onBeginLoad(adapter.mContext, BaseLoadAdapter.this.mParam);
            }

            @Override
            protected Object doInBackgroundImpl(Object[] params) throws Exception
            {
                return BaseLoadAdapter.this.mCallback.onLoad(BaseLoadAdapter.this.mParam);
            }

            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                List resultList = (List) result;
                if ((resultList != null) && (resultList.size() > 0))
                    adapter.addDataHolders(resultList);
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                adapter.mIsException = false;
                adapter.onAfterLoad(adapter.mContext, BaseLoadAdapter.this.mParam, null);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                Log.i(BaseLoadAdapter.class.getName(), "Execute loading failed.", e);
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                adapter.onAfterLoad(adapter.mContext, BaseLoadAdapter.this.mParam, e);
            }
        }.execute(new Object[] { "" });
        return true;
    }

    public void setParam(Object param)
    {
        if (param == null)
            throw new NullPointerException();
        this.mParam = param;
    }

    public boolean isLoading()
    {
        return this.mIsLoading;
    }

    public boolean isLoaded()
    {
        return this.mIsLoaded;
    }

    public boolean isException()
    {
        return this.mIsException;
    }

    protected abstract void onBeginLoad(Context paramContext, Object paramObject);

    protected abstract void onAfterLoad(Context paramContext, Object paramObject, Exception paramException);

    public static abstract class LoadCallback
    {
        private Object extra = null;

        public void setExtra(Object extra)
        {
            if (extra == null)
                throw new NullPointerException();
            this.extra = extra;
        }

        public Object getExtra()
        {
            return this.extra;
        }

        protected abstract List<DataHolder> onLoad(Object paramObject) throws Exception;
    }
}
