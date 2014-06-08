package com.greatwall.ui;

import java.util.WeakHashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.greatwall.app.Application;
import com.greatwall.app.manager.ActivityManager;
import com.greatwall.app.manager.ThemeManager;
import com.greatwall.app.manager.UIListenerManager;
import com.greatwall.ui.interfaces.UIListener;
import com.greatwall.util.ViewUtils;
import com.greatwall.util.WeakAsyncTask;

public abstract class BaseFragmentActivity extends FragmentActivity implements UIListener
{
    private final WeakHashMap<Integer, View> viewMap = new WeakHashMap<Integer, View>();
    protected int                            theme   = 0;
    protected Handler                        mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ActivityManager.getInstance().addActivity(this);
        UIListenerManager.getInstance().addClass(this);
        super.onCreate(savedInstanceState);
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

        if (this.asynctask != null && !this.asynctask.isCancelled())
            this.asynctask.cancel(true);

        clearViewMap();
    }

    protected void clearViewMap()
    {
        ViewUtils.recycleViews(this.viewMap, true);
        this.viewMap.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected abstract int layoutId();

    protected abstract void initView(Bundle savedInstanceState);

    protected abstract void setListener();

    public final View findViewById(int id)
    {
        View view = this.viewMap.get(id);
        if (null == view)
        {
            view = findViewById(id);
            this.viewMap.put(id, view);
        }
        return view;
    }

    protected final View findViewById(View rootView, int id)
    {
        View view = this.viewMap.get(String.valueOf(id));
        if (null == view)
        {
            view = rootView.findViewById(id);
            this.viewMap.put(id, view);
        }
        return view;
    }

    protected final void doLoad(Object... objs)
    {
        this.asynctask.execute(objs);
    }

    protected final void doLoad()
    {
        this.asynctask.execute();
    }

    protected Object onLoad(Object... objs)
    {
        return null;
    }

    protected Object onLoad()
    {
        return null;
    }

    protected void onLoadFinish(Object curResult)
    {
    };

    protected void onLoadFail(Exception e)
    {

    }

    private final WeakAsyncTask<Object, Object, Object> asynctask = new WeakAsyncTask<Object, Object, Object>(this)
                                                                  {
                                                                      @Override
                                                                      protected Object doInBackgroundImpl(Object... objs) throws Exception
                                                                      {
                                                                          if (null != objs && objs.length > 1)
                                                                              return onLoad(objs);
                                                                          else
                                                                              return onLoad();
                                                                      }

                                                                      @Override
                                                                      protected void onPostExecute(Object[] objs, Object curResult)
                                                                      {
                                                                          super.onPostExecute(objs, curResult);
                                                                          onLoadFinish(curResult);
                                                                      }

                                                                      @Override
                                                                      protected void onException(Object[] objs, Exception e)
                                                                      {
                                                                          super.onException(objs, e);
                                                                      }
                                                                  };

    @Override
    public void onUpdate(Object... obj)
    {

    }

    @Override
    public void onError(Throwable error)
    {

    }

}
