package com.greatwall.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.greatwall.app.ActivityManager;
import com.greatwall.ui.interfaces.UIListener;
import com.greatwall.ui.interfaces.UIListenerManager;
import com.greatwall.util.ViewUtils;
import com.greatwall.util.WeakAsyncTask;

public abstract class BaseActivity extends Activity implements UIListener
{
    private final List<ViewGroup> viewList = new ArrayList<ViewGroup>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ActivityManager.getInstance().addActivity(this);
        UIListenerManager.getInstance().addClass(this);
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        initView();
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

        ViewUtils.recycleViewGroupAndChildViews(this.viewList, true);
        this.viewList.clear();
    }

    protected abstract int layoutId();

    protected abstract void initView();

    protected abstract void setListener();

    protected final View getViewById(int id)
    {
        View view = findViewById(id);
        this.viewList.add((ViewGroup) view);
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
