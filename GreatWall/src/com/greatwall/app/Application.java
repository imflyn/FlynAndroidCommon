package com.greatwall.app;

import java.util.ArrayList;

import com.greatwall.app.manager.AppManager;

public class Application extends android.app.Application
{

    // singleton
    private static Application    mContext        = null;
    private volatile boolean      mIsRunning      = false;
    private ArrayList<AppManager> mAppManagerList = new ArrayList<AppManager>();

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = this;
    }

    public static Application getInstance()
    {
        if (mContext == null)
            throw new IllegalStateException();
        return mContext;
    }

    public void addManager(AppManager appManager)
    {
        this.mAppManagerList.add(appManager);
    }

   

    public void clear()
    {
        for (int i = 0; i < mAppManagerList.size(); i++)
        {
            mAppManagerList.get(i).onClear();
        }
    }

    public void close()
    {
        mIsRunning = false;
        for (int i = 0; i < mAppManagerList.size(); i++)
        {
            mAppManagerList.get(i).onClose();
        }
    }

}
