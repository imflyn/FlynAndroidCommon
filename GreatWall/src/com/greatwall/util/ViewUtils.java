package com.greatwall.util;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

public class ViewUtils
{
    public static void recycleView(View view, boolean recycleBitmap)
    {

        if (view instanceof WebView)
        {
            WebView webView = (WebView) view;
            webView.loadUrl("about:blank");
            webView.stopLoading();
            return;
        }

        if (view instanceof ViewGroup)
        {
            recycleViewGroupAndChildViews((ViewGroup) view, true);
            return;
        }

        if (view instanceof ImageView)
        {
            ImageView iv = (ImageView) view;
            Drawable drawable = iv.getDrawable();
            if (drawable instanceof BitmapDrawable)
            {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (recycleBitmap && bitmap != null)
                {
                    bitmap.recycle();
                }
            }
            iv.setImageBitmap(null);
            iv.setBackgroundDrawable(null);
            return;
        }

        view.setBackgroundDrawable(null);

    }

    public static void recycleViewGroupAndChildViews(ViewGroup viewGroup, boolean recycleBitmap)
    {
        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            View child = viewGroup.getChildAt(i);
            recycleView(child, recycleBitmap);
        }
        viewGroup.setBackgroundDrawable(null);
    }

    public static void recycleViews(WeakHashMap<String, View> viewMap, boolean recycleBitmap)
    {
        for (Map.Entry<String, View> entry : viewMap.entrySet())
        {
            View view = entry.getValue();
            recycleView(view, recycleBitmap);
        }
    }

    public static void recycleViews(List<View> viewList, boolean recycleBitmap)
    {
        for (int i = 0; i < viewList.size(); i++)
        {
            View view = viewList.get(i);
            recycleView(view, recycleBitmap);
        }
    }
}
