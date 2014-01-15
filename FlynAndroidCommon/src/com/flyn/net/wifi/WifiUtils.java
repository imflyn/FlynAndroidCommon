package com.flyn.net.wifi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.flyn.net.NetManager;
import com.flyn.net.wifi.support.Wifi;
import com.flyn.util.Logger;

public final class WifiUtils
{
    private Context              context     = null;
    private WifiManager          wifiManager = null;
    private WifiManager.WifiLock wifiLock    = null;

    public WifiUtils(Context context)
    {
        if (context == null)
            throw new NullPointerException();
        this.context = context;
        this.wifiManager = ((WifiManager) context.getSystemService("wifi"));
        this.wifiLock = this.wifiManager.createWifiLock("com.flyn.net.wifi.WifiUtils");
    }

    public boolean isWifiEnabled()
    {
        return this.wifiManager.isWifiEnabled();
    }

    public boolean isWifiConnected()
    {
        NetworkInfo wifiNetworkInfo = NetManager.getWifiNetworkInfo(this.context);

        return (wifiNetworkInfo != null) && (wifiNetworkInfo.isConnected());
    }

    public boolean isWifiUseful(int timeout, int tryTimes)
    {
        return (isWifiConnected()) && (NetManager.isNetUseful(timeout, tryTimes));
    }

    public WifiInfo getConnectionInfo()
    {
        return this.wifiManager.getConnectionInfo();
    }

    public void lockWifi()
    {
        this.wifiLock.acquire();
    }

    public void unlockWifi()
    {
        if (!this.wifiLock.isHeld())
        {
            this.wifiLock.release();
        }
    }

    public List<WifiConfiguration> getConfigurations()
    {
        return this.wifiManager.getConfiguredNetworks();
    }

    public List<WifiConfiguration> getConfiguration(ScanResult sr, boolean compareSecurity)
    {
        return Wifi.getWifiConfiguration(this.wifiManager, sr, compareSecurity);
    }

    public List<WifiConfiguration> getConfiguration(WifiConfiguration wc, boolean compareSecurity)
    {
        return Wifi.getWifiConfiguration(this.wifiManager, wc, compareSecurity);
    }

    public String getScanResultSecurity(ScanResult sr)
    {
        return Wifi.getScanResultSecurity(sr);
    }

    public void setupSecurity(WifiConfiguration wc, String security, String password)
    {
        Wifi.setupSecurity(wc, security, password);
    }

    public int getLevelGrade(int dbmLevel)
    {
        if (dbmLevel >= -47)
            return 1;
        if (dbmLevel >= -59)
            return 2;
        if (dbmLevel >= -71)
            return 3;
        if (dbmLevel >= -83)
        {
            return 4;
        }
        return 5;
    }

    public boolean disconnect()
    {
        return this.wifiManager.disconnect();
    }

    public WifiManager getWifiManager()
    {
        return this.wifiManager;
    }

    public void checkWifiExist(final WifiCallback callback, final int timeout)
    {
        if (callback == null)
            return;
        if (isWifiEnabled())
        {
            callback.onCheckWifiExist();
            return;
        }
        setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                Logger.logD(WifiUtils.class, "revert to previous wifi state...");
                WifiUtils.this.setWifiEnabled(false, new WifiCallback(WifiUtils.this.context)
                {
                    @Override
                    public void onWifiDisabled()
                    {
                        Logger.logD(WifiUtils.class, "revert to previous wifi state successfully.");
                        callback.onCheckWifiExist();
                    }

                    @Override
                    public void onWifiFailed()
                    {
                        Logger.logD(WifiUtils.class, "revert to previous wifi state unsuccessfully.");
                        callback.onCheckWifiExist();
                    }

                    @Override
                    public void onTimeout()
                    {
                        Logger.logD(WifiUtils.class, "revert to previous wifi state time out.");
                        callback.onCheckWifiExist();
                    }
                }, timeout);
            }

            @Override
            public void onWifiFailed()
            {
                callback.onCheckWifiNotExist();
            }

