package com.greatwall.util.command;

public class CommandManager
{
    private static CommandManager instance;
    private CommandExecutor       mCommandExecutor;

    public static CommandManager getInstance()
    {
        if (null == instance)
        {
            synchronized (CommandManager.class)
            {
                if (null == instance)
                    instance = new CommandManager();
            }
        }
        return instance;
    }

    public void doCommand(Class<? extends ICommand> cmdClass, Request request, AbstractResponseListener listener)
    {
        if (listener != null)
        {
            try
            {
                CommandExecutor.getInstance().enqueueCommand(cmdClass, request, listener);
            } catch (CommandException e)
            {
                listener.onFailure(response);
            }
        } else
        {

            Object[] newTag = { request.getTag(), listener };
            request.setTag(newTag);

            CommandExecutor.getInstance().enqueueCommand(cmdClass, request, new AbstractResponseListener()
            {

                @Override
                public void onSuccess(Response response)
                {

                }

                @Override
                public void onFailure(Response response)
                {

                }
            });

        }

    }

}
