package com.greatwall.ui.interfaces;

import android.app.Activity;

public abstract class BaseController
{
    protected Activity         mContext;

    public static final String PREFIX = "com.greatwall.ui.controller.";
    public static final String INFIX  = "controller.";
    public static final String SUFFIX = "Controller";

    public BaseController(Activity context)
    {
        if (!BaseController.this.getClass().getSimpleName().startsWith(context.getClass().getSimpleName()))
            throw new IllegalArgumentException("wrong controller name");

        this.mContext = context;
    }

    public abstract void onCreate();

    public abstract void onDestory();

    public abstract void onStart();

    public abstract void onResume();

    public abstract void onStop();

    public abstract void onPause();

}