            @Override
            public void onTimeout()
            {
                Logger.logD(WifiUtils.class, "revert to previous wifi state...");
                WifiUtils.this.setWifiEnabled(false, new WifiCallback(WifiUtils.this.context)
                {
                    @Override
                    public void onWifiDisabled()
                    {
                        Logger.logD(WifiUtils.class, "revert to previous wifi state successfully.");
                        callback.onTimeout();
                    }

                    @Override
                    public void onWifiFailed()
                    {
                        Logger.logD(WifiUtils.class, "revert to previous wifi state unsuccessfully.");
                        callback.onTimeout();
                    }

                    @Override
                    public void onTimeout()
                    {
                        Logger.logD(WifiUtils.class, "revert to previous wifi state time out.");
                        callback.onTimeout();
                    }
                }, timeout);
            }
        }, timeout);
    }

    public void setWifiEnabled(final boolean enabled, final WifiCallback callback, final int timeout)
    {
        if (isWifiEnabled() == enabled)
        {
            if (callback != null)
            {
                if (enabled)
                    callback.onWifiEnabled();
                else
                    callback.onWifiDisabled();
            }
            return;
        }
        if (enabled)
        {
            try
            {
                int thisTimeout = timeout;
                if (callback == null)
                    thisTimeout = 20000;
                setWifiApEnabled(null, false, new WifiCallback(this.context)
                {
                    @Override
                    public void onWifiApDisabled()
                    {
                        super.onWifiApDisabled();
                        WifiUtils.this.setWifiEnabledImpl(enabled, callback, timeout);
                    }

                    @Override
                    public void onWifiApFailed()
                    {
                        super.onWifiApFailed();
                        WifiUtils.this.setWifiEnabledImpl(enabled, callback, timeout);
                    }

                    @Override
                    public void onTimeout()
                    {
                        super.onTimeout();
                        WifiUtils.this.setWifiEnabledImpl(enabled, callback, timeout);
                    }
                }, thisTimeout);
            } catch (Exception e)
            {
                Logger.logW(WifiUtils.class, "disable wifi ap failed.", e);
                setWifiEnabledImpl(enabled, callback, timeout);
            }
        } else
            setWifiEnabledImpl(enabled, callback, timeout);
    }

    private void setWifiEnabledImpl(boolean enabled, WifiCallback callback, int timeout)
    {
        if (callback != null)
        {
            if (enabled)
                callback.setAutoUnregisterActions(new int[] { 0, 4 });
            else
                callback.setAutoUnregisterActions(new int[] { 2, 4 });
            callback.registerMe(timeout);
        }
        boolean circs = this.wifiManager.setWifiEnabled(enabled);
        if ((!circs) && (callback != null))
        {
            if (callback.unregisterMe())
            {
                callback.onWifiFailed();
            }
        }
    }

    public void startScan(WifiCallback callback, int timeout)
    {
        if (!isWifiEnabled())
        {
            if (callback != null)
            {
                callback.onScanFailed();
            }
            return;
        }
        if (callback != null)
        {
            callback.setAutoUnregisterActions(new int[] { 5 });
            callback.registerMe(timeout);
        }
        boolean circs = this.wifiManager.startScan();
        if ((!circs) && (callback != null))
        {
            if (callback.unregisterMe())
            {
                callback.onScanFailed();
            }
        }
    }

    public void connect(WifiConfiguration wc, WifiCallback callback, int timeout)
    {
        WifiInfo info = getConnectionInfo();
        if (!isWifiEnabled())
        {
            if (callback != null)
            {
                callback.onNetworkFailed(info);
            }
            return;
        }
        String ssid = info.getSSID();
        String bssid = info.getBSSID();

        if ((ssid != null) && (Wifi.convertToQuotedString(ssid).equals(wc.SSID)) && ((wc.BSSID == null) || (wc.BSSID.equals(bssid))))
        {
            if (callback != null)
            {
                callback.onNetworkConnected(info);
            }
            return;
        }
        if (callback != null)
        {
            callback.setAutoUnregisterActions(new int[] { 11, 10, 9 });
            callback.ignoreInitialNetworkActions(true);
            callback.registerMe(timeout);
        }
        boolean circs = Wifi.connectToConfiguredNetwork(this.context, this.wifiManager, wc, true);
        if ((!circs) && (callback != null))
        {
            if (callback.unregisterMe())
            {
                callback.onNetworkFailed(info);
            }
        }
    }

    public void connect(ScanResult sr, String password, WifiCallback callback, int timeout)
    {
        WifiInfo info = getConnectionInfo();
        if (!isWifiEnabled())
        {
            if (callback != null)
            {
                callback.onNetworkFailed(info);
            }
            return;
        }
        String ssid = info.getSSID();
        String bssid = info.getBSSID();

        if ((ssid != null) && (sr.SSID != null) && (Wifi.convertToQuotedString(ssid).equals(Wifi.convertToQuotedString(sr.SSID))) && (bssid != null) && (bssid.equals(sr.BSSID)))
        {
            if (callback != null)
            {
                callback.onNetworkConnected(info);
            }
            return;
        }
        List<WifiConfiguration>  oldList = getConfiguration(sr, false);
        if ((oldList != null) && (oldList.size() != 0))
        {
            WifiConfiguration old = (WifiConfiguration) oldList.get(0);
            String security = getScanResultSecurity(sr);
            setupSecurity(old, security, password);
            if (!this.wifiManager.saveConfiguration())
            {
                if (callback != null)
                {
                    callback.onNetworkFailed(info);
                }
                return;
            }
            connect(old, callback, timeout);
            return;
        }
        if (callback != null)
        {
            callback.setAutoUnregisterActions(new int[] { 11, 10, 9 });
            callback.ignoreInitialNetworkActions(true);
            callback.registerMe(timeout);
        }
        boolean circs = Wifi.connectToNewNetwork(this.context, this.wifiManager, sr, password, 2147483647);
        if ((!circs) && (callback != null))
        {
            if (callback.unregisterMe())
            {
                callback.onNetworkFailed(info);
            }
        }
    }

    public boolean isWifiApEnabled() throws RuntimeException
    {
        try
        {
            Method method = WifiManager.class.getDeclaredMethod("isWifiApEnabled", new Class[0]);
            method.setAccessible(true);
            return ((Boolean) method.invoke(this.wifiManager, new Object[0])).booleanValue();
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

    public WifiConfiguration getWifiApConfiguration() throws RuntimeException
    {
        try
        {
            Method method = WifiManager.class.getDeclaredMethod("getWifiApConfiguration", new Class[0]);
            method.setAccessible(true);
            return (WifiConfiguration) method.invoke(this.wifiManager, new Object[0]);
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

    public boolean setWifiApConfiguration(WifiConfiguration apConfig) throws RuntimeException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        try
        {
            Method method = WifiManager.class.getDeclaredMethod("setWifiApConfig", new Class[] { WifiConfiguration.class });
            method.setAccessible(true);
            return ((Integer) method.invoke(this.wifiManager, new Object[] { apConfig })).intValue() > 0;
        } catch (NoSuchMethodException localNoSuchMethodException1)
        {
            Method method = WifiManager.class.getDeclaredMethod("setWifiApConfiguration", new Class[] { WifiConfiguration.class });
            method.setAccessible(true);
            return ((Boolean) method.invoke(this.wifiManager, new Object[] { apConfig })).booleanValue();
        } catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

    }

    public void setWifiApEnabled(final WifiConfiguration apConfig, final boolean enabled, final WifiCallback callback, final int timeout) throws RuntimeException
    {
        if (isWifiApEnabled() == enabled)
        {
            if ((!enabled) || (apConfig == null))
            {
                if (callback != null)
                {
                    if (enabled)
                        callback.onWifiApEnabled();
                    else
                        callback.onWifiApDisabled();
                }
                return;
            }
        }
        if (enabled)
        {
            int thisTimeout = timeout;
            if (callback == null)
                thisTimeout = 20000;
            setWifiEnabled(false, new WifiCallback(this.context)
            {
                @Override
                public void onWifiDisabled()
                {
                    super.onWifiDisabled();
                    try
                    {
                        WifiUtils.this.setWifiApEnabledImpl(apConfig, enabled, callback, timeout);
                    } catch (RuntimeException e)
                    {
                        Logger.logE(WifiUtils.class, "set wifi ap enabled failed.", e);
                        if (callback != null)
                            callback.onWifiApFailed();
                    }
                }

                @Override
                public void onWifiFailed()
                {
                    super.onWifiFailed();
                    if (callback != null)
                        callback.onWifiApFailed();
                }

                @Override
                public void onTimeout()
                {
                    super.onTimeout();
                    if (callback != null)
                        callback.onTimeout();
                }
            }, thisTimeout);
        } else
        {
            setWifiApEnabledImpl(apConfig, enabled, callback, timeout);
        }
    }

    private void setWifiApEnabledImpl(final WifiConfiguration apConfig, final boolean enabled, final WifiCallback callback, final int timeout) throws RuntimeException
    {
        if ((isWifiApEnabled()) && (enabled) && (apConfig != null))
        {
            int thisTimeout = timeout;
            if (callback == null)
                thisTimeout = 20000;
            setWifiApEnabledImpl(null, false, new WifiCallback(this.context)
            {
                @Override
                public void onWifiApDisabled()
                {
                    super.onWifiApDisabled();
                    try
                    {
                        WifiUtils.this.setWifiApEnabledImpl(apConfig, enabled, callback, timeout);
                    } catch (RuntimeException e)
                    {
                        Logger.logE(WifiUtils.class, "set wifi ap enabled failed.", e);
                        if (callback != null)
                            callback.onWifiApFailed();
                    }
                }

                @Override
                public void onWifiApFailed()
                {
                    super.onWifiApFailed();
                    if (callback != null)
                        callback.onWifiApFailed();
                }

                @Override
                public void onTimeout()
                {
                    super.onTimeout();
                    if (callback != null)
                        callback.onTimeout();
                }
            }, thisTimeout);
            return;
        }
        if (callback != null)
        {
            if (enabled)
                callback.setAutoUnregisterActions(new int[] { 12, 16 });
            else
                callback.setAutoUnregisterActions(new int[] { 14, 16 });
            callback.registerMe(timeout);
        }
        try
        {
            Method method = WifiManager.class.getDeclaredMethod("setWifiApEnabled", new Class[] { WifiConfiguration.class, Boolean.TYPE });
            method.setAccessible(true);
            boolean circs = ((Boolean) method.invoke(this.wifiManager, new Object[] { apConfig, Boolean.valueOf(enabled) })).booleanValue();
            if ((!circs) && (callback != null))
            {
                if (callback.unregisterMe())
                {
                    callback.onWifiApFailed();
                }
            }
        } catch (NoSuchMethodException e)
        {
            if (callback != null)
                callback.unregisterMe();
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            if (callback != null)
                callback.unregisterMe();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            if (callback != null)
                callback.unregisterMe();
            throw new RuntimeException(e);
        }
    }
}
