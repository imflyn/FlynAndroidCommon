package com.flyn.net.wifi.support;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class ReenableAllApsWhenNetworkStateChanged
{
    public static void schedule(Context ctx)
    {
        ctx.startService(new Intent(ctx, BackgroundService.class));
    }

    private static boolean reenableAllAps(Context ctx)
    {
        WifiManager wifiMgr = (WifiManager) ctx.getSystemService("wifi");
        List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations == null)
            return false;
        for (WifiConfiguration config : configurations)
        {
            wifiMgr.enableNetwork(config.networkId, false);
        }
        return true;
    }

    public static class BackgroundService extends Service
    {
        private boolean           mReenabled;
        private BroadcastReceiver mReceiver = new BroadcastReceiver()
                                            {
                                                public void onReceive(Context context, Intent intent)
                                                {
                                                    String action = intent.getAction();
                                                    if ("android.net.wifi.STATE_CHANGE".equals(action))
                                                    {
                                                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                                                        NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
                                                        switch (detailed)
                                                        {
                                                            case DISCONNECTED:
                                                            case DISCONNECTING:
                                                            case SCANNING:
                                                                return;
                                                            case FAILED:
                                                            case IDLE:
                                                            case OBTAINING_IPADDR:
                                                        }
                                                        if (!ReenableAllApsWhenNetworkStateChanged.BackgroundService.this.mReenabled)
                                                        {
                                                            ReenableAllApsWhenNetworkStateChanged.BackgroundService.this.mReenabled = true;
                                                            ReenableAllApsWhenNetworkStateChanged.schedule(context);
                                                            ReenableAllApsWhenNetworkStateChanged.BackgroundService.this.stopSelf();
                                                        }
                                                    }
                                                }
                                            };
        private IntentFilter      mIntentFilter;

        public IBinder onBind(Intent intent)
        {
            return null;
        }

        public void onCreate()
        {
            super.onCreate();
            this.mReenabled = false;
            this.mIntentFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
            registerReceiver(this.mReceiver, this.mIntentFilter);
        }

        public void onDestroy()
        {
            super.onDestroy();
            unregisterReceiver(this.mReceiver);
        }
    }
}
