package com.flyn.service;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;

/** Manage notifications about message, subscription and authentication. */
public class NotificationManager
{

    public static final int                       PERSISTENT_NOTIFICATION_ID    = 1;

    private static final long                     VIBRATION_DURATION            = 500;
    private final long                            startTime;
    private Application                           application;
    private final android.app.NotificationManager notificationManager;
    private final Notification                    persistentNotification;
    private final PendingIntent                   clearNotifications;
    private final Handler                         handler;

    /** Runnable to start vibration. */
    private final Runnable                        startVibro;

    /** Runnable to force stop vibration. */
    private final Runnable                        stopVibro;

    /** List of providers for notifications. */

    /** List of */

    private final static NotificationManager      instance;

    static
    {
        instance = new NotificationManager();
    }

    public static NotificationManager getInstance()
    {
        return instance;
    }

    private NotificationManager()
    {
        this.application = AppContext.getInstance();
        notificationManager = (android.app.NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
        persistentNotification = new Notification();
        handler = new Handler();
        startTime = System.currentTimeMillis();
        clearNotifications = PendingIntent.getActivity(application, 0, ClearNotificationsActivity.createIntent(application), 0);
        stopVibro = new Runnable()
        {
            @Override
            public void run()
            {
                handler.removeCallbacks(startVibro);
                handler.removeCallbacks(stopVibro);
                ((Vibrator) NotificationManager.this.application.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
            }
        };
        startVibro = new Runnable()
        {
            @Override
            public void run()
            {
                handler.removeCallbacks(startVibro);
                handler.removeCallbacks(stopVibro);
                ((Vibrator) NotificationManager.this.application.getSystemService(Context.VIBRATOR_SERVICE)).cancel();
                ((Vibrator) NotificationManager.this.application.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION);
                handler.postDelayed(stopVibro, VIBRATION_DURATION);
            }
        };
    }

    public void notify(int id, Notification notification)
    {
        try
        {
            notificationManager.notify(id, notification);
        } catch (SecurityException e)
        {
        }

    }

    public void cancel(int id)
    {
        notificationManager.cancel(id);
    }

    public void canlceAll()
    {
        notificationManager.cancelAll();
    }

    public Notification getPersistentNotification()
    {
        return persistentNotification;
    }

}
