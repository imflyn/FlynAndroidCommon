package com.flyn.util;

import android.util.Log;

public final class LogManager
{
    private static final int VERBOSE       = 2;
    private static final int DEBUG         = 3;
    private static final int INFO          = 4;
    private static final int WARN          = 5;
    private static final int ERROR         = 6;
    // private static final int ASSERT = 7;
    private static int       LOGGING_LEVEL = VERBOSE;

    private static boolean   ENABLED_JLOG  = false;
    private static boolean   ENABLED       = true;

    public static void v(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (2 >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.v(tagStr, createMessage(msg));
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("V", tagStr, createMessage(msg));
        }
    }

    public static void v(Class<? extends Object> tag, String msg, Throwable tr)
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
                Log.v(tagStr, createMessage(msg));
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("V", tagStr, createMessage(msg));
            } else
            {
                Log.v(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("V", tagStr, createMessage(msg));
            }
        }
    }

    public static void d(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.d(tagStr, createMessage(msg));
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("D", tagStr, createMessage(msg));
        }
    }

    public static void d(Class<? extends Object> tag, String msg, Throwable tr)
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
                Log.d(tagStr, createMessage(msg));
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("D", tagStr, createMessage(msg));
            } else
            {
                Log.d(tagStr, createMessage(msg), tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("D", tagStr, createMessage(msg));
            }
        }
    }

    public static void i(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (INFO >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.i(tagStr, createMessage(msg));
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("I", tagStr, createMessage(msg));
        }
    }

    public static void i(Class<? extends Object> tag, String msg, Throwable tr)
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
                Log.i(tagStr, createMessage(msg));
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("I", tagStr, createMessage(msg));
            } else
            {
                Log.i(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("I", tagStr, createMessage(msg));
            }
        }
    }

    public static void w(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.w(tagStr, createMessage(msg));
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("W", tagStr, createMessage(msg));
        }
    }

    public static void w(Class<? extends Object> tag, Throwable tr)
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

    public static void w(Class<? extends Object> tag, String msg, Throwable tr)
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
                Log.w(tagStr, createMessage(msg));
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, createMessage(msg));
            } else
            {
                Log.w(tagStr, createMessage(msg), tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, tr.toString());
            }
        }
    }

    public static void e(Class<? extends Object> tag, String msg)
    {
        if (!ENABLED)
            return;

        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.e(tagStr, createMessage(msg));
            if (ENABLED_JLOG)
                JLog.writeLogtoFile("W", tagStr, createMessage(msg));
        }
    }

    public static void e(Class<? extends Object> tag, String msg, Throwable tr)
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
                Log.e(tagStr, createMessage(msg));
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, createMessage(msg));
            } else
            {
                Log.e(tagStr, createMessage(msg), tr);
                if (ENABLED_JLOG)
                    JLog.writeLogtoFile("W", tagStr, createMessage(msg));
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

            if (st.getClassName().equals(LogManager.class.getName()))
            {
                continue;
            }

            return "line:" + st.getLineNumber();
            // return "[" + Thread.currentThread().getName() + "(" +
            // Thread.currentThread().getId() + "): " + st.getFileName() + ":" +
            // st.getLineNumber() + "]";
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
