package com.flyn.ui.adapterView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;

public class GenericExpandableListAdapter extends BaseExpandableListAdapter
{
    Context                       mContext  = null;
    private List<GroupDataHolder> mHolders  = null;

    private AsyncDataExecutor     mExecutor = null;
    private Handler               mHandler  = new Handler();
    private ViewGroup             mRunView  = null;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GenericExpandableListAdapter(Context context)
    {
        if (context == null)
            throw new NullPointerException();
        this.mContext = context;
        this.mHolders = new ArrayList();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GenericExpandableListAdapter(Context context, List<GroupDataHolder> holders)
    {
        if ((context == null) || (holders == null))
            throw new NullPointerException();
        this.mContext = context;
        this.mHolders = new ArrayList(holders);
    }

    public void bindAsyncDataExecutor(AsyncDataExecutor executor)
    {
        this.mExecutor = executor;
    }

    public void addDataHolder(GroupDataHolder holder)
    {
        this.mHolders.add(holder);
        notifyDataSetChanged();
    }

    public void addDataHolder(int location, GroupDataHolder holder)
    {
        this.mHolders.add(location, holder);
        notifyDataSetChanged();
    }

    public void addDataHolders(List<GroupDataHolder> holders)
    {
        this.mHolders.addAll(holders);
        notifyDataSetChanged();
    }

    public void addDataHolders(int location, List<GroupDataHolder> holders)
    {
        this.mHolders.addAll(location, holders);
        notifyDataSetChanged();
    }

    public void removeDataHolder(int location)
    {
        this.mHolders.remove(location);
        notifyDataSetChanged();
    }

    public void removeDataHolder(GroupDataHolder holder)
    {
        this.mHolders.remove(holder);
        notifyDataSetChanged();
    }

    public GroupDataHolder queryDataHolder(int location)
    {
        return this.mHolders.get(location);
    }

    public int queryDataHolder(GroupDataHolder holder)
    {
        return this.mHolders.indexOf(holder);
    }

    public void clearDataHolders()
    {
        this.mHolders.clear();
        notifyDataSetChanged();
    }

    @Override
    public final int getGroupCount()
    {
        return this.mHolders.size();
    }

    @Override
    public final int getChildrenCount(int i)
    {
        return queryDataHolder(i).getChildrenCount();
    }

    @Override
    public final Object getGroup(int i)
    {
        return queryDataHolder(i);
    }

    @Override
    public final Object getChild(int i, int i2)
    {
        return queryDataHolder(i).queryChild(i2);
    }

    @Override
    public final long getGroupId(int i)
    {
        return i;
    }

    @Override
    public final long getChildId(int i, int i2)
    {
        return i2;
    }

    @Override
    public final boolean hasStableIds()
    {
        return true;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final View getGroupView(int i, boolean b, View view, final ViewGroup viewGroup)
    {
        GroupDataHolder holder = queryDataHolder(i);

        holder.setExpanded(b);
        holder.mExecuteConfig.mShouldExecute = false;
        holder.mExecuteConfig.mGroupPosition = -1;
        holder.mExecuteConfig.mPosition = i;
        View returnVal;
        if (view == null)
        {
            returnVal = holder.onCreateView(this.mContext, i, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(this.mContext, i, view, holder.getData());
        }
        if (this.mExecutor != null)
        {
            if (this.mRunView != viewGroup)
            {
                this.mRunView = viewGroup;
                final AsyncDataExecutor curExecutor = this.mExecutor;
                this.mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        AsyncDataManager.computeAsyncData((AdapterView) viewGroup, curExecutor);
                        if (GenericExpandableListAdapter.this.mRunView == viewGroup)
                            GenericExpandableListAdapter.this.mRunView = null;
                    }
                }, 1000L);
            }
        }
        return returnVal;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final View getChildView(int i, int i2, boolean b, View view, final ViewGroup viewGroup)
    {
        DataHolder holder = queryDataHolder(i).queryChild(i2);

        holder.mExecuteConfig.mShouldExecute = false;
        holder.mExecuteConfig.mGroupPosition = i;
        holder.mExecuteConfig.mPosition = i2;
        View returnVal;
        if (view == null)
        {
            returnVal = holder.onCreateView(this.mContext, i2, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(this.mContext, i2, view, holder.getData());
        }
        if (this.mExecutor != null)
        {
            if (this.mRunView != viewGroup)
            {
                this.mRunView = viewGroup;
                final AsyncDataExecutor curExecutor = this.mExecutor;
                this.mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        AsyncDataManager.computeAsyncData((AdapterView) viewGroup, curExecutor);
                        if (GenericExpandableListAdapter.this.mRunView == viewGroup)
                            GenericExpandableListAdapter.this.mRunView = null;
                    }
                }, 1000L);
            }
        }
        return returnVal;
    }

    @Override
    public final boolean isChildSelectable(int i, int i2)
    {
        return true;
    }
}