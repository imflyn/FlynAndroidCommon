package com.greatwall.ui;

import java.io.Serializable;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.greatwall.app.Application;

public abstract class BaseFragment extends FixedOnActivityResultBugFragment
{
    protected View    mContextView;
    private boolean   isViewDetached = false;
    protected Handler mHandler;
    protected Dialog  mDialog;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return true;
            }
        });
        initView(savedInstanceState);
        setListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(getLayoutId(), container, false);
        this.mContextView = view;
        return view;
    }

    protected abstract int getLayoutId();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

    public void setSerializable(Serializable serializable)
    {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = new Bundle();
        bundle.putSerializable("SERIALIZABLE", serializable);
        super.setArguments(bundle);
    }

    public Serializable getSerializable()
    {
        Bundle bundle = getArguments();
        if (bundle != null)
        {
            return bundle.getSerializable("SERIALIZABLE");
        }
        return null;
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
        this.isViewDetached = true;

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
        this.isViewDetached = false;

    }

    public boolean isViewDetached()
    {
        return this.isViewDetached;
    }

    public View getContextView()
    {
        return this.mContextView != null ? this.mContextView : LayoutInflater.from(getActivity()).inflate(getLayoutId(), null);
    }

}
