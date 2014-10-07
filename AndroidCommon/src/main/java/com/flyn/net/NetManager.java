package com.flyn.net;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.flyn.net.synchttp.HttpConnectionManager;
import com.flyn.net.synchttp.HttpResponseResult;
import com.flyn.util.LogManager;

import java.io.IOException;

@SuppressLint("DefaultLocale")
public final class NetManager
{
    public static boolean isNetConnected(Context context)
    {
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null)
        {
            return info.getState() == NetworkInfo.State.CONNECTED;
        }
        return false;
    }

    public static boolean isNetUseful(int timeout, int tryTimes)
    {
        if (tryTimes <= 0)
        {
            throw new IllegalArgumentException("trying times should be greater than zero.");
        }
        int th = 1;
        while (th <= tryTimes)
        {
            try
            {
                HttpResponseResult result = HttpConnectionManager.doGet("http://www.baidu.com", true, timeout, null);
                String host = result.getResponseURL().getHost();
                String content = result.getDataString("utf-8");

                return ("www.baidu.com".equalsIgnoreCase(host)) && (content.indexOf("baidu.com") >= 0);
            } catch (IOException e)
            {
                LogManager.e(NetManager.class, "the " + th + " time to check net for method of isNetUseful failed.", e);

                th++;
            }
        }
        LogManager.e(NetManager.class, "checking net for method of isNetUseful has all failed,will return false.");
        return false;
    }

    public static NetworkInfo getActiveNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return connectivityManager.getActiveNetworkInfo();
    }

    public static NetworkInfo getMobileNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return connectivityManager.getNetworkInfo(0);
    }

    public static NetworkInfo getWifiNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return connectivityManager.getNetworkInfo(1);
    }

    public static NetworkInfo[] getAllNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return connectivityManager.getAllNetworkInfo();
    }

    @SuppressLint("DefaultLocale")
    public static String getNetworkDetailType(NetworkInfo info)
    {
        if (info.getType() == 1)
        {
            return "WIFI";
        }
        String extraInfo = info.getExtraInfo();
        if (extraInfo == null)
        {
            return info.getTypeName().toUpperCase();
        }
        extraInfo = extraInfo.toUpperCase();
        if (extraInfo.contains("CMNET"))
        {
            return "CMNET";
        }
        if (extraInfo.contains("CMWAP"))
        {
            return "CMWAP";
        }
        if (extraInfo.contains("UNINET"))
        {
            return "UNINET";
        }
        if (extraInfo.contains("UNIWAP"))
        {
            return "UNIWAP";
        }
        if (extraInfo.contains("CTNET"))
        {
            return "CTNET";
        }
        if (extraInfo.contains("CTWAP"))
        {
            return "CTWAP";
        }
        return extraInfo;
    }

    public static String getCurNetworkDetailType(Context context)
    {
        NetworkInfo ni = getActiveNetworkInfo(context);
        if (ni == null)
        {
            return null;
        }
        return getNetworkDetailType(ni);
    }

    public static boolean isInAirplaneMode(Context context)
    {
        return Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public static boolean isAvailableMultiConnectedNets(Context context)
    {
        boolean wifiConnected = false;
        boolean otherConnected = false;
        NetworkInfo[] infos = getAllNetworkInfo(context);
        if (infos != null)
        {
            for (int i = 0; i < infos.length; i++)
            {
                if (infos[i].getState() != NetworkInfo.State.CONNECTED)
                {
                    continue;
                }
                if (infos[i].getType() == 1)
                {
                    wifiConnected = true;
                } else
                {
                    otherConnected = true;
                }
            }
        }

        return (wifiConnected) && (otherConnected);
    }

    public static void startWirelessSettingsActivity(Context context)
    {
        Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
        intent.addCategory("android.intent.category.DEFAULT");
        context.startActivity(intent);
    }
}
