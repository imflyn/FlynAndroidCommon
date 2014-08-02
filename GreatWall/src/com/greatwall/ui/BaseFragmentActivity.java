package com.greatwall.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

import com.greatwall.app.Application;
import com.greatwall.app.manager.ActivityManager;
import com.greatwall.app.manager.ThemeManager;
import com.greatwall.app.manager.UIListenerManager;
import com.greatwall.ui.interfaces.UIListener;
import com.greatwall.util.ViewUtils;

public abstract class BaseFragmentActivity extends FragmentActivity implements UIListener
{
    protected Context mContext;
    protected int     theme = 0;
    protected Handler mHandler;
    protected View    rootView;
    protected Dialog  mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ActivityManager.getInstance().addActivity(this);
        UIListenerManager.getInstance().addClass(this);
        super.onCreate(savedInstanceState);
        this.mContext = this;
        this.mHandler = Application.getInstance().getHandler();
        if (savedInstanceState == null)
        {
            theme = ThemeManager.getInstance().getCurrentThemeStyle();
        } else
        {
            theme = savedInstanceState.getInt("theme");
        }
        setTheme(theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (layoutId() > 0)
            setContentView(layoutId());
        initView(savedInstanceState);
        setListener();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (theme != ThemeManager.getInstance().getCurrentThemeStyle())
        {
            reload();
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

    private void reload()
    {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void startActivity(Intent intent)
    {
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onDestroy()
    {
        ActivityManager.getInstance().removeActivity(this);
        UIListenerManager.getInstance().removeClass(this);
        super.onDestroy();

        ViewUtils.recycleView(rootView, false);
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

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);

    }

    protected abstract int layoutId();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

    @Override
    public void onUpdate(Object... obj)
    {

    }

    @Override
    public void onError(Throwable error)
    {

    }

}
