package com.greatwall.util;

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
        if (null == view)
            return;

        if (view instanceof WebView)
        {
            WebView webView = (WebView) view;
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.removeAllViews();
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
            iv.setImageDrawable(null);
            iv.setBackgroundResource(-1);
            return;
        }

        view.setBackgroundResource(-1);

    }

    private static void recycleViewGroupAndChildViews(ViewGroup viewGroup, boolean recycleBitmap)
    {
        for (int i = 0, len = viewGroup.getChildCount(); i < len; i++)
        {
            View child = viewGroup.getChildAt(i);
            recycleView(child, recycleBitmap);
        }
        viewGroup.setBackgroundResource(-1);
    }

}
