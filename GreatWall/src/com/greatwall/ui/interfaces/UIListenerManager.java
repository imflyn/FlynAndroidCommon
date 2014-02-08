package com.greatwall.ui.interfaces;

import java.util.concurrent.ConcurrentHashMap;

import com.greatwall.app.AppManager;

public class UIListenerManager implements AppManager
{
    private static UIListenerManager              instance;
    private ConcurrentHashMap<String, UIListener> mUIListeners = new ConcurrentHashMap<String, UIListener>();

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

    public void addClass(UIListener cls)
    {
        if (null == cls)
            throw new IllegalStateException("Happened when addClass " + cls + " is null.");

        if (null != this.mUIListeners)
            this.mUIListeners = new ConcurrentHashMap<String, UIListener>();

        if (!this.mUIListeners.contains(cls))
            this.mUIListeners.put(cls.getClass().getName(), cls);
    }

    public void update(Class<? extends UIListener> cls, Object... obj)
    {
        if (null == this.mUIListeners)
            return;

        if (this.mUIListeners.contains(cls))
            this.mUIListeners.get(cls.getClass().getName()).onUpdate(obj);

    }

    public void error(Class<? extends UIListener> cls, Throwable error)
    {
        if (null == this.mUIListeners)
            return;

        if (this.mUIListeners.contains(cls))
            this.mUIListeners.get(cls.getClass().getName()).onError(error);
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
