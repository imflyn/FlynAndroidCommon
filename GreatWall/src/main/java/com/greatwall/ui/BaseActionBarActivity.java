package com.greatwall.ui;

import android.app.Activity;
import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.greatwall.app.manager.ActivityManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;

public abstract class BaseActionBarActivity extends ActionBarActivity
{
    protected Activity context;
    protected Handler myHandler;
    protected View rootView;
    protected Dialog mDialog;
    protected BaseController<?> controller;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ActivityManager.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        myHandler = new InternalHandler(this);
        context = this;
        if (layoutId() > 0)
        {
            setContentView(layoutId());
        }
        initController();
        findViews();
        initView(savedInstanceState);
        setListener();
    }

    @SuppressWarnings("unchecked")
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

    protected abstract void findViews();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

    protected void handlerMessage(Message msg)
    {

    }

    protected static class InternalHandler extends Handler
    {

        private WeakReference<BaseActionBarActivity> mHandler;

        public InternalHandler(BaseActionBarActivity activity)
        {
            super(Looper.getMainLooper());
            mHandler = new WeakReference<BaseActionBarActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            BaseActionBarActivity activity = mHandler.get();
            if (activity != null)
            {
                activity.handlerMessage(msg);
            }
        }
    }

    public static class MyTabListener<T extends Fragment> implements TabListener
    {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private Fragment mFragment;

        /**
         * Constructor used each time a new tab is created.
         *
         * @param activity The host Activity, used to instantiate the fragment
         * @param tag      The identifier tag for the fragment
         * @param clz      The fragment's Class, used to instantiate the fragment
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
