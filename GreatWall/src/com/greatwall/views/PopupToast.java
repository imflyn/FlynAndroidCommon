package com.greatwall.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.PopupWindow;

import com.greatwall.app.Application;
import com.greatwall.util.LogManager;

public class PopupToast
{
    protected Context         mContext;
    private final PopupWindow pw;

    public PopupToast(Context context)
    {
        this.pw = new PopupWindow(context);
        this.mContext = context;
    }

    public void setSoftInputMode(int mode)
    {
        this.pw.setSoftInputMode(mode);
    }

    public void setWidth(int width)
    {
        this.pw.setWidth(width);
    }

    public void setHeight(int height)
    {
        this.pw.setHeight(height);
    }

    public void setContentView(View contentView)
    {
        this.pw.setContentView(contentView);
    }

    public void setBackgroundDrawable(Drawable background)
    {
        this.pw.setBackgroundDrawable(background);
    }

    public void setContentView(int resId)
    {
        View view = LayoutInflater.from(mContext).inflate(resId, null);
        setContentView(view);
    }

    public void show(Window mainWindow, int distanceToTop)
    {
        this.pw.showAsDropDown(mainWindow.getDecorView(), 0, distanceToTop);
    }

    public void showForMillis(Window mainWindow, int distanceToBottom, int millis)
    {
        show(mainWindow, distanceToBottom);
        Application.getInstance().getHandler().postDelayed(new Runnable()
        {
            public void run()
            {
                PopupToast.this.dismiss();
            }
        }, millis);
    }

    public void showCenter(Window mainWindow)
    {
        this.pw.showAtLocation(mainWindow.getDecorView(), 17, 0, 0);
    }

    public void showCenterForMillis(Window mainWindow, int millis)
    {
        showCenter(mainWindow);
        Application.getInstance().getHandler().postDelayed(new Runnable()
        {
            public void run()
            {
                PopupToast.this.dismiss();
            }
        }, millis);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff)
    {
        this.pw.showAsDropDown(anchor, xoff, yoff);
    }

    public void setFocusable(boolean focusable)
    {
        this.pw.setFocusable(focusable);
    }

    public void setAnimationStyle(int animationStyle)
    {
        this.pw.setAnimationStyle(animationStyle);
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener)
    {
        this.pw.setOnDismissListener(listener);
    }

    public void dismiss()
    {
        try
        {
            this.pw.dismiss();
        } catch (RuntimeException e)
        {
            LogManager.e(PopupToast.class, "dismiss  failed.", e);
        }
    }
}
