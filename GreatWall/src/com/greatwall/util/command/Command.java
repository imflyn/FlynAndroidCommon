package com.greatwall.util.command;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public abstract class Command extends BaseCommand
{
    protected final static int command_start   = 1;
    protected final static int command_runting = 2;
    protected final static int command_failure = 3;
    protected final static int command_success = 4;
    protected final static int command_finish  = 5;
    private AbstractResponseListener  listener;
    private final Handler      handler         = new Handler(Looper.getMainLooper())
                                               {
                                                   @Override
                                                   public void handleMessage(Message msg)
                                                   {
                                                       switch (msg.what)
                                                       {
                                                           case command_start:
                                                               listener.onStart();
                                                               break;
                                                           case command_runting:
                                                               listener.onRuning(getResponse());
                                                               break;
                                                           case command_success:
                                                               listener.onSuccess(getResponse());
                                                               break;
                                                           case command_failure:
                                                               listener.onFailure(getResponse());
                                                               break;
                                                           case command_finish:
                                                               listener.onFinish();
                                                               break;
                                                           default:
                                                               break;
                                                       }
                                                   };

                                               };

    @Override
    public final void execute()
    {
        onPreExecuteCommand();
        executeCommand();
        onPostExecuteCommand();
    }

    protected abstract void executeCommand();

    protected void onPreExecuteCommand()
    {
        sendStartMessage();
    }

    protected void onPostExecuteCommand()
    {

    }

    protected void sendMessage(int state)
    {
        listener = getResponseListener();
        if (listener != null)
        {
            handler.sendEmptyMessage(state);
        }
    }

    public void sendStartMessage()
    {
        sendMessage(command_start);
    }

    public void sendSuccessMessage(Object object)
    {
        Response response = new Response();
        response.setData(object);
        setResponse(response);
        sendMessage(command_success);
    }

    public void sendFailureMessage(Object object)
    {
        Response response = new Response();
        response.setData(object);
        setResponse(response);
        sendMessage(command_failure);
    }

    public void sendRuntingMessage(Object object)
    {
        Response response = new Response();
        response.setData(object);
        setResponse(response);
        sendMessage(command_runting);
    }

    public void sendFinishMessage()
    {
        sendMessage(command_finish);
    }
}
