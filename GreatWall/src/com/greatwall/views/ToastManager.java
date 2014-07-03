package com.greatwall.views;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.widget.Toast;

import com.greatwall.app.Application;

public class ToastManager
{
    protected Timer             timerForEver = new Timer();
    protected TimerTask         taskForEver  = null;
    protected Toast             lastToast    = null;
    private Context             mContext;

    private static ToastManager toastManager;

    public static ToastManager getInstance()
    {
        if (null == toastManager)
        {
            synchronized (ToastManager.class)
            {
                if (null == toastManager)
                {
                    toastManager = new ToastManager();

                }
            }
        }
        return toastManager;
    }

    private ToastManager()
    {
        mContext = Application.getInstance();
    }

    public Toast show(Toast toast)
    {
        cancelToast();
        lastToast = toast;
        toast.show();
        return toast;
    }

    public Toast showShort(String text)
    {
        cancelToast();
        lastToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        lastToast.show();
        return lastToast;
    }

    public Toast showShort(int textResId)
    {
        return showShort(mContext.getString(textResId));
    }

    public Toast showLong(String text)
    {
        cancelToast();
        lastToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
        lastToast.show();
        return lastToast;
    }

    public Toast showLong(int textResId)
    {
        return showLong(mContext.getString(textResId));
    }

    public void cancelToast()
    {
        if (null != lastToast)
        {
            lastToast.cancel();
            lastToast = null;
        }
    }

    public void showForEver(String text)
    {
        if (taskForEver != null)
        {
            lastToast.setText(text);
            return;
        }
        lastToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        taskForEver = new TimerTask()
        {
            public void run()
            {
                lastToast.show();
            }
        };
        timerForEver.schedule(taskForEver, new Date(), 2000L);
    }

    public void showForEver(int textResId)
    {
        showForEver(mContext.getString(textResId));
    }

    public void updateForEver(String text)
    {
        if (lastToast != null)
            lastToast.setText(text);
    }

    public void updateForEver(int textResId)
    {
        updateForEver(mContext.getString(textResId));
    }

    public void interruptForEver()
    {
        if (taskForEver != null)
        {
            taskForEver.cancel();
            timerForEver.purge();
            taskForEver = null;
        }
    }
}