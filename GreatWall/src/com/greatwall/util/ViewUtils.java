package com.greatwall.util;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

public class ViewUtils
{
    public static void recycleViewGroupAndChildViews(List<ViewGroup> viewGroupList, boolean recycleBitmap)
    {
        for (ViewGroup viewGroup : viewGroupList)
        {
            recycleViewGroupAndChildViews(viewGroup, recycleBitmap);
        }
    }

    public static void recycleViewGroupAndChildViews(ViewGroup viewGroup, boolean recycleBitmap)
    {
        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {

            View child = viewGroup.getChildAt(i);

            if (child instanceof WebView)
            {
                WebView webView = (WebView) child;
                webView.loadUrl("about:blank");
                webView.stopLoading();
                continue;
            }

            if (child instanceof ViewGroup)
            {
                recycleViewGroupAndChildViews((ViewGroup) child, true);
                continue;
            }

            if (child instanceof ImageView)
            {
                ImageView iv = (ImageView) child;
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
                continue;
            }

            child.setBackgroundDrawable(null);

        }

        viewGroup.setBackgroundDrawable(null);
    }
}
