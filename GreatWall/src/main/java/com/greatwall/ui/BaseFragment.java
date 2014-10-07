package com.greatwall.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.greatwall.app.Application;
import com.greatwall.ui.interfaces.BaseActivityController;
import com.greatwall.ui.interfaces.BaseFragmentController;

import java.io.Serializable;
import java.lang.reflect.Constructor;

public abstract class BaseFragment extends FixedOnActivityResultBugFragment
{
    protected Handler mHandler;
    protected Dialog mDialog;
    protected BaseFragmentController<?> controller;
    private View mContextView;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        initContorller();
        initView(savedInstanceState);
        setListener();
    }

    @SuppressWarnings("unchecked")
    private void initContorller()
    {
        try
        {
            int index = getClass().getName().lastIndexOf(".");
            Class<? extends BaseFragmentController<?>> clz = (Class<? extends BaseFragmentController<?>>) Class.forName(getClass().getName().substring(0, index + 1) + BaseActivityController.INFIX + getClass().getSimpleName() + BaseFragmentController.SUFFIX);
            Constructor<? extends BaseFragmentController<?>> constructor = clz.getConstructor(Fragment.class);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (mContextView == null)
        {
            mContextView = inflater.inflate(getLayoutId(), container, false);
        }
        return mContextView;
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
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.mHandler = Application.getInstance().getHandler();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (null != mContextView)
        {
            ((ViewGroup) mContextView.getParent()).removeView(mContextView);
        }
        dismissDialog();
    }

    protected void showDialog()
    {
        if (mDialog != null && !mDialog.isShowing() && !getActivity().isFinishing())
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }

    public View getContextView()
    {
        return this.mContextView != null ? this.mContextView : LayoutInflater.from(getActivity()).inflate(getLayoutId(), null);
    }

}
