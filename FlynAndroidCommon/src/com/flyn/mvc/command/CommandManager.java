package com.flyn.mvc.command;

import android.content.Context;

import com.flyn.mvc.common.IResponseListener;
import com.flyn.mvc.common.Request;
import com.flyn.mvc.common.Response;

public class CommandManager implements IResponseListener
{
    private CommandExecutor mCommandExecutor;

    public void registerCommand(int resID, Class<? extends ICommand> command, Context context)
    {

        String commandKey = context.getString(resID);
        registerCommand(commandKey, command);

    }

    public void registerCommand(String commandKey, Class<? extends ICommand> command)
    {
        if (command != null)
        {
            mCommandExecutor.registerCommand(commandKey, command);
        }
    }

    public void unregisterCommand(int resID, Context context)
    {
        String commandKey = context.getString(resID);
        unregisterCommand(commandKey);
    }

    public void unregisterCommand(String commandKey)
    {

        mCommandExecutor.unregisterCommand(commandKey);
    }

    public void doCommand(String commandKey, Request request, IResponseListener listener, boolean record, boolean resetStack)
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
