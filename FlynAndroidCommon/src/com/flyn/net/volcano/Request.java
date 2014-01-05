package com.flyn.net.volcano;

import java.io.IOException;

import org.apache.http.protocol.HTTP;

import android.util.Log;

public abstract class Request implements Runnable
{
    public static final int     TYPE_HTTPCLIENT         = 0x01;
    public static final int     TYPE_HTTPCONNECTION     = 0x02;

    private static final String DEFAULT_PARAMS_ENCODING = HTTP.UTF_8;

    private NetStack            httpStack;
    private IResponseHandler    responseHandler;

    private int                 executionCount;
    private boolean             isCancelled             = false;
    private boolean             cancelIsNotified        = false;
    private boolean             isFinished              = false;

    public Request(NetStack httpStack, IResponseHandler responseHandler)
    {
        this.httpStack = httpStack;
        this.responseHandler = responseHandler;
    }

    @Override
    public void run()
    {
        if (isCancelled())
            return;

        if (null != responseHandler)
            responseHandler.sendStartMessage();

        if (isCancelled())
        {
            return;
        }

        try
        {
            makeRequestWithRetries();
        } catch (IOException e)
        {
            if (!isCancelled() && responseHandler != null)
            {
                responseHandler.sendFailureMessage(0, null, null, e);
            } else
            {
                Log.e("HttpRequest", "makeRequestWithRetries returned error, but handler is null", e);
            }
        }
        if (isCancelled())
        {
            return;
        }

        if (responseHandler != null)
        {
            responseHandler.sendFinishMessage();
        }

        isFinished = true;
    }

    protected abstract void makeRequest() throws IOException;

    protected abstract void makeRequestWithRetries() throws IOException;

    public final boolean isCancelled()
    {
        if (isCancelled)
            sendCancleNotification();

        return isCancelled;
    }

    private synchronized void sendCancleNotification()
    {
        if (!isFinished && isCancelled && !cancelIsNotified)
        {
            cancelIsNotified = true;
            if (null != responseHandler)
                responseHandler.sendCancleMessage();
        }
    }

    public final boolean isFinished()
    {
        return isCancelled() || isFinished;
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
        isCancelled = true;
        if (mayInterruptIfRunning && httpStack != null && !httpStack.isAbort(this))
        {
            httpStack.abort(this);
        }
        return isCancelled();

    }

    public interface Method
    {
        int DEPRECATED_GET_OR_POST = -1;
        int GET                    = 0;
        int POST                   = 1;
        int PUT                    = 2;
        int DELETE                 = 3;
    }

}
