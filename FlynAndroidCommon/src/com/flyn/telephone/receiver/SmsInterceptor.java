package com.flyn.telephone.receiver;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;

import com.flyn.telephone.SmsFilter;
import com.flyn.util.Logger;

public abstract class SmsInterceptor
{
    private Context           context                              = null;
    private BroadcastReceiver receiver                             = null;
    private boolean           autoUnregisterWhenIntercept          = false;
    private boolean           isDoneForAutoUnregisterWhenIntercept = false;
    private Handler           handler                              = new Handler();
    private boolean           isUnregistered                       = true;
    private boolean           isUnregisteredCompletely             = true;
    private int               curTimeout                           = -1;

    public SmsInterceptor(Context ctx, SmsFilter interceptFilter)
    {
        if (ctx == null)
            throw new NullPointerException();
        this.context = ctx;
        if (interceptFilter == null)
            interceptFilter = new SmsFilter()
            {
                public boolean accept(SmsMessage msg)
                {
                    return true;
                }
            };
        final SmsFilter interceptFilterPoint = interceptFilter;
        this.receiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, final Intent intent)
            {
                if (SmsInterceptor.this.isUnregistered)
                    return;
                Bundle bundle = intent.getExtras();
                Object[] messages = (Object[]) bundle.get("pdus");
                final SmsMessage[] smsMessages = new SmsMessage[messages.length];
                boolean isIntercept = false;
                for (int i = 0; i < messages.length; i++)
                {
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
                    if ((isIntercept) || (!interceptFilterPoint.accept(smsMessages[i])))
                        continue;
                    isIntercept = true;
                    abortBroadcast();
                }

                if (isIntercept)
                {
                    SmsInterceptor.this.handler.post(new Runnable()
                    {
                        public void run()
                        {
                            SmsInterceptor.this.dealInterceptDelay(intent, smsMessages);
                        }
                    });
                }
            }
        };
    }

    private void dealInterceptDelay(Intent smsIntent, SmsMessage[] smsMessages)
    {
        if (this.isUnregistered)
        {
            Logger.logI(SmsInterceptor.class, "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
            this.context.sendBroadcast(smsIntent);
            return;
        }
        if (this.autoUnregisterWhenIntercept)
        {
            this.isDoneForAutoUnregisterWhenIntercept = true;
            if (!unregisterMe())
            {
                Logger.logI(SmsInterceptor.class, "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
                this.context.sendBroadcast(smsIntent);
                return;
            }
        }
        onIntercept(smsMessages);
    }

    public void onIntercept(SmsMessage[] msg)
    {
    }

    public void onTimeout()
    {
    }

    public void setAutoUnregisterWhenIntercept(boolean auto)
    {
        if (!this.isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterWhenIntercept = auto;
    }

    public void registerMe(int priority, int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!this.isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.setPriority(priority);
        smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.isDoneForAutoUnregisterWhenIntercept = false;
        this.isUnregistered = false;
        this.isUnregisteredCompletely = false;
        this.context.registerReceiver(this.receiver, smsIntentFilter, null, this.handler);
        this.curTimeout = timeout;
        if (this.curTimeout > 0)
        {
            new Timer().schedule(new TimerTask()
            {
                protected long timeCount = 0L;

                public void run()
                {
                    this.timeCount += 100L;
                    if (SmsInterceptor.this.isDoneForAutoUnregisterWhenIntercept)
                    {
                        cancel();
                        SmsInterceptor.this.handler.post(new Runnable()
                        {
                            public void run()
                            {
                                SmsInterceptor.this.isUnregisteredCompletely = true;
                            }
                        });
                    } else if (this.timeCount >= SmsInterceptor.this.curTimeout)
                    {
                        cancel();
                        SmsInterceptor.this.handler.post(new Runnable()
                        {
                            public void run()
                            {
                                if (SmsInterceptor.this.unregisterMe())
                                {
                                    SmsInterceptor.this.onTimeout();
                                }
                                SmsInterceptor.this.isUnregisteredCompletely = true;
                            }
                        });
                    }
                }
            }, 100L, 100L);
        }
    }

    public boolean unregisterMe()
    {
        if (this.curTimeout > 0)
            this.isDoneForAutoUnregisterWhenIntercept = true;
        else
            this.isUnregisteredCompletely = true;
        this.isUnregistered = true;
        try
        {
            this.context.unregisterReceiver(this.receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            Logger.logW(SmsInterceptor.class, "unregister receiver failed.", e);
        }
        return false;
    }
}