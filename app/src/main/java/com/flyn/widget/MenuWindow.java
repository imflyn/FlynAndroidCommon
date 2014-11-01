package com.flyn.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class MenuWindow
{
    private PopupWindow pw = null;

    public MenuWindow(Context context)
    {
        this.pw = new PopupWindow(context);
        setWidth(-1);
        setHeight(-2);
        setFocusable(true);
        setAnimationStyle(context.getResources().getIdentifier("MenuWindow", "style", context.getPackageName()));
        setContentView(new LinearLayout(context));
        setBackgroundDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("bitmap_menuwindow_bg", "drawable", context.getPackageName())));
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

    public void show(Window mainWindow, int distanceToBottom)
    {
        this.pw.showAtLocation(mainWindow.getDecorView(), 80, 0, distanceToBottom);
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

    public boolean isShowing()
    {
        return this.pw.isShowing();
    }

    public void dismiss()
    {
        this.pw.dismiss();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener)
    {
        this.pw.setOnDismissListener(onDismissListener);
    }
}