package com.greatwall.ui.interfaces;

import android.util.SparseArray;
import android.view.View;

public class ViewHolder
{
    // I added a generic return type to reduce the casting noise in client code
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id)
    {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null)
        {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null)
        {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

    public static <T extends View> T findViewById(View rootView, int id)
    {
        View view = rootView.findViewById(id);

        return (T) view;
    }
}