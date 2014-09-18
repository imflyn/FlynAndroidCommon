package com.greatwall.ui.interfaces;

import android.support.v4.app.Fragment;

import com.greatwall.ui.BaseFragment;

public class BaseFragmentController<D extends BaseFragment & ControllerListener>
{
    protected D                mContext;

    public static final String PREFIX = "com.greatwall.ui.controller.";
    public static final String INFIX  = "controller.";
    public static final String SUFFIX = "Controller";

    @SuppressWarnings("unchecked")
    public BaseFragmentController(Fragment fragment)
    {
        if (!BaseFragmentController.this.getClass().getSimpleName().startsWith(fragment.getClass().getSimpleName()))
            throw new IllegalArgumentException("wrong controller name");

        mContext = (D) fragment;
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
