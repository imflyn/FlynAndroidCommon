package com.greatwall.ui.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ListBaseAdapter<T> extends android.widget.BaseAdapter
{
    protected LayoutInflater mInflater;
    protected ArrayList<T>   data = new ArrayList<T>();
    protected ViewGroup      viewGroup;

    public ListBaseAdapter(ViewGroup viewGroup)
    {
        this.viewGroup = viewGroup;
        this.mInflater = LayoutInflater.from(viewGroup.getContext());
    }

    public synchronized void setData(List<T> data)
    {
        this.data.clear();
        this.data.addAll(data);
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

    public synchronized void swap(int index1, int index2)
    {
        Collections.swap(this.data, index1, index2);
        notifyDataSetChanged();
    }

    public View updateView(int index)
    {
        View view = viewGroup.getChildAt(index);

        return view;
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
