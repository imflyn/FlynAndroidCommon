package com.flyn.pm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public final class PackageMgr
{
    public static List<ApplicationInfo> getInstalledApplications(Context context, boolean isSort)
    {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
        if (isSort)
            Collections.sort(applicationInfos, new ApplicationInfo.DisplayNameComparator(pm));
        return applicationInfos;
    }

    public static ApplicationInfo getInstalledApplication(Context context, String packageName)
    {
        try
        {
            return context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException localNameNotFoundException)
        {
        }
        return null;
    }

    public static List<PackageInfo> getInstalledPackages(Context context)
    {
        return context.getPackageManager().getInstalledPackages(0);
    }

    public static PackageInfo getInstalledPackage(Context context, String packageName)
    {
        try
        {
            return context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException localNameNotFoundException)
        {
        }
        return null;
    }

    public static List<ResolveInfo> queryIntentActivities(Context context, Intent intent, boolean isSort)
    {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (isSort)
            Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        return resolveInfos;
    }

    public static List<ResolveInfo> queryIntentPackageActivities(Context context, Intent intent, String packageName, boolean isSort)
    {
        List<ResolveInfo> resolveInfos = queryIntentActivities(context, intent, false);
        List<ResolveInfo> filterResolveInfos = new ArrayList<ResolveInfo>();
        for (ResolveInfo resolveInfo : resolveInfos)
        {
            if (!resolveInfo.activityInfo.packageName.equals(packageName))
                continue;
            filterResolveInfos.add(resolveInfo);
        }
        if (isSort)
            Collections.sort(filterResolveInfos, new ResolveInfo.DisplayNameComparator(context.getPackageManager()));
        return filterResolveInfos;
    }

    public static Intent getLaunchIntentForPackage(Context context, String packageName)
    {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }
}
