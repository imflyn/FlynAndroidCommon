package com.greatwall.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.greatwall.app.Application;
import com.greatwall.app.manager.ActivityManager;
import com.greatwall.app.manager.UIListenerManager;
import com.greatwall.ui.interfaces.UIListener;

public abstract class BaseActionBarActivity extends ActionBarActivity implements UIListener
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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.putInt("theme", theme);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    public static class MyTabListener<T extends Fragment> implements TabListener
    {
        private Fragment       mFragment;
        private final Activity mActivity;
        private final String   mTag;
        private final Class<T> mClass;

        /**
         * Constructor used each time a new tab is created.
         * 
         * @param activity
         *            The host Activity, used to instantiate the fragment
         * @param tag
         *            The identifier tag for the fragment
         * @param clz
         *            The fragment's Class, used to instantiate the fragment
         */
        public MyTabListener(Activity activity, String tag, Class<T> clz)
        {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /* The following are each of the ActionBar.TabListener callbacks */
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            // Check if the fragment is already initialized
            if (mFragment == null)
            {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else
            {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            if (mFragment != null)
            {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
            // User selected the already selected tab. Usually do nothing.
        }
    }
}
