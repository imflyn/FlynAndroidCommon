package com.greatwall.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
import com.greatwall.ui.interfaces.BaseController;

public abstract class BaseFragmentActivity<T extends BaseController> extends FragmentActivity
{
    protected Context mContext;
    protected int     theme = 0;
    protected Handler mHandler;
    protected View    rootView;
    protected Dialog  mDialog;
    protected T       controller;

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
            setContentView(layoutId());
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
            Class<? extends BaseController> clz = (Class<? extends BaseController>) Class.forName(getClass().getName().substring(0, index + 1) + BaseController.INFIX + getClass().getSimpleName()
                    + BaseController.SUFFIX);
            Constructor<? extends BaseController> constructor = clz.getConstructor(Activity.class);
            controller = (T) constructor.newInstance(this);
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        if (null != controller)
            controller.onCreate();
    }

    protected T getController()
    {
        return controller;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (null != controller)
            controller.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (null != controller)
            controller.onResume();
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
            controller.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (null != controller)
            controller.onStop();
    }

    @Override
    protected void onDestroy()
    {
        ActivityManager.getInstance().removeActivity(this);
        super.onDestroy();
        if (null != controller)
            controller.onDestory();
        dismissDialog();
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
        }
    }

    protected abstract int layoutId();

    protected abstract void findViews();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

}
