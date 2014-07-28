package com.greatwall.ui.interfaces;

import java.util.ArrayList;
import java.util.List;

public abstract class ListBaseAdapter<T> extends android.widget.BaseAdapter
{
    private List<T> data = new ArrayList<T>();

    public synchronized void setData(List<T> data)
    {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
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
