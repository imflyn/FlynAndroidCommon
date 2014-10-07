package com.greatwall.app.manager;

import com.greatwall.app.Application;

public abstract class AppManager
{
    public Application mContext;

    public AppManager()
    {
        mContext = Application.getInstance();
        mContext.addManager(this);
        onInit();
    }

    public abstract void onInit();

    public abstract void onClear();

    public abstract void onClose();
}
