package com.greatwall.util.command;

import java.lang.reflect.Modifier;
import java.util.HashMap;


public class CommandExecutor
{
    private final HashMap<String, Class<? extends ICommand>> commands    = new HashMap<String, Class<? extends ICommand>>();

    private static final CommandExecutor                     instance    = new CommandExecutor();
    private static final String                              TAG         = CommandExecutor.class.getName();
    private boolean                                          initialized = false;

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
        if (!initialized)
        {
            initialized = true;
            CommandQueueManager.getInstance().initialize();
        }
    }

    /** 所有命令终止或标记为结束 */
    public void terminateAll()
    {

    }

    /**
     * 命令入列
     * 
     * @param commandKey
     *            命令ID
     * @param request
     *            提交的参数
     * @param listener
     *            响应监听器
     * @throws RuntimeException
     */
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

        if (commands.containsKey(commandKey))
        {
            Class<? extends ICommand> cmd = commands.get(commandKey);
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
            commands.put(commandKey, command);
        }
    }

    public void unregisterCommand(String commandKey)
    {
        commands.remove(commandKey);
    }
}
