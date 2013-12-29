package com.flyn.net.wifi;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.flyn.util.Logger;

public abstract class WifiCallback
{
    public static final int           ACTION_WIFI_ENABLED            = 0;
    public static final int           ACTION_WIFI_ENABLING           = 1;
    public static final int           ACTION_WIFI_DISABLED           = 2;
    public static final int           ACTION_WIFI_DISABLING          = 3;
    public static final int           ACTION_WIFI_FAILED             = 4;
    public static final int           ACTION_SCAN_RESULTS            = 5;
    public static final int           ACTION_NETWORK_IDLE            = 6;
    public static final int           ACTION_NETWORK_SCANNING        = 7;
    public static final int           ACTION_NETWORK_OBTAININGIP     = 8;
    public static final int           ACTION_NETWORK_DISCONNECTED    = 9;
    public static final int           ACTION_NETWORK_CONNECTED       = 10;
    public static final int           ACTION_NETWORK_FAILED          = 11;
    public static final int           ACTION_WIFI_AP_ENABLED         = 12;
    public static final int           ACTION_WIFI_AP_ENABLING        = 13;
    public static final int           ACTION_WIFI_AP_DISABLED        = 14;
    public static final int           ACTION_WIFI_AP_DISABLING       = 15;
    public static final int           ACTION_WIFI_AP_FAILED          = 16;
    private static String             WIFI_AP_STATE_CHANGED_ACTION   = null;
    private static String             EXTRA_WIFI_AP_STATE            = null;
    private static int                WIFI_AP_STATE_DISABLING        = -1;
    private static int                WIFI_AP_STATE_DISABLED         = -1;
    private static int                WIFI_AP_STATE_ENABLING         = -1;
    private static int                WIFI_AP_STATE_ENABLED          = -1;
    private static int                WIFI_AP_STATE_FAILED           = -1;

    private Context                   context                        = null;
    private BroadcastReceiver         receiver                       = null;
    private boolean                   ignoreInitialNetworkActions    = false;
    private boolean                   isInitialNetworkAction         = true;
    private NetworkInfo.DetailedState prevNetworkDetailed            = null;
    private int[]                     autoUnregisterActions          = new int[0];
    private boolean                   isDoneForAutoUnregisterActions = false;
    private Handler                   handler                        = new Handler();
    private boolean                   isUnregistered                 = true;
    private boolean                   isUnregisteredCompletely       = true;
    private int                       curTimeout                     = -1;

