package com.greatwall.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

import com.greatwall.ui.interfaces.UILIstener;
import com.greatwall.util.ViewUtils;
import com.greatwall.util.WeakAsyncTask;

public abstract class BaseFragmentActivity extends FragmentActivity implements UILIstener
{
    private final List<ViewGroup> viewList = new ArrayList<ViewGroup>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        initView();
        setListener();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        if(this.asynctask!=null&&!this.asynctask.isCancelled())
        this.asynctask.cancel(true);
        
        ViewUtils.recycleViewGroupAndChildViews(this.viewList, true);
        this.viewList.clear();
    }

    protected abstract int layoutId();

    protected abstract void initView();

    protected abstract void setListener();

    protected final View getViewById(int id)
    {
        final View view = findViewById(id);
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
}
