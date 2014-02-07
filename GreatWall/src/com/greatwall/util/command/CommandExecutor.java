package com.greatwall.util.command;

import java.lang.reflect.Modifier;
import java.util.HashMap;

public class CommandExecutor
{

    private static final String                              TAG          = CommandExecutor.class.getName();

    private static final CommandExecutor                     instance     = new CommandExecutor();

    private final HashMap<String, Class<? extends ICommand>> mCommandMap  = new HashMap<String, Class<? extends ICommand>>();
    private boolean                                          mInitialized = false;

    public CommandExecutor()
    {
        ensureInitialized();
    }

    public static CommandExecutor getInstance()
    {
        return instance;
    }

    public void ensureInitialized()
    {
        if (!this.mInitialized)
        {
            this.mInitialized = true;
            CommandQueueManager.getInstance().initialize();
        }
    }

    public void terminateAll()
    {

    }

    public void enqueueCommand(String commandKey, Request request, AbstractResponseListener listener) throws RuntimeException
    {
        final ICommand cmd = getCommand(commandKey);
        enqueueCommand(cmd, request, listener);
    }

    public void enqueueCommand(ICommand command, Request request, AbstractResponseListener listener) throws RuntimeException
    {
        if (command != null)
        {
            command.setRequest(request);
            command.setResponseListener(listener);
            CommandQueueManager.getInstance().enqueue(command);
        }
    }

    public void enqueueCommand(ICommand command, Request request) throws RuntimeException
    {
        enqueueCommand(command, null, null);
    }

    public void enqueueCommand(ICommand command) throws RuntimeException
    {
        enqueueCommand(command, null);
    }

    private ICommand getCommand(String commandKey) throws RuntimeException
    {
        ICommand rv = null;

        if (this.mCommandMap.containsKey(commandKey))
        {
            Class<? extends ICommand> cmd = this.mCommandMap.get(commandKey);
            if (cmd != null)
            {
                int modifiers = cmd.getModifiers();
                if ((modifiers & Modifier.ABSTRACT) == 0 && (modifiers & Modifier.INTERFACE) == 0)
                {
                    try
                    {
                        rv = cmd.newInstance();
                    } catch (Exception e)
                    {
                        throw new RuntimeException("no such command " + commandKey);
                    }
                } else
                {
                    throw new RuntimeException("no such command " + commandKey);
                }
            }
        }

        return rv;
    }

    public void registerCommand(String commandKey, Class<? extends ICommand> command)
    {
        if (command != null)
        {
            this.mCommandMap.put(commandKey, command);
        }
    }

    public void unregisterCommand(String commandKey)
    {
        this.mCommandMap.remove(commandKey);
    }
}
