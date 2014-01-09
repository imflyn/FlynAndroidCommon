package com.flyn.ui.adapterView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import com.flyn.util.OptionalExecutorTask;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class AsyncDataManager
{
    private static PushTask           PUSH_TASK = new PushTask();
    private static Executor           EXECUTOR;
    private static AdapterViewWrapper WRAPPER;

    static
    {
        PUSH_TASK.execute(new Object[0]);

        EXECUTOR = new ThreadPoolExecutor(0, 5, 45L, TimeUnit.SECONDS, new SynchronousQueue(), new ThreadPoolExecutor.CallerRunsPolicy());
        WRAPPER = null;
    }

    public static void computeAsyncData(AdapterView<? extends Adapter> view, AsyncDataExecutor executor)
    {
        if ((view == null) || (executor == null))
            throw new NullPointerException();
        Object adapter = null;
        if ((view instanceof ExpandableListView))
        {
            adapter = ((ExpandableListView) view).getExpandableListAdapter();
        } else
        {
            adapter = view.getAdapter();
            if ((adapter instanceof WrapperListAdapter))
                adapter = ((WrapperListAdapter) adapter).getWrappedAdapter();
        }
        if (adapter == null)
            throw new IllegalStateException("please call this method after 'setAdapter(adapter)' is called.");
        int firstPos = view.getFirstVisiblePosition();
        int lastPos = view.getLastVisiblePosition();
        List holders = new ArrayList();
        if ((adapter instanceof GenericAdapter))
        {
            GenericAdapter adapterPoint = (GenericAdapter) adapter;
            if ((view instanceof ListView))
            {
                int headerCount = ((ListView) view).getHeaderViewsCount();
                firstPos -= headerCount;
                lastPos -= headerCount;
            }
            if (firstPos < 0)
                firstPos = 0;
            int count = adapterPoint.getCount();
            if (lastPos >= count)
                lastPos = count - 1;
            for (int i = firstPos; i <= lastPos; i++)
            {
                holders.add(adapterPoint.queryDataHolder(i));
            }
        } else if ((adapter instanceof GenericExpandableListAdapter))
        {
            GenericExpandableListAdapter adapterPoint = (GenericExpandableListAdapter) adapter;
            ExpandableListView expandableView = (ExpandableListView) view;
            for (int i = firstPos; i <= lastPos; i++)
            {
                long packedPos = expandableView.getExpandableListPosition(i);
                int groupPos = ExpandableListView.getPackedPositionGroup(packedPos);
                int childPos = ExpandableListView.getPackedPositionChild(packedPos);
                if (groupPos == -1)
                    continue;
                DataHolder holder = adapterPoint.queryDataHolder(groupPos);
                if (childPos != -1)
                    holder = ((GroupDataHolder) holder).queryChild(childPos);
                holders.add(holder);
            }
        } else
        {
            throw new IllegalStateException("the adapter for AdapterView can only be GenericAdapter or GenericExpandableListAdapter.");
        }
        if (holders.size() == 0)
            return;
        if ((WRAPPER == null) || (WRAPPER.getAdapterView() != view))
            WRAPPER = new AdapterViewWrapper(view);
        else
            WRAPPER.getExecuteRunnable().cancel();
        ExecuteRunnable runnable = new ExecuteRunnable(view, adapter, holders, executor);
        PUSH_TASK.push(runnable);
        WRAPPER.setExecuteRunnable(runnable);
    }

    private static class AdapterViewWrapper
    {
        private WeakReference<AdapterView<? extends Adapter>> viewRef  = null;
        private AsyncDataManager.ExecuteRunnable              runnable = null;

        public AdapterViewWrapper(AdapterView<? extends Adapter> view)
        {
            this.viewRef = new WeakReference(view);
        }

        public AdapterView<? extends Adapter> getAdapterView()
        {
            return this.viewRef.get();
        }

        public void setExecuteRunnable(AsyncDataManager.ExecuteRunnable runnable)
        {
            if (runnable == null)
                throw new NullPointerException();
            this.runnable = runnable;
        }

        public AsyncDataManager.ExecuteRunnable getExecuteRunnable()
        {
            return this.runnable;
        }
    }

    private static class ExecuteRunnable implements Runnable
    {
        private WeakReference<AdapterView<? extends Adapter>> viewRef     = null;
        private WeakReference<Object>                         adapterRef  = null;
        private List<DataHolder>                              holders     = null;
        private AsyncDataExecutor                             executor    = null;
        private boolean                                       isCancelled = false;

        public ExecuteRunnable(AdapterView<? extends Adapter> view, Object adapter, List<DataHolder> holders, AsyncDataExecutor executor)
        {
            this.viewRef = new WeakReference(view);
            this.adapterRef = new WeakReference(adapter);
            this.holders = holders;
            this.executor = executor;
        }

        @Override
        public void run()
        {
            for (DataHolder holder : this.holders)
            {
                if (this.isCancelled)
                    return;
                AdapterView view = this.viewRef.get();
                Object adapter = this.adapterRef.get();
                if ((view == null) || (adapter == null))
                    return;
                if (!holder.mExecuteConfig.mShouldExecute)
                    continue;
                if (holder.mExecuteConfig.mIsExecuting)
                    continue;
                holder.mExecuteConfig.mIsExecuting = true;

                AsyncDataManager.ExecuteTask task = new AsyncDataManager.ExecuteTask(view, adapter, holder, this.executor);

                view = null;
                adapter = null;

                task.executeOnExecutor(AsyncDataManager.EXECUTOR, new Object[0]);
            }
        }

        public void cancel()
        {
            this.isCancelled = true;
        }
    }

    private static class ExecuteTask extends OptionalExecutorTask<Object, Object, Object>
    {
        private WeakReference<AdapterView<? extends Adapter>> viewRef    = null;
        private WeakReference<Object>                         adapterRef = null;
        private DataHolder                                    holder     = null;
        private AsyncDataExecutor                             executor   = null;

        public ExecuteTask(AdapterView<? extends Adapter> view, Object adapter, DataHolder holder, AsyncDataExecutor executor)
        {
            this.viewRef = new WeakReference(view);
            this.adapterRef = new WeakReference(adapter);
            this.holder = holder;
            this.executor = executor;
        }

        @Override
        protected Object doInBackground(Object[] params)
        {
            for (int i = 0; i < this.holder.getAsyncDataCount(); i++)
            {
                Object curAsyncData = this.holder.getAsyncData(i);
                if (curAsyncData != null)
                    continue;
                try
                {
                    Object asyncData = this.executor.onExecute(this.holder.mExecuteConfig.mPosition, this.holder, i);
                    if (asyncData == null)
                        throw new NullPointerException("the method 'AsyncDataExecutor.onExecute' returns null");
                    this.holder.setAsyncData(i, asyncData);

                    publishProgress(new Object[] { asyncData, Integer.valueOf(i) });
                } catch (Exception e)
                {
                    Log.i(AsyncDataManager.class.getName(), "execute async data failed(position:" + this.holder.mExecuteConfig.mPosition + ",index:" + i + ")", e);
                }
            }

            this.holder.mExecuteConfig.mIsExecuting = false;
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values)
        {
            super.onProgressUpdate(values);
            Object adapterObj = this.adapterRef.get();
            if (adapterObj == null)
                return;
            if (this.executor.isNotifyAsyncDataForAll())
            {
                if ((adapterObj instanceof GenericAdapter))
                    ((GenericAdapter) adapterObj).notifyDataSetChanged();
                else if ((adapterObj instanceof GenericExpandableListAdapter))
                    ((GenericExpandableListAdapter) adapterObj).notifyDataSetChanged();
            } else
            {
                AdapterView adapterView = this.viewRef.get();
                if (adapterView == null)
                    return;
                int position = this.holder.mExecuteConfig.mPosition;
                int wholePosition = -1;
                if ((adapterObj instanceof GenericAdapter))
                {
                    GenericAdapter adapter = (GenericAdapter) adapterObj;
                    if (position >= adapter.getCount())
                        return;
                    if (!this.holder.equals(adapter.queryDataHolder(position)))
                        return;
                    wholePosition = position;
                    if ((adapterView instanceof ListView))
                        wholePosition += ((ListView) adapterView).getHeaderViewsCount();
                } else if ((adapterObj instanceof GenericExpandableListAdapter))
                {
                    GenericExpandableListAdapter adapter = (GenericExpandableListAdapter) adapterObj;
                    int groupPos = this.holder.mExecuteConfig.mGroupPosition;
                    long packedPosition = -1L;
                    if (groupPos == -1)
                    {
                        if (position >= adapter.getGroupCount())
                            return;
                        if (!this.holder.equals(adapter.queryDataHolder(position)))
                            return;
                        packedPosition = ExpandableListView.getPackedPositionForGroup(position);
                    } else
                    {
                        if (groupPos >= adapter.getGroupCount())
                            return;
                        GroupDataHolder group = adapter.queryDataHolder(groupPos);
                        if (position >= group.getChildrenCount())
                            return;
                        if (!this.holder.equals(group.queryChild(position)))
                            return;
                        packedPosition = ExpandableListView.getPackedPositionForChild(groupPos, position);
                    }
                    wholePosition = ((ExpandableListView) adapterView).getFlatListPosition(packedPosition);
                }
                int first = adapterView.getFirstVisiblePosition();
                int last = adapterView.getLastVisiblePosition();
                if ((wholePosition >= first) && (wholePosition <= last))
                    this.holder.onAsyncDataExecuted(adapterView.getContext(), position, adapterView.getChildAt(wholePosition - first), values[0], ((Integer) values[1]).intValue());
            }
        }
    }

    private static class PushTask extends OptionalExecutorTask<Object, Object, Object>
    {
        private LinkedList<AsyncDataManager.ExecuteRunnable> runnables = new LinkedList();

        @Override
        protected Object doInBackground(Object[] params)
        {
            while (true)
            {
                AsyncDataManager.ExecuteRunnable curRunnable = null;
                synchronized (this.runnables)
                {
                    if (this.runnables.size() > 0)
                        curRunnable = this.runnables.removeFirst();
                }
                if (curRunnable == null)
                {
                    try
                    {
                        Thread.sleep(100L);
                    } catch (InterruptedException localInterruptedException)
                    {
                    }
                    continue;
                }
                curRunnable.run();
            }
        }

        public void push(AsyncDataManager.ExecuteRunnable runnable)
        {
            synchronized (this.runnables)
            {
                this.runnables.addFirst(runnable);
            }
        }
    }
}
