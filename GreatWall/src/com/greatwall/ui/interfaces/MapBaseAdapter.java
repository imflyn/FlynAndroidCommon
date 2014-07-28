package com.greatwall.ui.interfaces;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;

public abstract class MapBaseAdapter<Key, T> extends android.widget.BaseExpandableListAdapter
{
    private LinkedHashMap<Key, List<T>> data    = new LinkedHashMap<Key, List<T>>();
    private ViewGroup                   viewGroup;

    public MapBaseAdapter(ViewGroup viewGroup)
    {
        this.viewGroup = viewGroup;
    }

    public synchronized void setData(Map<Key, List<T>> data)
    {
        this.data.clear();
        this.data.putAll(data);

        notifyDataSetChanged();
    }

    public synchronized void addItem(Key key, T item)
    {
        List<T> list = this.data.get(key);
        if (null != list)
        {
            list.add(item);
            notifyDataSetChanged();
        }
    }

    public synchronized void addItem(Key key, int location, T item)
    {
        List<T> list = this.data.get(key);
        if (null != list)
        {
            list.add(location, item);
            notifyDataSetChanged();
        }
    }

    public synchronized void addItems(Key key, List<T> items)
    {
        List<T> list = this.data.get(key);
        if (null != list)
        {
            list.addAll(items);
            notifyDataSetChanged();
        }
    }

    public synchronized void addItems(Key key, int location, List<T> items)
    {
        List<T> list = this.data.get(key);
        if (null != list)
        {
            list.addAll(location, items);
            notifyDataSetChanged();
        }
    }

    public synchronized void put(Key key, List<T> items)
    {
        this.data.put(key, items);
        notifyDataSetChanged();
    }

    public synchronized void removeItem(Key key, T item)
    {
        List<T> list = this.data.get(key);
        if (null != list)
        {
            list.remove(item);
            notifyDataSetChanged();
        }
    }

    public synchronized void removeItem(Key key, int location)
    {
        List<T> list = this.data.get(key);
        if (null != list)
        {
            list.remove(location);
            notifyDataSetChanged();
        }
    }

    public synchronized void removeAll(Key key)
    {
        Object obj = this.data.get(key);
        if (null != obj)
        {
            this.data.remove(obj);
            notifyDataSetChanged();
        }
    }

    public View updateView(int index)
    {
        View view = viewGroup.getChildAt(index);

        return view;
    }

    @Override
    public int getGroupCount()
    {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        if (data == null)
            return 0;

        @SuppressWarnings("unchecked")
        Key[] key = (Key[]) data.keySet().toArray();
        if (key == null || key.length <= 0 || key[groupPosition] == null)
            return 0;
        else
        {
            List<T> list = data.get(key[groupPosition]);
            if (list == null)
            {
                return 0;
            } else
            {
                return list.size();
            }
        }
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        if (data == null)
            return null;

        @SuppressWarnings("unchecked")
        Key[] key = (Key[]) data.keySet().toArray();
        if (key == null || key.length <= 0)
            return null;
        else
        {
            return key[groupPosition];
        }

    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        if (data == null)
            return null;

        @SuppressWarnings("unchecked")
        Key[] key = (Key[]) data.keySet().toArray();
        if (key == null || key.length <= 0 || key[groupPosition] == null)
            return null;
        else
        {
            List<T> list = data.get(key[groupPosition]);
            if (list == null)
            {
                return null;
            } else
            {
                return list.get(childPosition);
            }
        }
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }

}
