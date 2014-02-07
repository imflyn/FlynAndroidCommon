package com.greatwall.util.command;

public class Test
{
    private CommandExecutor mCommandExecutor;

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
                CommandExecutor.getInstance().enqueueCommand(commandKey, request, new AbstractResponseListener()
                {

                    @Override
                    void onSuccess(Response response)
                    {

                    }

                    @Override
                    void onFailure(Response response)
                    {

                    }
                });
            } catch (RuntimeException e)
            {
                e.printStackTrace();
            }

        }

    }


    public void registerCommand( Class<? extends ICommand> command)
    {
        if (command != null)
        {
            mCommandExecutor.registerCommand( command);
        }
    }


    public void unregisterCommand(Class<? extends ICommand> command)
    {

        mCommandExecutor.unregisterCommand(command);
    }

}
