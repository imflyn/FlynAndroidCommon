package com.flyn.util;

import android.util.Log;

public final class Logger
{
    private static final int VERBOSE       = 2;
    private static final int DEBUG         = 3;
    private static final int INFO          = 4;
    private static final int WARN          = 5;
    private static final int ERROR         = 6;
    private static final int ASSERT        = 7;
    private static int       LOGGING_LEVEL = 2;

    private static boolean   ENABLED_JLOG  = false;
    private static boolean   ENABLED       = true;

    public static void logV(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (2 >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.v(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("V", tagStr, msg);
        }
    }

    public static void logV(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (VERBOSE >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.v(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("V", tagStr, msg);
            } else
            {
                Log.v(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("V", tagStr, msg);
            }
        }
    }

    public static void logD(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.d(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("D", tagStr, msg);
        }
    }

    public static void logD(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.d(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("D", tagStr, msg);
            } else
            {
                Log.d(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("D", tagStr, msg);
            }
        }
    }

    public static void logI(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (INFO >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.i(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("I", tagStr, msg);
        }
    }

    public static void logI(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (4 >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.i(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("I", tagStr, msg);
            } else
            {
                Log.i(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("I", tagStr, msg);
            }
        }
    }

    public static void logW(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.w(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("W", tagStr, msg);
        }
    }

    public static void logW(Class<? extends Object> tag, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.w(tagStr, "");
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, "");
            } else
            {
                Log.w(tagStr, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, tr.toString());
            }
        }
    }

    public static void logW(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.w(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr,msg);
            } else
            {
                Log.w(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, tr.toString());
            }
        }
    }

    public static void logE(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.e(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("W", tagStr, msg);
        }
    }

    public static void logE(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (!ENABLED)
            return;

        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.e(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, msg);
            } else
            {
                Log.e(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, msg);
            }
        }
    }
}
