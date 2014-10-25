package com.greatwall.sharedpreferences;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPreferenceFactory
{

    // singleton registry
    private static ConcurrentHashMap<Class<? extends AbstractSharedPreference>, AbstractSharedPreference> cache = new ConcurrentHashMap<Class<? extends AbstractSharedPreference>, AbstractSharedPreference>();

    public static <T extends AbstractSharedPreference> AbstractSharedPreference getSharedPreference(Context context, Class<T> clazz)
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
