package com.greatwall.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

/**
 * @Title AndroidVersionCheckUtils
 * @Description AndroidVersionCheckUtils 用于多版本兼容检测
 */
public class VersionCheckUtils
{
    private VersionCheckUtils()
    {
    };

    public static int SDK_INT = Build.VERSION.SDK_INT;

    /**
     * 获取版本名称
     */
    public static String getVersionName(Context context)
    {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();

        PackageInfo packInfo;
        String version = "";
        try
        {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 获取版本号
     * 
     * @param context
     * @return
     */
    public static int getVersionCode(Context context)
    {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        PackageInfo packInfo;
        int version = 0;
        try
        {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version = packInfo.versionCode;
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 当前Android系统版本是否在（ Donut） Android 1.6或以上
     * 
     * @return
     */
    public static boolean hasDonut()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT;
    }

    /**
     * 当前Android系统版本是否在（ Eclair） Android 2.0或 以上
     * 
     * @return
     */
    public static boolean hasEclair()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR;
    }

    /**
     * 当前Android系统版本是否在（ Froyo） Android 2.2或 Android 2.2以上
     * 
     * @return
     */
    public static boolean hasFroyo()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * 当前Android系统版本是否在（ Gingerbread） Android 2.3x或 Android 2.3x 以上
     * 
     * @return
     */
    public static boolean hasGingerbread()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * 当前Android系统版本是否在（ Honeycomb） Android3.0或 Android3.0以上
     * 
     * @return
     */
    public static boolean hasHoneycomb()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * 当前Android系统版本是否在（ HoneycombMR1） Android3.1.x或 Android3.1.x以上
     * 
     * @return
     */
    public static boolean hasHoneycombMR1()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * 当前Android系统版本是否在（ IceCreamSandwich） Android4.0或 Android4.0以上
     * 
     * @return
     */
    public static boolean hasIcecreamsandwich()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * 4.1
     * 
     * @return
     */
    public static boolean hasJellyBean()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * 4.4
     * 
     * @return
     */
    public static boolean hasKitkat()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

}
