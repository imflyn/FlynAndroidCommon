package com.greatwall.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.greatwall.app.Application;

import java.io.Serializable;
import java.lang.reflect.Constructor;

public abstract class BaseFragment extends FixedOnActivityResultBugFragment
{

    protected BaseController<?> controller;
    protected View mRootView;
    protected Dialog mDialog;
    protected Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mHandler = Application.getInstance().getHandler();
        initController();
    }

    @SuppressWarnings("unchecked")
    private void initController()
    {
        try
        {

            String pack = ((Object) this).getClass().getPackage().getName().replaceFirst("package", "").replaceAll(" ", "").replace("activity", "") + BaseController.NAME;
            Class<? extends BaseController<?>> clz = (Class<? extends BaseController<?>>) Class.forName(pack + ((Object) this).getClass().getSimpleName() + BaseController.SUFFIX);
            Constructor<? extends BaseController<?>> constructor = clz.getConstructor(((Object) this).getClass());
            controller = constructor.newInstance(getActivity());
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        if (null == mRootView)
        {
            mRootView = inflater.inflate(getLayoutId(), container, false);
            initView(savedInstanceState);
            setListener();
        }
        return mRootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (null != controller)
        {
            controller.onStart();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (null != controller)
        {
            controller.onResume();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (null != controller)
        {
            controller.onPause();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (null != controller)
        {
            controller.onStop();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (null != controller)
        {
            controller.onDestory();
        }

        if (null != mRootView)
        {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
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


    protected abstract int getLayoutId();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

    public Serializable getSerializable()
    {
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            return bundle.getSerializable("SERIALIZABLE");
        }
        return null;
    }

    public void setSerializable(Serializable serializable)
    {
        Bundle bundle = getArguments();
        if (bundle == null)
        {
            bundle = new Bundle();
        }
        bundle.putSerializable("SERIALIZABLE", serializable);
        super.setArguments(bundle);
    }

    @Override
    public void setArguments(Bundle args)
    {
        throw new UnsupportedOperationException("use setSerializable(serializable) instead.");
    }

    public void refresh()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this);
        ft.attach(this);
        ft.commitAllowingStateLoss();
    }


}
