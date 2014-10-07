package com.greatwall.util.command;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

class CommandFactory extends AbstractCommandFactory
{
    private static CommandFactory mCommandFactory;
    private static ConcurrentHashMap<String, ICommand> mCommandMap = new ConcurrentHashMap<String, ICommand>();

    public static CommandFactory getInstance()
    {

        if (null == mCommandFactory)
        {
            synchronized (CommandFactory.class)
            {
                if (null == mCommandFactory)
                {
                    try
                    {
                        Class<?> cls = Class.forName(CommandFactory.class.getName());
                        Constructor<?> constructor = cls.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        mCommandFactory = (CommandFactory) constructor.newInstance();
                    } catch (Exception e)
                    {
                    }
                }
            }
        }

        return mCommandFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ICommand> T createCommand(Class<T> cls) throws CommandException
    {

        ICommand command = null;
        if (null != cls)
        {
            command = mCommandMap.get(cls.getName());
            if (null == command)
            {
                try
                {
                    command = (ICommand) Class.forName(cls.getName()).newInstance();
                } catch (Exception e)
                {
                    throw new CommandException(e);
                }

                mCommandMap.put(command.getClass().getName(), command);
            }
        } else
        {
            throw new CommandException("Illage argument Class<T> is" + cls);
        }

        return (T) command;
    }

}
