package com.flyn.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.flyn.util.Logger;

public class ToastWindow
{
    private PopupWindow pw   = null;
    private TextView    text = null;

    public ToastWindow(Context context)
    {
        this.pw = new PopupWindow(context);
        setWidth(-2);
        setHeight(-2);
        setFocusable(false);
        this.text = new TextView(context);
        this.text.setGravity(17);
        setContentView(this.text);
        setBackgroundDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("bitmap_generic_toast_bg", "drawable", context.getPackageName())));
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

    public void setText(int textResId)
    {
        this.text.setText(textResId);
    }

    public void setText(CharSequence textStr)
    {
        this.text.setText(textStr);
    }

    public void show(Window mainWindow, int distanceToBottom)
    {
        this.pw.showAtLocation(mainWindow.getDecorView(), 80, 0, distanceToBottom);
    }

    public void showForMillis(Window mainWindow, int distanceToBottom, int millis)
    {
        show(mainWindow, distanceToBottom);
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                ToastWindow.this.dismiss();
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
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                ToastWindow.this.dismiss();
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
            Logger.logE(ToastWindow.class, "dismiss ToastWindow failed.", e);
        }
    }
}