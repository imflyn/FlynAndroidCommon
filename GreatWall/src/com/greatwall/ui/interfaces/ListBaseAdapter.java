package com.greatwall.ui.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public abstract class ListBaseAdapter<T> extends android.widget.BaseAdapter
{
    protected LayoutInflater mInflater;
    protected ArrayList<T>   data = new ArrayList<T>();
    protected ListView       mListView;
    protected Context        mContext;

    public ListBaseAdapter(ListView mListView)
    {
        this.mListView = mListView;
        this.mContext = mListView.getContext();
        this.mInflater = LayoutInflater.from(mContext);
    }

    public synchronized void setData(List<T> data)
    {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public synchronized void setItem(int location, T item)
    {
        this.data.set(location, item);
        // TODO 跟新单个item
    }

    public synchronized void addItem(T item)
    {
        this.data.add(item);
        notifyDataSetChanged();
    }

    public synchronized void addItem(int location, T item)
    {
        this.data.add(location, item);
        notifyDataSetChanged();
    }

    public synchronized void addAll(List<T> items)
    {
        this.data.addAll(items);
        notifyDataSetChanged();
    }

    public synchronized void addAll(int location, List<T> items)
    {
        this.data.addAll(location, items);
        notifyDataSetChanged();
    }

    public synchronized void removeItem(T item)
    {
        this.data.remove(item);
        notifyDataSetChanged();
    }

    public synchronized void removeItem(int location)
    {
        this.data.remove(location);
        notifyDataSetChanged();
    }

    public synchronized void removeAll(List<T> items)
    {
        this.data.remove(items);
        notifyDataSetChanged();
    }

    public synchronized void swap(int index1, int index2)
    {
        Collections.swap(this.data, index1, index2);
        notifyDataSetChanged();
    }

    public SparseArray<View> getItemView(int position)
    {

        int wantedPosition = position - mListView.getHeaderViewsCount();
        int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= mListView.getChildCount())
        {
            return null;
        }

        View wantedView = mListView.getChildAt(wantedChild);
        @SuppressWarnings("unchecked")
        SparseArray<View> sparseArray = (SparseArray<View>) wantedView.getTag();

        return sparseArray;
    }

    public View getItemViewById(int position, int id)
    {
        int wantedPosition = position - mListView.getHeaderViewsCount();
        int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= mListView.getChildCount())
        {
            return null;
        }

        View wantedView = mListView.getChildAt(wantedChild);
        @SuppressWarnings("unchecked")
        SparseArray<View> sparseArray = (SparseArray<View>) wantedView.getTag();

        return sparseArray.get(id);
    }

    @Override
    public int getCount()
    {
        return data == null ? 0 : data.size();
    }

    @Override
    public T getItem(int position)
    {
        return data == null ? null : data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

}
