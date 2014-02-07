package com.greatwall.util.command;

import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

public class CommandExecutor
{

    private static final String                              TAG          = CommandExecutor.class.getName();

    private static final CommandExecutor                     instance     = new CommandExecutor();

    private final ConcurrentHashMap<String, Class<? extends ICommand>> mCommandMap  = new ConcurrentHashMap<String, Class<? extends ICommand>>();
    private boolean                                          mInitialized = false;

    public CommandExecutor()
    {
        initialize();
    }

    public static CommandExecutor getInstance()
    {
        return instance;
    }

    private void initialize()
    {
        if (!this.mInitialized)
        {
            this.mInitialized = true;
            CommandQueueManager.getInstance().initialize();
            Log.i(TAG, "CommandExecutor initialize.");
        }
    }

    public void terminateAll()
    {

    }

    public void enqueueCommand(String commandKey, Request request, AbstractResponseListener listener) throws IllegalStateException
    {
        final ICommand cmd = getCommand(commandKey);
        enqueueCommand(cmd, request, listener);
    }

    public void enqueueCommand(ICommand command, Request request, AbstractResponseListener listener) throws IllegalStateException
    {
        if (command != null)
        {
            command.setRequest(request);
            command.setResponseListener(listener);
            CommandQueueManager.getInstance().addQueue(command);
        }
    }

    public void enqueueCommand(ICommand command, Request request) throws IllegalStateException
    {
        enqueueCommand(command, null, null);
    }

    public void enqueueCommand(ICommand command) throws IllegalStateException
    {
        enqueueCommand(command, null);
    }

    private ICommand getCommand(String commandKey)  throws IllegalStateException
    {
        ICommand commond = null;

        if (this.mCommandMap.containsKey(commandKey))
        {
            Class<? extends ICommand> cmdClass = this.mCommandMap.get(commandKey);
            if (cmdClass != null)
            {
                int modifiers = cmdClass.getModifiers();
                if ((modifiers & Modifier.ABSTRACT) == 0 && (modifiers & Modifier.INTERFACE) == 0)
                {
                    try
                    {
                        commond = CommondFactory.getInstance().createCommond(cmdClass);
                    } catch (Exception e)
                    {
                        throw new IllegalStateException("No such command " + commandKey);
                    }
                } else
                {
                    throw new IllegalStateException("No such command " + commandKey);
                }
            }
        }

        return commond;
    }

    public void registerCommand( Class<? extends ICommand> command)
    {
        if (command != null)
        {
            this.mCommandMap.put(command.getName(), command);
        }
    }

    public void unregisterCommand(Class<? extends ICommand> command)
    {
        this.mCommandMap.remove(command.getName());
    }
}
