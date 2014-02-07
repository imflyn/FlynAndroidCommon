package com.greatwall.util.command;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

class CommondFactory extends AbstractCommondFactory
{
    private static CommondFactory mCommondFactory;
    private static ConcurrentHashMap<String, ICommand> mCommondMap  = new ConcurrentHashMap<String, ICommand>();

    public static CommondFactory getInstance()
    {

        if (null == mCommondFactory)
        {
            synchronized (CommondFactory.class)
            {
                if (null == mCommondFactory)
                {
                    try
                    {
                        Class<?> cls = Class.forName(CommondFactory.class.getName());
                        Constructor<?> constructor = cls.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        mCommondFactory = (CommondFactory) constructor.newInstance();
                    } catch (Exception e)
                    {
                    }
                }
            }
        }

        return mCommondFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ICommand> T createCommond(Class<T> cls)
    {

        ICommand commond = null;
        try
        {
            commond = (ICommand) Class.forName(cls.getName()).newInstance();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return (T) commond;
    }
  

}
