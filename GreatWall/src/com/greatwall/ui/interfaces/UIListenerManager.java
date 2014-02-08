package com.greatwall.ui.interfaces;

import java.util.ArrayList;

import com.greatwall.app.AppManager;

public class UIListenerManager implements AppManager
{
    private static UIListenerManager instance;
    private ArrayList<UIListener>    mUIListeners;

    public static UIListenerManager getInstance()
    {
        if (instance == null)
        {
            synchronized (UIListenerManager.class)
            {
                if (instance == null)
                {
                    instance = new UIListenerManager();
                }
            }
        }
        return instance;
    }

    public void addClass(UIListener listener)
    {
        if (null == listener)
            throw new IllegalStateException("Happened when add Class " + listener + " is null.");

        if (null != this.mUIListeners)
            this.mUIListeners = new ArrayList<UIListener>(5);

        this.mUIListeners.remove(listener);
        this.mUIListeners.add(listener);
    }

    public void removeClass(UIListener listener)
    {
        if (null == listener)
            throw new IllegalStateException("Happened when remove Class " + listener + " is null.");

        if (null != this.mUIListeners)
            this.mUIListeners = new ArrayList<UIListener>(5);

            this.mUIListeners.remove(listener);
    }

    public void update(Class<? extends UIListener> cls, Object... obj)
    {
        UIListener listener = getListener(cls);
        if (null != listener)
            listener.onUpdate(obj);

    }

    public void error(Class<? extends UIListener> cls, Throwable error)
    {
        UIListener listener = getListener(cls);
        if (null != listener)
            listener.onError(error);
    }

    private UIListener getListener(Class<? extends UIListener> cls)
    {
        UIListener listener = null;
        if (null == this.mUIListeners || cls == null)
            return listener;

        String className = cls.getName();
        for (int i = 0; i < mUIListeners.size(); i++)
        {
            UIListener uiListener = mUIListeners.get(i);
            if (className.equals(uiListener.getClass().getName()))
            {
                listener = uiListener;
            }
        }

        return listener;
    }

    @Override
    public void onClose()
    {
        if (null != mUIListeners)
        {
            mUIListeners.clear();
            mUIListeners = null;
        }

    }

}
