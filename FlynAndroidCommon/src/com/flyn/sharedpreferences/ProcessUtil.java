
package com.flyn.sharedpreferences;

import android.app.ActivityManager;
import android.content.Context;

public class ProcessUtil
{

    /**
     * 获取当前的进程名
     * 
     * */
    public static String getCurrentProcessName(Context context)
    {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses())
        {
            if (appProcess.pid == pid)
            {
                return appProcess.processName;
            }
        }
        return null;
    }
}
