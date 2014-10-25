package com.greatwall.ui;

import android.app.Activity;
import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.greatwall.app.Application;
import com.greatwall.app.manager.ActivityManager;

import java.lang.reflect.Constructor;

public abstract class BaseActivity extends FragmentActivity
{
    protected Dialog mDialog;
    protected BaseController<?> controller;
    protected Activity mContext;
    protected Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去除标题栏
        ActivityManager.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mHandler = Application.getInstance().getHandler();
        mContext = this;
        if (layoutId() > 0)
        {
            setContentView(layoutId());
        }
        initController();
        findViews();
        initView(savedInstanceState);
        setListener();
    }

    private void initController()
    {
        try
        {

            String pack = ((Object) this).getClass().getPackage().getName().replaceFirst("package", "").replaceAll(" ", "").replace("activity", "").replace("fragment", "") + BaseController.NAME;

            Class<? extends BaseController<?>> clz = (Class<? extends BaseController<?>>) Class.forName(pack + ((Object) this).getClass().getSimpleName() + BaseController.SUFFIX);
            Constructor<? extends BaseController<?>> constructor = clz.getConstructor(((Object) this).getClass());
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

    protected abstract BaseController<?> getController();

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