    public WifiCallback(Context ctx)
    {
        if (ctx == null)
            throw new NullPointerException();
        this.context = ctx;
        this.receiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                if (WifiCallback.this.isUnregistered)
                    return;
                String action = intent.getAction();
                if ((isInitialStickyBroadcast()) && (WifiCallback.this.autoUnregisterActions.length > 0))
                {
                    if (action.equals("android.net.wifi.STATE_CHANGE"))
                    {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo.getType() == 1)
                        {
                            WifiCallback.this.prevNetworkDetailed = networkInfo.getDetailedState();
                        }
                    }
                    Logger.logD(WifiCallback.class, "ignore initial sticky state");
                    return;
                }
                WifiUtils wifiUtils = new WifiUtils(context);
                if (action.equals("android.net.wifi.WIFI_STATE_CHANGED"))
                {
                    int state = intent.getIntExtra("wifi_state", 0);
                    if (state == 3)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_ENABLED");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 0) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiEnabled();
                    } else if (state == 2)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_ENABLING");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 1) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiEnabling();
                    } else if (state == 1)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_DISABLED");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 2) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiDisabled();
                    } else if (state == 0)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_DISABLING");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 3) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiDisabling();
                    } else if (state == 4)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_UNKNOWN");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 4) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiFailed();
                    }
                } else if (action.equals("android.net.wifi.SCAN_RESULTS"))
                {
                    Logger.logD(WifiCallback.class, "receive scan state -> SCAN_RESULTS_AVAILABLE");
                    if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 5) > -1)
                    {
                        WifiCallback.this.isDoneForAutoUnregisterActions = true;
                        if (!WifiCallback.this.unregisterMe())
                            return;
                    }
                    List<ScanResult> results = wifiUtils.getWifiManager().getScanResults();
                    if (results != null)
                    {
                        for (int i = 0; i < results.size(); i++)
                        {
                            ScanResult curr = (ScanResult) results.get(i);
                            for (int j = i - 1; j >= 0; j--)
                            {
                                ScanResult pre = (ScanResult) results.get(j);
                                if (curr.level <= pre.level)
                                {
                                    if (i == j + 1)
                                        break;
                                    results.remove(i);
                                    results.add(j + 1, curr);

                                    break;
                                }
                                if (j != 0)
                                    continue;
                                if (i == 0)
                                    continue;
                                results.remove(i);
                                results.add(0, curr);
                            }

                        }

                        for (int i = 0; i < results.size(); i++)
                        {
                            ScanResult curr = (ScanResult) results.get(i);
                            for (int j = 0; j < i; j++)
                            {
                                ScanResult pre = (ScanResult) results.get(j);
                                if ((!curr.SSID.equals(pre.SSID)) || (!wifiUtils.getScanResultSecurity(curr).equals(wifiUtils.getScanResultSecurity(pre))))
                                    continue;
                                results.remove(i);
                                i--;
                                break;
                            }
                        }
                    }

                    WifiCallback.this.onScanResults(results);
                } else if (action.equals("android.net.wifi.STATE_CHANGE"))
                {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo.getType() == 1)
                    {
                        NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
                        if (WifiCallback.this.ignoreInitialNetworkActions)
                        {
                            if (WifiCallback.this.isInitialNetworkAction)
                            {
                                if (detailed == NetworkInfo.DetailedState.IDLE)
                                {
                                    WifiCallback.this.isInitialNetworkAction = false;
                                } else if (detailed == NetworkInfo.DetailedState.SCANNING)
                                {
                                    if ((WifiCallback.this.prevNetworkDetailed == null) || (WifiCallback.this.prevNetworkDetailed == NetworkInfo.DetailedState.SCANNING))
                                    {
                                        WifiCallback.this.prevNetworkDetailed = detailed;
                                        Logger.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_SCANNING");
                                        return;
                                    }
                                    WifiCallback.this.isInitialNetworkAction = false;
                                } else if (detailed == NetworkInfo.DetailedState.OBTAINING_IPADDR)
                                {
                                    if ((WifiCallback.this.prevNetworkDetailed == null) || (WifiCallback.this.prevNetworkDetailed == NetworkInfo.DetailedState.SCANNING)
                                            || (WifiCallback.this.prevNetworkDetailed == NetworkInfo.DetailedState.OBTAINING_IPADDR))
                                    {
                                        WifiCallback.this.prevNetworkDetailed = detailed;
                                        Logger.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_OBTAININGIP");
                                        return;
                                    }
                                    WifiCallback.this.isInitialNetworkAction = false;
                                } else
                                {
                                    if (detailed == NetworkInfo.DetailedState.DISCONNECTED)
                                    {
                                        WifiCallback.this.isInitialNetworkAction = false;
                                        Logger.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_DISCONNECTED");
                                        return;
                                    }
                                    if (detailed == NetworkInfo.DetailedState.CONNECTED)
                                    {
                                        WifiCallback.this.isInitialNetworkAction = false;
                                    } else if (detailed == NetworkInfo.DetailedState.FAILED)
                                    {
                                        WifiCallback.this.isInitialNetworkAction = false;
                                        Logger.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_FAILED");
                                        return;
                                    }
                                }
                            }
                        }
                        if (detailed == NetworkInfo.DetailedState.IDLE)
                        {
                            Logger.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_IDLE");
                            if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 6) > -1)
                            {
                                WifiCallback.this.isDoneForAutoUnregisterActions = true;
                                if (!WifiCallback.this.unregisterMe())
                                    return;
                            }
                            WifiCallback.this.onNetworkIdle(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.SCANNING)
                        {
                            Logger.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_SCANNING");
                            if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 7) > -1)
                            {
                                WifiCallback.this.isDoneForAutoUnregisterActions = true;
                                if (!WifiCallback.this.unregisterMe())
                                    return;
                            }
                            WifiCallback.this.onNetworkScanning(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.OBTAINING_IPADDR)
                        {
                            Logger.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_OBTAININGIP");
                            if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 8) > -1)
                            {
                                WifiCallback.this.isDoneForAutoUnregisterActions = true;
                                if (!WifiCallback.this.unregisterMe())
                                    return;
                            }
                            WifiCallback.this.onNetworkObtainingIp(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.DISCONNECTED)
                        {
                            Logger.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_DISCONNECTED");
                            if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 9) > -1)
                            {
                                WifiCallback.this.isDoneForAutoUnregisterActions = true;
                                if (!WifiCallback.this.unregisterMe())
                                    return;
                            }
                            WifiCallback.this.onNetworkDisconnected(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.CONNECTED)
                        {
                            Logger.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_CONNECTED");
                            if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 10) > -1)
                            {
                                WifiCallback.this.isDoneForAutoUnregisterActions = true;
                                if (!WifiCallback.this.unregisterMe())
                                    return;
                            }
                            WifiCallback.this.onNetworkConnected(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.FAILED)
                        {
                            Logger.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_FAILED");
                            if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 11) > -1)
                            {
                                WifiCallback.this.isDoneForAutoUnregisterActions = true;
                                if (!WifiCallback.this.unregisterMe())
                                    return;
                            }
                            WifiCallback.this.onNetworkFailed(wifiUtils.getConnectionInfo());
                        }
                    }
                } else if (action.equals(WifiCallback.WIFI_AP_STATE_CHANGED_ACTION))
                {
                    if (WifiCallback.EXTRA_WIFI_AP_STATE == null)
                        return;
                    int state = intent.getIntExtra(WifiCallback.EXTRA_WIFI_AP_STATE, 0);
                    if (state == WifiCallback.WIFI_AP_STATE_ENABLED)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_ENABLED");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 12) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiApEnabled();
                    } else if (state == WifiCallback.WIFI_AP_STATE_ENABLING)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_ENABLING");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 13) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiApEnabling();
                    } else if (state == WifiCallback.WIFI_AP_STATE_DISABLED)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_DISABLED");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 14) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiApDisabled();
                    } else if (state == WifiCallback.WIFI_AP_STATE_DISABLING)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_DISABLING");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 15) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiApDisabling();
                    } else if (state == WifiCallback.WIFI_AP_STATE_FAILED)
                    {
                        Logger.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_FAILED");
                        if (Arrays.binarySearch(WifiCallback.this.autoUnregisterActions, 16) > -1)
                        {
                            WifiCallback.this.isDoneForAutoUnregisterActions = true;
                            if (!WifiCallback.this.unregisterMe())
                                return;
                        }
                        WifiCallback.this.onWifiApFailed();
                    }
                }
            }
        };
        Arrays.sort(this.autoUnregisterActions);
        try
        {
            Field field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_CHANGED_ACTION");
            field.setAccessible(true);
            WIFI_AP_STATE_CHANGED_ACTION = (String) field.get(null);
            field = WifiManager.class.getDeclaredField("EXTRA_WIFI_AP_STATE");
            field.setAccessible(true);
            EXTRA_WIFI_AP_STATE = (String) field.get(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_DISABLING");
            field.setAccessible(true);
            WIFI_AP_STATE_DISABLING = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_DISABLED");
            field.setAccessible(true);
            WIFI_AP_STATE_DISABLED = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_ENABLING");
            field.setAccessible(true);
            WIFI_AP_STATE_ENABLING = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_ENABLED");
            field.setAccessible(true);
            WIFI_AP_STATE_ENABLED = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_FAILED");
            field.setAccessible(true);
            WIFI_AP_STATE_FAILED = field.getInt(null);
        } catch (NoSuchFieldException e)
        {
            Logger.logW(WifiCallback.class, "reflect wifi ap field failed.", e);
        } catch (IllegalAccessException e)
        {
            Logger.logW(WifiCallback.class, "reflect wifi ap field failed.", e);
        }
    }

    public void onCheckWifiExist()
    {
    }

    public void onCheckWifiNotExist()
    {
    }

    public void onWifiEnabled()
    {
    }

    public void onWifiEnabling()
    {
    }

    public void onWifiDisabled()
    {
    }

    public void onWifiDisabling()
    {
    }

    public void onWifiFailed()
    {
    }

    public void onScanResults(List<ScanResult> scanResults)
    {
    }

    public void onScanFailed()
    {
    }

    public void onNetworkIdle(WifiInfo wifiInfo)
    {
    }

    public void onNetworkScanning(WifiInfo wifiInfo)
    {
    }

    public void onNetworkObtainingIp(WifiInfo wifiInfo)
    {
    }

    public void onNetworkDisconnected(WifiInfo wifiInfo)
    {
    }

    public void onNetworkConnected(WifiInfo wifiInfo)
    {
    }

    public void onNetworkFailed(WifiInfo wifiInfo)
    {
    }

    public void onWifiApEnabled()
    {
    }

    public void onWifiApEnabling()
    {
    }

    public void onWifiApDisabled()
    {
    }

    public void onWifiApDisabling()
    {
    }

    public void onWifiApFailed()
    {
    }

    public void onTimeout()
    {
    }

    public void setAutoUnregisterActions(int[] actions)
    {
        if (actions == null)
            throw new NullPointerException();
        if (!this.isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterActions = ((int[]) actions.clone());
        Arrays.sort(this.autoUnregisterActions);
    }

    public void ignoreInitialNetworkActions(boolean ignore)
    {
        if (!this.isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.ignoreInitialNetworkActions = ignore;
    }

    public void registerMe(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!this.isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        wifiIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        wifiIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        if (WIFI_AP_STATE_CHANGED_ACTION != null)
            wifiIntentFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        this.isDoneForAutoUnregisterActions = false;
        this.isUnregistered = false;
        this.isUnregisteredCompletely = false;
        this.context.registerReceiver(this.receiver, wifiIntentFilter, null, this.handler);
        this.curTimeout = timeout;
        if (this.curTimeout > 0)
        {
            new Timer().schedule(new TimerTask()
            {
                protected long timeCount = 0L;

                public void run()
                {
                    this.timeCount += 100L;
                    if (WifiCallback.this.isDoneForAutoUnregisterActions)
                    {
                        cancel();
                        WifiCallback.this.handler.post(new Runnable()
                        {
                            public void run()
                            {
                                WifiCallback.this.isUnregisteredCompletely = true;
                            }
                        });
                    } else if (this.timeCount >= WifiCallback.this.curTimeout)
                    {
                        cancel();
                        WifiCallback.this.handler.post(new Runnable()
                        {
                            public void run()
                            {
                                if (WifiCallback.this.unregisterMe())
                                {
                                    WifiCallback.this.onTimeout();
                                }
                                WifiCallback.this.isUnregisteredCompletely = true;
                            }
                        });
                    }
                }
            }, 100L, 100L);
        }
    }

    public boolean unregisterMe()
    {
        this.isInitialNetworkAction = true;
        this.prevNetworkDetailed = null;
        if (this.curTimeout > 0)
            this.isDoneForAutoUnregisterActions = true;
        else
            this.isUnregisteredCompletely = true;
        this.isUnregistered = true;
        try
        {
            this.context.unregisterReceiver(this.receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            Logger.logW(WifiCallback.class, "unregister receiver failed.", e);
        }
        return false;
    }
}
