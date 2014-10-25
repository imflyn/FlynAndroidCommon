package com.greatwall.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;

public abstract class BaseFragment extends FixedOnActivityResultBugFragment
{

    protected BaseController<?> controller;
    protected View mContextView;
    protected Dialog mDialog;
    protected InternalHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        initController();
        super.onCreate(savedInstanceState);
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
    public void onStart()
    {
        super.onStart();
        if (null != controller)
        {
            controller.onStart();
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

        if (null != mContextView)
        {
            ((ViewGroup) mContextView.getParent()).removeView(mContextView);
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

    protected void handlerMessage(Message msg)
    {
    }

    protected static class InternalHandler extends Handler
    {

        private WeakReference<BaseFragment> mHandler;

        public InternalHandler(BaseFragment fragment)
        {
            super(Looper.getMainLooper());
            mHandler = new WeakReference<BaseFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg)
        {
            BaseFragment fragment = mHandler.get();
            if (fragment != null)
            {
                fragment.handlerMessage(msg);
            }
        }
    }
}
