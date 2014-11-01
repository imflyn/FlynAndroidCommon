package com.flyn.telephone;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.flyn.telephone.receiver.SmsInterceptor;
import com.flyn.telephone.receiver.SmsReceiver;
import com.flyn.telephone.receiver.SmsSendCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class SmsUtils
{
    public static final String SMS_SENT_ACTION = "com.flyn.telephony.SMS_SENT";
    public static final String SMS_DELIVERED_ACTION = "com.flyn.telephony.SMS_DELIVERED";
    private static int sendMessageToken = 0;

    public static void sendMessage(Context context, String to, String text, SmsSendCallback ssc, int timeout, int cardIndex) throws RuntimeException
    {
        boolean isDualMode = TelephoneMgr.isDualMode();
        String name = null;
        String model = Build.MODEL;
        if (cardIndex == 0)
        {
            if ("Philips T939".equals(model))
            {
                name = "isms0";
            } else
            {
                name = "isms";
            }
        } else if (cardIndex == 1)
        {
            if (!isDualMode)
            {
                return;
            }
            if ("Philips T939".equals(model))
            {
                name = "isms1";
            } else
            {
                name = "isms2";
            }
        } else
        {
            throw new IllegalArgumentException("cardIndex can only be 0 or 1");
        }
        sendMessageToken += 1;
        Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra("SMS_TOKEN", sendMessageToken);
        sentIntent.putExtra("SMS_TO", to);
        sentIntent.putExtra("SMS_TEXT", text);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 1073741824);

        if (ssc != null)
        {
            ssc.setToken(sendMessageToken);
            ssc.setAutoUnregisterActions(new int[1]);
            ssc.registerMe(timeout);
        }
        try
        {
            if (isDualMode)
            {
                try
                {
                    Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
                    method.setAccessible(true);
                    Object param = method.invoke(null, new Object[]{name});
                    if (param == null)
                    {
                        throw new RuntimeException("can not get service which is named '" + name + "'");
                    }
                    method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", new Class[]{IBinder.class});
                    method.setAccessible(true);
                    Object stubObj = method.invoke(null, new Object[]{param});
                    method = stubObj.getClass().getMethod("sendText", new Class[]{String.class, String.class, String.class, PendingIntent.class, PendingIntent.class});
                    method.invoke(stubObj, new Object[]{to, null, text, sentPI, null});
                } catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            } else
            {
                SmsManager.getDefault().sendTextMessage(to, null, text, sentPI, null);
            }
        } catch (RuntimeException e)
        {
            if (ssc != null)
            {
                ssc.unregisterMe();
            }
            throw e;
        }

    }

    public static void receiveMessage(SmsReceiver sr, boolean interruptWhenReceive, int timeout)
    {
        if (sr == null)
        {
            throw new NullPointerException();
        }
        sr.setAutoUnregisterWhenReceive(interruptWhenReceive);
        sr.registerMe(timeout);
    }

    public static void interceptMessage(SmsInterceptor si, boolean interruptWhenIntercept, int timeout)
    {
        if (si == null)
        {
            throw new NullPointerException();
        }
        si.setAutoUnregisterWhenIntercept(interruptWhenIntercept);
        si.registerMe(1000, timeout);
    }
}