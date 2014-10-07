package com.greatwall.util.command;

import java.lang.reflect.Modifier;

public class CommandExecutor
{

    private static final CommandExecutor instance = new CommandExecutor();
    private boolean mInitialized = false;

    private CommandExecutor()
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
        }
    }

    public void enqueueCommand(Class<? extends ICommand> cmdClass, Request request, AbstractResponseListener listener) throws CommandException
    {
        ICommand cmd = getCommand(cmdClass);
        enqueueCommand(cmd, request, listener);
    }

    public void enqueueCommand(ICommand command, Request request, AbstractResponseListener listener)
    {
        if (command != null)
        {
            command.setRequest(request);
            command.setResponseListener(listener);
            CommandQueueManager.getInstance().enqueue(command);
        }
    }

    public void enqueueCommand(ICommand command, Request request)
    {
        enqueueCommand(command, null, null);
    }

    public void enqueueCommand(ICommand command)
    {
        enqueueCommand(command, null);
    }

    public void cancelCommand(Class<? extends ICommand> cmdClass, Request request)
    {

    }

    private ICommand getCommand(Class<? extends ICommand> cmdClass) throws CommandException
    {
        ICommand command = null;

        if (cmdClass != null)
        {
            int modifiers = cmdClass.getModifiers();
            if ((modifiers & Modifier.ABSTRACT) == 0 && (modifiers & Modifier.INTERFACE) == 0)
            {
                try
                {
                    command = CommandFactory.getInstance().createCommand(cmdClass);
                } catch (Exception e)
                {
                    throw new CommandException("No such command " + cmdClass.getName());
                }
            } else
            {
                throw new CommandException("No such command " + cmdClass.getName());
            }
        }

        return command;
    }

}
