package com.greatwall.util.command;

public class CommandManager
{
    private static CommandManager instance;

    private CommandManager()
    {

    }

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
        if (listener == null)
        {
            listener = new AbstractResponseListener()
            {

                @Override
                public void onSuccess(Response response)
                {

                }

                @Override
                public void onFailure(Response response)
                {

                }
            };
        }
        try
        {
            CommandExecutor.getInstance().enqueueCommand(cmdClass, request, listener);
        } catch (CommandException e)
        {
            Response response = new Response();
            response.setTag(request.getTag());
            response.setData(response.getData());
            listener.onFailure(response);
        }

    }

    public void cancelCommand(Request request)
    {
        request.cancel();
    }

    public void cancelCommand(Request... requests)
    {
        for (int i = 0; i < requests.length; i++)
        {
            requests[i].cancel();
        }
    }
}
