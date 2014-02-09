package com.greatwall.util.command;

import com.greatwall.app.manager.AppManager;

public class CommandManager extends AppManager
{
    private static CommandManager instance = new CommandManager();

    private CommandManager()
    {
        super();
    }

    public static CommandManager getInstance()
    {
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

    @Override
    public void onInit()
    {

    }

    @Override
    public void onClear()
    {
        CommandQueueManager.getInstance().clear();
    }

    @Override
    public void onClose()
    {
        CommandQueueManager.getInstance().shutdown();
    }
}
