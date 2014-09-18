package com.greatwall.ui.interfaces;

import android.app.Activity;

import com.greatwall.ui.BaseActivity;

public abstract class BaseActivityController<D extends BaseActivity & ControllerListener>
{
    protected D                mContext;

    public static final String PREFIX = "com.greatwall.ui.controller.";
    public static final String INFIX  = "controller.";
    public static final String SUFFIX = "Controller";

    @SuppressWarnings("unchecked")
    public BaseActivityController(Activity context)
    {
        if (!BaseActivityController.this.getClass().getSimpleName().startsWith(context.getClass().getSimpleName()))
            throw new IllegalArgumentException("wrong controller name");

        mContext = (D) context;
    }

    public void onCreate()
    {
    };

    public void onDestory()
    {
    };

    public void onStart()
    {
    };

    public void onResume()
    {
    };

    public void onStop()
    {
    };

    public void onPause()
    {
    };

}
