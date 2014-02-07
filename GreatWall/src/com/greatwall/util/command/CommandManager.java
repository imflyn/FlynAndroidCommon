package com.greatwall.util.command;


public class CommandManager extends AbstractResponseListener
{
    private CommandExecutor mCommandExecutor;

    public void registerCommand( Class<? extends ICommand> command)
    {
        if (command != null)
        {
            this.mCommandExecutor.registerCommand( command);
        }
    }


    public void unregisterCommand( Class<? extends ICommand> command)
    {

        this.mCommandExecutor.unregisterCommand(command);
    }

    public void doCommand(String commandKey, Request request, AbstractResponseListener listener, boolean record, boolean resetStack)
    {
        if (listener != null)
        {
            try
            {
                CommandExecutor.getInstance().enqueueCommand(commandKey, request, listener);
            } catch (RuntimeException e)
            {
                e.printStackTrace();
            }
        } else
        {

            Object[] newTag = { request.getTag(), listener };
            request.setTag(newTag);

            try
            {
                CommandExecutor.getInstance().enqueueCommand(commandKey, request, this);
            } catch (RuntimeException e)
            {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onSuccess(Response response)
    {

    }

    @Override
    public void onRuning(Response response)
    {

    }

    @Override
    public void onFailure(Response response)
    {

    }

    @Override
    public void onFinish()
    {

    }
}
