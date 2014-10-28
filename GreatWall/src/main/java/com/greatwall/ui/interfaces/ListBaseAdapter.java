package com.greatwall.ui.interfaces;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class ListBaseAdapter<T> extends android.widget.BaseAdapter
{
    protected LayoutInflater mInflater;
    protected List<T> data = new ArrayList<T>();
    protected ListView mListView;
    protected Activity mContext;


    public ListBaseAdapter(ListView mListView)
    {
        this.mListView = mListView;
        this.mContext = (Activity) mListView.getContext();
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
        notifyDataSetChanged();
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
    
    public void removeAll() {
		this.data.clear();
		notifyDataSetChanged();
	}

    public synchronized void swap(int index1, int index2)
    {
        Collections.swap(this.data, index1, index2);
        notifyDataSetChanged();
    }

    public SparseArray<View> getItemSparseArray(int position)
    {

        int wantedPosition = position - mListView.getHeaderViewsCount();
        int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= mListView.getChildCount())
        {
            return new SparseArray<View>();
        }

        View wantedView = mListView.getChildAt(wantedChild);
        @SuppressWarnings("unchecked") SparseArray<View> sparseArray = (SparseArray<View>) wantedView.getTag();

        return sparseArray;
    }

    public View getItemView(int position)
    {

        int wantedPosition = position - mListView.getHeaderViewsCount();
        int firstPosition = mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        View wantedView = mListView.getChildAt(wantedChild);

        return wantedView;
    }

    public View getItemViewById(int position, int id)
    {
        View view = getItemView(position);

        return view == null ? null : view.findViewById(id);
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
