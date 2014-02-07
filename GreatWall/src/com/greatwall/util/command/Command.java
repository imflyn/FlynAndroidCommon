package com.greatwall.util.command;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class Command extends BaseCommand
{
    private final static int         COMMAND_START    = 1;
    private final static int         COMMAND_RUNNTING = 2;
    private final static int         COMMAND_FAILURE  = 3;
    private final static int         COMMAND_SUCCESS  = 4;
    private final static int         COMMAND_FINISH   = 5;

    private AbstractResponseListener mListener;
    private Handler                  mHandler;

    protected Command()
    {
        postRunnable();
    }

    protected void postRunnable()
    {
        boolean missingLooper = null == Looper.getMainLooper();
        if (missingLooper)
        {
            Looper.prepare();
        }
        if (null == this.mHandler)
        {
            this.mHandler = new ResponderHandler(this);
        }
        if (missingLooper)
        {
            Looper.loop();
        }
    }

    private static class ResponderHandler extends Handler
    {
        private final WeakReference<Command> mResponder;

        ResponderHandler(Command service)
        {
            this.mResponder = new WeakReference<Command>(service);
        }

        @Override
        public void handleMessage(Message msg)
        {
            Command service = this.mResponder.get();
            if (null != service)
            {
                service.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case COMMAND_START:
                this.mListener.onStart();
                break;
            case COMMAND_RUNNTING:
                this.mListener.onRuning(getResponse());
                break;
            case COMMAND_SUCCESS:
                this.mListener.onSuccess(getResponse());
                break;
            case COMMAND_FAILURE:
                this.mListener.onFailure(getResponse());
                break;
            case COMMAND_FINISH:
                this.mListener.onFinish();
                break;
            default:
                break;
        }
    }

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
        this.mListener = getResponseListener();
        if (this.mListener != null)
            this.mHandler.sendEmptyMessage(state);
    }

    public void sendStartMessage()
    {
        sendMessage(COMMAND_START);
    }

    public void sendSuccessMessage(Object object)
    {
        Response response = new Response();
        response.setData(object);
        setResponse(response);
        sendMessage(COMMAND_SUCCESS);
    }

    public void sendFailureMessage(Object object)
    {
        Response response = new Response();
        response.setData(object);
        setResponse(response);
        sendMessage(COMMAND_FAILURE);
    }

    public void sendRuntingMessage(Object object)
    {
        Response response = new Response();
        response.setData(object);
        setResponse(response);
        sendMessage(COMMAND_RUNNTING);
    }

    public void sendFinishMessage()
    {
        sendMessage(COMMAND_FINISH);
    }
}
