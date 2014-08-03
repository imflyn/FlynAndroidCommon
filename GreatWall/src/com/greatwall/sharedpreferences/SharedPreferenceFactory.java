package com.greatwall.sharedpreferences;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

public class SharedPreferenceFactory 
{

    // singleton registry
    private static ConcurrentHashMap<Class<? extends AbstractSharedPreference>, AbstractSharedPreference> cache = new ConcurrentHashMap<Class<? extends AbstractSharedPreference>, AbstractSharedPreference>();

    public static AbstractSharedPreference getSharedPreference(Context context, Class<? extends AbstractSharedPreference> clazz)
    {
        if (context == null || clazz == null)
        {
            return null;
        }
        AbstractSharedPreference asp = cache.get(clazz);
        if (asp == null)
        {
            synchronized (SharedPreferenceFactory.class)
            {
                asp = cache.get(clazz);
                if (asp == null)
                {
                    try
                    {
                        Constructor<? extends AbstractSharedPreference> constructor = clazz.getConstructor(Context.class);
                        asp = constructor.newInstance(context);
                    } catch (Throwable e)
                    {
                        throw new IllegalArgumentException("can not instantiate class:" + clazz, e);
                    }
                    cache.put(clazz, asp);
                }
            }
        }
        return asp;
    }
    
    public static void clear()
    {
        cache.clear();
    }

}
