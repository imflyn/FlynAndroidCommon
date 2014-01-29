package com.greatwall.ui;

import android.app.Activity;
import android.os.Bundle;

import com.greatwall.util.WeakAsyncTask;

public abstract class BaseActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        initView();
        setListener();
        this.asynctask.execute(getPreData());
    }

    protected abstract int layoutId();

    protected abstract void initView();

    protected abstract void setListener();

    protected abstract Object[] getPreData();

    protected Object onLoad(Object... objs)
    {
        return null;
    }

    protected Object onLoad()
    {
        return null;
    }

    protected void onLoadFinish(Object[] objs)
    {
    };

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
                                                                          onLoadFinish(objs);
                                                                      }

                                                                      @Override
                                                                      protected void onException(Object[] objs, Exception e)
                                                                      {
                                                                          super.onException(objs, e);
                                                                      }
                                                                  };
}
