package com.greatwall.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.greatwall.ui.interfaces.UpdataInterface;
import com.greatwall.util.ViewUtils;
import com.greatwall.util.WeakAsyncTask;

public abstract class BaseActivity extends Activity implements UpdataInterface
{
    private final List<ViewGroup> viewList = new ArrayList<ViewGroup>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        initView();
        setListener();
        this.asynctask.execute(getPreData());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.asynctask.cancel(true);
        ViewUtils.recycleViewGroupAndChildViews(this.viewList, true);
    }

    protected abstract int layoutId();

    protected abstract void initView();

    protected abstract void setListener();

    protected abstract Object[] getPreData();

    protected View getViewById(int id)
    {
        View view = findViewById(id);
        this.viewList.add((ViewGroup) view);
        return view;
    }

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
