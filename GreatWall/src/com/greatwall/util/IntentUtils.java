package com.greatwall.util;

import android.content.ComponentName;
import android.content.Intent;

public class IntentUtils
{

    public static Intent getSettingIntent()
    {
        Intent intent = null;
        // 判断手机系统的版本 即API大于10 就是3.0或以上版本
        if (android.os.Build.VERSION.SDK_INT > 10)
        {
            intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        } else
        {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        return intent;
    }
}
