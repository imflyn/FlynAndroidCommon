package com.greatwall.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

import com.greatwall.app.Application;
import com.greatwall.app.manager.ActivityManager;
import com.greatwall.ui.interfaces.BaseActivityController;

import java.lang.reflect.Constructor;

public abstract class BaseActivity extends FragmentActivity
{
    protected Context mContext;
    protected int theme = 0;
    protected Handler mHandler;
    protected View rootView;
    protected Dialog mDialog;
    protected BaseActivityController<?> controller;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ActivityManager.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        this.mContext = this;
        this.mHandler = Application.getInstance().getHandler();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initContorller();
        if (layoutId() > 0)
        {
            setContentView(layoutId());
        }
        findViews();
        initView(savedInstanceState);
        setListener();
    }

    @SuppressWarnings("unchecked")
    private void initContorller()
    {
        try
        {
            int index = getClass().getName().lastIndexOf(".");
            Class<? extends BaseActivityController<?>> clz = (Class<? extends BaseActivityController<?>>) Class.forName(getClass().getName().substring(0, index + 1) + BaseActivityController.INFIX + getClass().getSimpleName() + BaseActivityController.SUFFIX);
            Constructor<? extends BaseActivityController<?>> constructor = clz.getConstructor(Activity.class);
            controller = constructor.newInstance(this);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != controller)
        {
            controller.onCreate();
        }
    }

    protected abstract BaseActivityController<?> getController();

    @Override
    protected void onStart()
    {
        super.onStart();
        if (null != controller)
        {
            controller.onStart();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (null != controller)
        {
            controller.onResume();
        }
    }

    public void setContentView(int resId)
    {
        rootView = View.inflate(this, resId, null);
        if (null != rootView)
        {
            setContentView(rootView);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (null != controller)
        {
            controller.onPause();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (null != controller)
        {
            controller.onStop();
        }
    }

    @Override
    protected void onDestroy()
    {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
        if (null != controller)
        {
            controller.onDestory();
        }
        dismissDialog();
        rootView = null;
        controller = null;
    }

    protected void showDialog()
    {
        if (mDialog != null && !mDialog.isShowing() && !isFinishing())
        {
            mDialog.show();
        }
    }

    protected void dismissDialog()
    {
        if (null != mDialog && mDialog.isShowing())
        {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected abstract int layoutId();

    protected abstract void findViews();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

}
