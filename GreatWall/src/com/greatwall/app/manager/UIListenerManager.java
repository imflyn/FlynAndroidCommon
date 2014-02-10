package com.greatwall.app.manager;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;

import com.greatwall.ui.interfaces.UIListener;

public class UIListenerManager extends AppManager
{
    private static UIListenerManager                                    instance = new UIListenerManager();
    private HashMap<Class<? extends UIListener>, ArrayList<UIListener>> mUIListeners;
    private Handler                                                     handler;

    private UIListenerManager()
    {
        super();
    }

    public static UIListenerManager getInstance()
    {
        return instance;
    }

    public void addClass(UIListener listener)
    {
        if (null == listener)
            throw new IllegalStateException("Happened when add argument " + listener + " is null.");

        if (null == mUIListeners)
            mUIListeners = new HashMap<Class<? extends UIListener>, ArrayList<UIListener>>();

        getListeners(listener.getClass()).add(listener);
    }

    public void removeClass(UIListener listener)
    {
        if (null == listener)
            throw new IllegalStateException("Happened when remove Class " + listener + " is null.");

        if (null == this.mUIListeners)
            return;
        getListeners(listener.getClass()).remove(listener);
    }

    public void update(final Class<UIListener> cls, final Object... obj)
    {
        handler.post((new Runnable()
        {
            @Override
            public void run()
            {
                for (UIListener listener : getListeners(cls))
                {
                    listener.onUpdate(obj);
                }
            }
        }));
    }

    public void error(final Class<UIListener> cls, final Throwable error)
    {
        handler.post((new Runnable()
        {
            @Override
            public void run()
            {
                for (UIListener listener : getListeners(cls))
                {
                    listener.onError(error);
                }
            }
        }));
    }

    private ArrayList<UIListener> getListeners(Class<? extends UIListener> cls)
    {
        ArrayList<UIListener> collection = mUIListeners.get(cls);
        if (collection == null)
        {
            collection = new ArrayList<UIListener>();
            mUIListeners.put(cls, collection);
        }
        return collection;
    }

    @Override
    public void onClose()
    {
        if (null != mUIListeners)
        {
            mUIListeners.clear();
            mUIListeners = null;
            instance = null;
            handler = null;
        }

    }

    @Override
    public void onInit()
    {
        handler = new Handler();
    }

    @Override
    public void onClear()
    {
        if (null != mUIListeners)
            mUIListeners.clear();

    }

}
