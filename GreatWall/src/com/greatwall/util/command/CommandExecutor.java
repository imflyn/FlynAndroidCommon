package com.greatwall.util.command;

import java.lang.reflect.Modifier;

import android.util.Log;

public class CommandExecutor
{

    private static final String          TAG          = CommandExecutor.class.getName();

    private static final CommandExecutor instance     = new CommandExecutor();

    private boolean                      mInitialized = false;

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

    private ICommand getCommand(Class<? extends ICommand> cmdClass) throws CommandException
    {
        ICommand commond = null;

        if (cmdClass != null)
        {
            int modifiers = cmdClass.getModifiers();
            if ((modifiers & Modifier.ABSTRACT) == 0 && (modifiers & Modifier.INTERFACE) == 0)
            {
                try
                {
                    commond = CommandFactory.getInstance().createCommand(cmdClass);
                } catch (Exception e)
                {
                    throw new CommandException("No such command " + cmdClass.getName());
                }
            } else
            {
                throw new CommandException("No such command " + cmdClass.getName());
            }
        }

        return commond;
    }

}
