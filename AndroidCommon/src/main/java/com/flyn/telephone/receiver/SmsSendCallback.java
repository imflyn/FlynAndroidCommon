package com.flyn.telephone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.flyn.telephone.SmsUtils;
import com.flyn.util.LogManager;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public abstract class SmsSendCallback
{
    public static final int ACTION_SENT = 0;
    public static final int ACTION_DELIVERED = 1;
    private Context context = null;
    private BroadcastReceiver receiver = null;
    private int token = -1;
    private int[] autoUnregisterActions = new int[0];
    private boolean isDoneForAutoUnregisterActions = false;
    private Handler handler = new Handler();
    private boolean isUnregistered = true;
    private boolean isUnregisteredCompletely = true;
    private int curTimeout = -1;

    public SmsSendCallback(Context ctx)
    {
        if (ctx == null)
        {
            throw new NullPointerException();
        }
        this.context = ctx;
        this.receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (SmsSendCallback.this.isUnregistered)
                {
                    return;
                }
                String actionStr = intent.getAction();
                int code = getResultCode();
                int srcToken = intent.getIntExtra("SMS_TOKEN", -1);
                String to = intent.getStringExtra("SMS_TO");
                String text = intent.getStringExtra("SMS_TEXT");
                if ((SmsSendCallback.this.token == -1) || (SmsSendCallback.this.token == srcToken))
                {
                    if (actionStr.equals(SmsUtils.SMS_SENT_ACTION))
                    {
                        if (Arrays.binarySearch(SmsSendCallback.this.autoUnregisterActions, 0) > -1)
                        {
                            SmsSendCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!SmsSendCallback.this.unregisterMe())
                            {
                                return;
                            }
                        }
                        if (code == -1)
                        {
                            SmsSendCallback.this.onSendSuccess(to, text);
                        } else
                        {
                            SmsSendCallback.this.onSendFailure(to, text);
                        }
                    } else if (actionStr.equals(SmsUtils.SMS_DELIVERED_ACTION))
                    {
                        if (Arrays.binarySearch(SmsSendCallback.this.autoUnregisterActions, 1) > -1)
                        {
                            SmsSendCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!SmsSendCallback.this.unregisterMe())
                            {
                                return;
                            }
                        }
                        if (code == -1)
                        {
                            SmsSendCallback.this.onDeliverSuccess(to, text);
                        } else
                        {
                            SmsSendCallback.this.onDeliverFailure(to, text);
                        }
                    }
                }
            }
        };
        Arrays.sort(this.autoUnregisterActions);
    }

    public void onDeliverSuccess(String to, String text)
    {
    }

    public void onDeliverFailure(String to, String text)
    {
    }

    public void onSendSuccess(String to, String text)
    {
    }

    public void onSendFailure(String to, String text)
    {
    }

    public void onTimeout()
    {
    }

    public void setToken(int token)
    {
        if (!this.isUnregisteredCompletely)
        {
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        }
        this.token = token;
    }

    public void setAutoUnregisterActions(int[] actions)
    {
        if (actions == null)
        {
            throw new NullPointerException();
        }
        if (!this.isUnregisteredCompletely)
        {
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        }
        this.autoUnregisterActions = (actions.clone());
        Arrays.sort(this.autoUnregisterActions);
    }

    public void registerMe(int timeout)
    {
        if (timeout < 0)
        {
            throw new IllegalArgumentException("timeout could not be below zero.");
        }
        if (!this.isUnregisteredCompletely)
        {
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        }
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction(SmsUtils.SMS_SENT_ACTION);
        smsIntentFilter.addAction(SmsUtils.SMS_DELIVERED_ACTION);
        this.isDoneForAutoUnregisterActions = false;
        this.isUnregistered = false;
        this.isUnregisteredCompletely = false;
        this.context.registerReceiver(this.receiver, smsIntentFilter, null, this.handler);
        this.curTimeout = timeout;
        if (this.curTimeout > 0)
        {
            new Timer().schedule(new TimerTask()
            {
                protected long timeCount = 0L;

                @Override
                public void run()
                {
                    this.timeCount += 100L;
                    if (SmsSendCallback.this.isDoneForAutoUnregisterActions)
                    {
                        cancel();
                        SmsSendCallback.this.handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                SmsSendCallback.this.isUnregisteredCompletely = true;
                            }
                        });
                    } else if (this.timeCount >= SmsSendCallback.this.curTimeout)
                    {
                        cancel();
                        SmsSendCallback.this.handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (SmsSendCallback.this.unregisterMe())
                                {
                                    SmsSendCallback.this.onTimeout();
                                }
                                SmsSendCallback.this.isUnregisteredCompletely = true;
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
        {
            this.isDoneForAutoUnregisterActions = true;
        } else
        {
            this.isUnregisteredCompletely = true;
        }
        this.isUnregistered = true;
        try
        {
            this.context.unregisterReceiver(this.receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            LogManager.w(SmsSendCallback.class, "unregister receiver failed.", e);
        }
        return false;
    }
}