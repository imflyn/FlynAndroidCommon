package com.greatwall.util;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;

public class ViewUtils
{
    @SuppressWarnings("deprecation")
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
            view.setBackgroundDrawable(null);
            return;
        }

        view.setBackgroundDrawable(null);

    }

    @SuppressWarnings("deprecation")
    private static void recycleViewGroupAndChildViews(ViewGroup viewGroup, boolean recycleBitmap)
    {
        for (int i = 0, len = viewGroup.getChildCount(); i < len; i++)
        {
            View child = viewGroup.getChildAt(i);
            recycleView(child, recycleBitmap);
        }
        viewGroup.setBackgroundDrawable(null);
    }

    public static void fixInputMethodManagerLeak(Context destContext)
    {
        if (destContext == null)
        {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null)
        {
            return;
        }

        String[] arr = new String[] { "mCurRootView", "mServedView", "mNextServedView" };
        Field f = null;
        Object obj_get = null;
        for (int i = 0; i < arr.length; i++)
        {
            String param = arr[i];
            try
            {
                f = imm.getClass().getDeclaredField(param);
                if (f.isAccessible() == false)
                {
                    f.setAccessible(true);
                } 
                obj_get = f.get(imm);
                if (obj_get != null && obj_get instanceof View)
                {
                    View v_get = (View) obj_get;
                    if (v_get.getContext() == destContext)
                    { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                    } else
                    {
                        // 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                        break;
                    }
                }
            } catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }
}
