package com.flyn.util;

import android.util.Log;

public final class L
{

    private static final String    LOG_FORMAT = "%1$s\n%2$s";
    public static volatile boolean DISABLED   = false;
    private static final String    TAG        = "GreatWall";

    private L()
    {
    }

    public static void enableLogging()
    {
        DISABLED = false;
    }

    public static void disableLogging()
    {
        DISABLED = true;
    }

    public static void d(String message, Object... args)
    {
        log(Log.DEBUG, null, message, args);
    }

    public static void v(String message, Object... args)
    {
        log(Log.VERBOSE, null, message, args);
    }

    public static void i(String message, Object... args)
    {
        log(Log.INFO, null, message, args);
    }

    public static void w(String message, Object... args)
    {
        log(Log.WARN, null, message, args);
    }

    public static void e(Throwable ex)
    {
        log(Log.ERROR, ex, null);
    }

    public static void e(String message, Object... args)
    {
        log(Log.ERROR, null, message, args);
    }

    public static void e(Throwable ex, String message, Object... args)
    {
        log(Log.ERROR, ex, message, args);
    }

    private static void log(int priority, Throwable ex, String message, Object... args)
    {
        if (DISABLED)
            return;
        if (args.length > 0)
        {
            message = String.format("" + message, args);
        }

        String log;
        if (ex == null)
        {
            log = message;
        } else
        {
            String logMessage = message == null ? ex.getMessage() : message;
            String logBody = Log.getStackTraceString(ex);
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }

        Log.println(priority, TAG, createMessage(log));
    }

    private static String getFunctionName()
    {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null)
        {
            return null;
        }

        for (StackTraceElement st : sts)
        {
            if (st.isNativeMethod())
            {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName()))
            {
                continue;
            }

            if (st.getClassName().equals(L.class.getName()))
            {
                continue;
            }
            return "[" + st.getFileName() + "] line:" + st.getLineNumber() + "==";
        }

        return null;
    }

    private static String createMessage(String msg)
    {
        String functionName = getFunctionName();
        String message = (functionName == null ? msg : (functionName + msg));
        return message;
    }

}