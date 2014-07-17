package com.greatwall.util;

import android.util.Log;

public final class L
{
    private static final int    VERBOSE       = 2;
    private static final int    DEBUG         = 3;
    private static final int    INFO          = 4;
    private static final int    WARN          = 5;
    private static final int    ERROR         = 6;
    private static int          LOGGING_LEVEL = VERBOSE;

    private static boolean      ENABLED       = true;
    private static final String TAG           = "GreatWall";

    public static void v(String msg)
    {
        if (!ENABLED)
            return;

        if (2 >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            Log.v(TAG, createMessage(msg));
        }
    }

    public static void v(String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (VERBOSE >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            if (tr == null)
            {
                Log.v(TAG, createMessage(msg));
            } else
            {
                Log.v(TAG, msg, tr);
            }
        }
    }

    public static void d(String msg)
    {
        if (!ENABLED)
            return;

        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            Log.d(TAG, createMessage(msg));
        }
    }

    public static void d(String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            if (tr == null)
            {
                Log.d(TAG, createMessage(msg));
            } else
            {
                Log.d(TAG, createMessage(msg), tr);
            }
        }
    }

    public static void i(String msg)
    {
        if (!ENABLED)
            return;

        if (INFO >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            Log.i(TAG, createMessage(msg));
        }
    }

    public static void i(String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (4 >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            if (tr == null)
            {
                Log.i(TAG, createMessage(msg));
            } else
            {
                Log.i(TAG, msg, tr);
            }
        }
    }

    public static void w(String msg)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            Log.w(TAG, createMessage(msg));
        }
    }

    public static void w(Throwable tr)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            if (tr == null)
            {
                Log.w(TAG, "");
            } else
            {
                Log.w(TAG, tr);
            }
        }
    }

    public static void w(String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            if (tr == null)
            {
                Log.w(TAG, createMessage(msg));
            } else
            {
                Log.w(TAG, createMessage(msg), tr);
            }
        }
    }

    public static void e(String msg)
    {
        if (!ENABLED)
            return;

        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            Log.e(TAG, createMessage(msg));
        }
    }

    public static void e(String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            if (tr == null)
            {
                Log.e(TAG, createMessage(msg));
            } else
            {
                Log.e(TAG, createMessage(msg), tr);
            }
        }
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

            return "line:" + st.getLineNumber();
        }

        return null;
    }

    private static String createMessage(String msg)
    {
        String functionName = getFunctionName();
        String message = (functionName == null ? msg : (functionName + "[" + msg + "]"));
        return message;
    }

}
