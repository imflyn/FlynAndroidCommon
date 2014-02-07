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

    public void registerCommand(int resID, Class<? extends ICommand> command)
    {

        // String commandKey = getString(resID);
        String commandKey = "";
        registerCommand(commandKey, command);

    }

    public void registerCommand(String commandKey, Class<? extends ICommand> command)
    {
        if (command != null)
        {
            mCommandExecutor.registerCommand(commandKey, command);
        }
    }

    public void unregisterCommand(int resID)
    {
        // String commandKey = getString(resID);
        String commandKey = "";
        unregisterCommand(commandKey);
    }

    public void unregisterCommand(String commandKey)
    {

        mCommandExecutor.unregisterCommand(commandKey);
    }

}
