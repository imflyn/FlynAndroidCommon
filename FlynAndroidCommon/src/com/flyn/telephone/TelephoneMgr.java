package com.flyn.telephone;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.flyn.util.LogManager;

public final class TelephoneMgr
{
    public static boolean isDualMode()
    {
        try
        {
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[] { String.class });
            method.setAccessible(true);
            String model = Build.MODEL;
            if ("Philips T939".equals(model))
            {
                if (method.invoke(null, new Object[] { "phone0" }) != null)
                    if (method.invoke(null, new Object[] { "phone1" }) != null)
                        return true;
                return false;
            }
            if (method.invoke(null, new Object[] { "phone" }) != null)
            {
                if (method.invoke(null, new Object[] { "phone2" }) != null)
                    ;
            } else if (method.invoke(null, new Object[] { "telephony.registry" }) != null)
            {
                if (method.invoke(null, new Object[] { "telephony.registry2" }) != null)
                    ;
            } else
                return false;

            return true;
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
    }

    public static String getDeviceId(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        return tm.getDeviceId();
    }

    public static String getSubscriberId(int cardIndex)
    {
        String name = null;
        String model = Build.MODEL;
        if (cardIndex == 0)
        {
            if ("Philips T939".equals(model))
                name = "iphonesubinfo0";
            else
                name = "iphonesubinfo";
        } else if (cardIndex == 1)
        {
            if ("Philips T939".equals(model))
                name = "iphonesubinfo1";
            else
                name = "iphonesubinfo2";
        } else
            throw new IllegalArgumentException("cardIndex can only be 0 or 1");
        try
        {
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[] { String.class });
            method.setAccessible(true);
            Object param = method.invoke(null, new Object[] { name });
            if ((param == null) && (cardIndex == 1))
                param = method.invoke(null, new Object[] { "iphonesubinfo1" });
            if (param == null)
                return null;
            method = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub").getDeclaredMethod("asInterface", new Class[] { IBinder.class });
            method.setAccessible(true);
            Object stubObj = method.invoke(null, new Object[] { param });

            return (String) stubObj.getClass().getMethod("getSubscriberId", new Class[0]).invoke(stubObj, new Object[0]);
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
        }
        return "";
    }

    public static int getFirstSimState() throws RuntimeException
    {
        return getSimState("gsm.sim.state");
    }

    public static int getSecondSimState() throws RuntimeException
    {
        return getSimState("gsm.sim.state_2");
    }

    private static int getSimState(String simState)
    {
        try
        {
            Method method = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", new Class[] { String.class });
            method.setAccessible(true);
            String prop = (String) method.invoke(null, new Object[] { simState });
            if (prop != null)
                prop = prop.split(",")[0];
            if ("ABSENT".equals(prop))
            {
                return 1;
            }
            if ("PIN_REQUIRED".equals(prop))
            {
                return 2;
            }
            if ("PUK_REQUIRED".equals(prop))
            {
                return 3;
            }
            if ("NETWORK_LOCKED".equals(prop))
            {
                return 4;
            }
            if ("READY".equals(prop))
            {
                return 5;
            }
            return 0;

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
        }
        return 0;
    }

    public static boolean isFirstSimValid() throws RuntimeException
    {
        return getFirstSimState() == 5;
    }

    public static boolean isSecondSimValid() throws RuntimeException
    {
        return getSecondSimState() == 5;
    }

    public static boolean isChinaMobileCard(String subscriberId)
    {
        if (subscriberId == null)
        {
            return false;
        }
        return (subscriberId.contains("46000")) || (subscriberId.contains("46002")) || (subscriberId.contains("46007"));
    }

    public static String getExternalStorageState()
    {
        return Environment.getExternalStorageState();
    }

    public static long getExternalStorageSize()
    {
        return getFileStorageSize(Environment.getExternalStorageDirectory());
    }

    public static long getFileStorageSize(File file)
    {
        StatFs stat = new StatFs(file.getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    public static long getExternalStorageAvailableSize()
    {
        return getFileStorageAvailableSize(Environment.getExternalStorageDirectory());
    }

    public static long getFileStorageAvailableSize(File file)
    {
        StatFs stat = new StatFs(file.getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * (availableBlocks - 4L);
    }

    public static boolean isExternalStorageValid()
    {
        return getExternalStorageState().equals("mounted");
    }

    public static int getSDKVersion()
    {
        try
        {
            return Integer.valueOf(Build.VERSION.SDK_INT).intValue();
        } catch (NumberFormatException e)
        {
            LogManager.w(TelephoneMgr.class, "can not convert SDK", e);
        }
        return 0;
    }

    public static boolean isAndroid4Above()
    {
        return getSDKVersion() >= 14;
    }

    public static boolean isOPhone()
    {
        Class[] dclass = Settings.class.getClasses();
        if (dclass != null)
        {
            for (int i = 0; i < dclass.length; i++)
            {
                Class singleC = dclass[i];
                String name = singleC.getName();
                if (name.indexOf("Data_connection") != -1)
                {
                    return true;
                }
            }
        }
        try
        {
            Class.forName("oms.dcm.DataConnectivityConstants");
            return true;
        } catch (ClassNotFoundException localClassNotFoundException)
        {
        }
        return false;
    }

    public static boolean isOPhone20()
    {
        return (isOPhone()) && (getSDKVersion() == 7);
    }

    public static boolean isUsingNewButtonPlacementStyle()
    {
        String model = Build.MODEL;
        if (("GT-P3108".equals(model)) || ("GT-I9050".equals(model)) || ("BBK S6T".equals(model)))
            return false;
        if (("GT-I9108".equals(model)) || ("GT-I9228".equals(model)))
            return getSDKVersion() >= 16;
        return isAndroid4Above();
    }
}