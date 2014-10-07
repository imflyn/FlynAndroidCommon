package com.flyn.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ForegroundService extends Service
{
    private static final String TAG = "ForegroundService";
    private static final Class<?>[] mSetForegroundSignature = new Class[]{boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{boolean.class};
    private final static boolean flag = true;
    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    @Override
    public void onCreate()
    {
        mNM = NotificationManager.getInstance();
        try
        {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e)
        {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }

        if (mStartForeground == null || mStopForeground == null)
        {
            try
            {
                mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
            } catch (NoSuchMethodException e)
            {
                throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
            }
        }
        changeForeground();
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        AppContext.getInstance().onServiceStarted();

    }

    @Override
    public void onDestroy()
    {
        // Make sure our notification is gone.
        stopForegroundCompat(NotificationManager.PERSISTENT_NOTIFICATION_ID);
        AppContext.getInstance().onServiceDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void changeForeground()
    {
        if (flag && AppContext.getInstance().isRunning())
        {
            startForegroundCompat(NotificationManager.PERSISTENT_NOTIFICATION_ID, NotificationManager.getInstance().getPersistentNotification());
        } else
        {
            stopForegroundCompat(NotificationManager.PERSISTENT_NOTIFICATION_ID);
        }
    }

    private void invokeMethod(Method method, Object[] args)
    {
        try
        {
            method.invoke(this, args);
        } catch (InvocationTargetException e)
        {
            // Should not happen.
            Log.w(TAG, "Unable to invoke method", e);
        } catch (IllegalAccessException e)
        {
            // Should not happen.
            Log.w(TAG, "Unable to invoke method", e);
        }
    }

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    private void startForegroundCompat(int id, Notification notification)
    {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null)
        {
            Log.i(TAG, "startForegroundCompat");
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    private void stopForegroundCompat(int id)
    {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null)
        {
            Log.i(TAG, "stopForegroundCompat");
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }

        // Fall back on the old API. Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

}
