package com.talkingoa.android.app.ui.adapterview;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.talkingoa.android.app.support.AsyncWeakTask;

public abstract class BaseLazyLoadAdapter extends BaseLoadAdapter
{
    private int              mPage               = 0;

    private int              mPages              = -1;

    private boolean          mIsLoadedAllNoPages = false;

    private LazyLoadCallback mCallback           = null;

    public BaseLazyLoadAdapter(Context context, LazyLoadCallback callback)
    {
        this(context, callback, 1);
    }

    public BaseLazyLoadAdapter(Context context, LazyLoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        this.mCallback = callback;
    }

    @Override
    public LazyLoadCallback getLoadCallback()
    {
        return this.mCallback;
    }

    public void bindLazyLoading(AdapterView<? extends Adapter> adapterView, int remainingCount)
    {
        if ((adapterView instanceof AbsListView))
        {
            try
            {
                AbsListView absList = (AbsListView) adapterView;
                Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
                field.setAccessible(true);
                AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener) field.get(absList);
                if ((onScrollListener != null) && ((onScrollListener instanceof WrappedOnScrollListener)))
                {
                    absList.setOnScrollListener(new WrappedOnScrollListener(((WrappedOnScrollListener) onScrollListener).getOriginalListener(), remainingCount));
                    return;
                }
                absList.setOnScrollListener(new WrappedOnScrollListener(onScrollListener, remainingCount));
            } catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        } else
            throw new UnsupportedOperationException("Only supports lazy loading for the AdapterView which is AbsListView.");
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean load()
    {
        if (this.mIsLoading)
            return false;
        this.mIsLoading = true;
        final int start = getRealCount();
        final int page = this.mPage;
        new AsyncWeakTask(new Object[] { this })
        {
            @Override
            protected void onPreExecute(Object[] objs)
            {
                BaseLazyLoadAdapter adapter = (BaseLazyLoadAdapter) objs[0];
                adapter.onBeginLoad(adapter.mContext, BaseLazyLoadAdapter.this.mParam);
            }

            @Override
            protected Object doInBackgroundImpl(Object[] params) throws Exception
            {
                return BaseLazyLoadAdapter.this.mCallback.onLoad(BaseLazyLoadAdapter.this.mParam, start, page + 1);
            }

            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                BaseLazyLoadAdapter adapter = (BaseLazyLoadAdapter) objs[0];
                adapter.mPage += 1;
                List resultList = (List) result;
                if ((resultList != null) && (resultList.size() >= 0))
                    adapter.addDataHolders(resultList);
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                if (adapter.mPages == -1)
                {
                    if ((resultList == null) || (resultList.size() == 0))
                        adapter.mIsLoadedAllNoPages = true;
                    else
                        adapter.mIsLoadedAllNoPages = false;
                }
                adapter.mIsException = false;
                adapter.onAfterLoad(adapter.mContext, BaseLazyLoadAdapter.this.mParam, null);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                Log.i(BaseLazyLoadAdapter.class.getName(), "Execute lazy loading failed.", e);
                BaseLazyLoadAdapter adapter = (BaseLazyLoadAdapter) objs[0];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                adapter.onAfterLoad(adapter.mContext, BaseLazyLoadAdapter.this.mParam, e);
            }
        }.execute(new Object[] { "" });
        return true;
    }

    public int getPage()
    {
        return this.mPage;
    }

    public void setPages(int pages)
    {
        if (pages < 0)
            throw new IllegalArgumentException("pages could not be less than zero.");
        this.mPages = pages;
    }

    public int getPages()
    {
        return this.mPages;
    }

    public boolean isLoadedAll()
    {
        if (this.mPages == -1)
        {
            return this.mIsLoadedAllNoPages;
        }

        return this.mPage >= this.mPages;
    }

    @Override
    public void clearDataHolders()
    {
        super.clearDataHolders();
        this.mPage = 0;
        this.mIsLoadedAllNoPages = false;
    }

    public static abstract class LazyLoadCallback extends BaseLoadAdapter.LoadCallback
    {
        @Override
        protected final List<DataHolder> onLoad(Object param) throws Exception
        {
            throw new UnsupportedOperationException("Unsupported,use onLoad(param,start,page) instead");
        }

        protected abstract List<DataHolder> onLoad(Object paramObject, int paramInt1, int paramInt2) throws Exception;
    }

    private class WrappedOnScrollListener implements AbsListView.OnScrollListener
    {
        private AbsListView.OnScrollListener mOriginalListener = null;
        private int                          mRemainingCount   = 0;

        public WrappedOnScrollListener(AbsListView.OnScrollListener originalListener, int remainingCount)
        {
            if ((originalListener != null) && ((originalListener instanceof WrappedOnScrollListener)))
                throw new IllegalArgumentException("the OnScrollListener could not be WrappedOnScrollListener");
            this.mOriginalListener = originalListener;
            this.mRemainingCount = remainingCount;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            if (this.mOriginalListener != null)
                this.mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            if (view.getVisibility() == 8)
            {
                return;
            }
            if (visibleItemCount == 0)
                return;
            if ((firstVisibleItem + visibleItemCount + this.mRemainingCount >= totalItemCount) && (!BaseLazyLoadAdapter.this.isLoadedAll()) && (!BaseLazyLoadAdapter.this.isException()))
            {
                BaseLazyLoadAdapter.this.load();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            if (this.mOriginalListener != null)
                this.mOriginalListener.onScrollStateChanged(view, scrollState);
        }

        public AbsListView.OnScrollListener getOriginalListener()
        {
            return this.mOriginalListener;
        }
    }
}