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

public abstract class SmsReceiver
{
    private Context           context                            = null;
    private BroadcastReceiver receiver                           = null;
    private boolean           autoUnregisterWhenReceive          = false;
    private boolean           isDoneForAutoUnregisterWhenReceive = false;
    private Handler           handler                            = new Handler();
    private boolean           isUnregistered                     = true;
    private boolean           isUnregisteredCompletely           = true;
    private int               curTimeout                         = -1;

    public SmsReceiver(Context ctx, SmsFilter receiveFilter)
    {
        if (ctx == null)
            throw new NullPointerException();
        this.context = ctx;
        if (receiveFilter == null)
            receiveFilter = new SmsFilter()
            {
                public boolean accept(SmsMessage msg)
                {
                    return true;
                }
            };
        final SmsFilter receiveFilterPoint = receiveFilter;
        this.receiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                if (SmsReceiver.this.isUnregistered)
                    return;
                Bundle bundle = intent.getExtras();
                Object[] messages = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessages = new SmsMessage[messages.length];
                int smsMessagesIndex = 0;
                for (int i = 0; i < messages.length; i++)
                {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) messages[i]);
                    if (!receiveFilterPoint.accept(msg))
                        continue;
                    smsMessages[smsMessagesIndex] = msg;
                    smsMessagesIndex++;
                }

                SmsMessage[] returnSmsMessages = new SmsMessage[smsMessagesIndex];
                for (int i = 0; i < returnSmsMessages.length; i++)
                {
                    returnSmsMessages[i] = smsMessages[i];
                }
                if (returnSmsMessages.length > 0)
                {
                    if (SmsReceiver.this.autoUnregisterWhenReceive)
                    {
                        SmsReceiver.this.isDoneForAutoUnregisterWhenReceive = true;
                        if (!SmsReceiver.this.unregisterMe())
                            return;
                    }
                    SmsReceiver.this.onReceive(returnSmsMessages);
                }
            }
        };
    }

    public void onReceive(SmsMessage[] msg)
    {
    }

    public void onTimeout()
    {
    }

    public void setAutoUnregisterWhenReceive(boolean auto)
    {
        if (!this.isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterWhenReceive = auto;
    }

    public void registerMe(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!this.isUnregisteredCompletely)
        {
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        }
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

        this.isDoneForAutoUnregisterWhenReceive = false;
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
                    if (SmsReceiver.this.isDoneForAutoUnregisterWhenReceive)
                    {
                        cancel();
                        SmsReceiver.this.handler.post(new Runnable()
                        {
                            public void run()
                            {
                                SmsReceiver.this.isUnregisteredCompletely = true;
                            }
                        });
                    } else if (this.timeCount >= SmsReceiver.this.curTimeout)
                    {
                        cancel();
                        SmsReceiver.this.handler.post(new Runnable()
                        {
                            public void run()
                            {
                                if (SmsReceiver.this.unregisterMe())
                                {
                                    SmsReceiver.this.onTimeout();
                                }
                                SmsReceiver.this.isUnregisteredCompletely = true;
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
            this.isDoneForAutoUnregisterWhenReceive = true;
        else
            this.isUnregisteredCompletely = true;
        this.isUnregistered = true;
        try
        {
            this.context.unregisterReceiver(this.receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            Logger.logW(SmsReceiver.class, "unregister receiver failed.", e);
        }
        return false;
    }
}
