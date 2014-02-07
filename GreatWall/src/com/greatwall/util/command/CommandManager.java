package com.greatwall.util.command;

public class CommandManager 
{
    private  static  CommandManager instance;
    private CommandExecutor mCommandExecutor;
    
    public static CommandManager getInstance()
    {
        if(null==instance)
        {
            synchronized (CommandManager.class)
            {
                if(null==instance)
                    instance=new CommandManager();
            }
        }
        return instance;
    }
    
    public void registerCommand(Class<? extends ICommand> command)
    {
        if (command != null)
        {
            this.mCommandExecutor.registerCommand(command);
        }
    }

    public void unregisterCommand(Class<? extends ICommand> command)
    {

        this.mCommandExecutor.unregisterCommand(command);
    }

    public  void doCommand(String commandKey, Request request, AbstractResponseListener listener)
    {
        if (listener != null)
        {
                CommandExecutor.getInstance().enqueueCommand(commandKey, request, listener);
        } else
        {

            Object[] newTag = { request.getTag(), listener };
            request.setTag(newTag);

                CommandExecutor.getInstance().enqueueCommand(commandKey, request, new AbstractResponseListener()
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
