package com.flyn.net.volcano;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class NetResponseHandler implements IResponseHandler
{
    private static final String LOG_TAG            = "AsyncHttpResponseHandler";

    protected static final int  SUCCESS_MESSAGE    = 0;
    protected static final int  FAILURE_MESSAGE    = 1;
    protected static final int  START_MESSAGE      = 2;
    protected static final int  FINISH_MESSAGE     = 3;
    protected static final int  PROGRESS_MESSAGE   = 4;
    protected static final int  RETRY_MESSAGE      = 5;
    protected static final int  CANCEL_MESSAGE     = 6;

    protected static final int  BUFFER_SIZE        = 4096;

    private Handler             handler;
    public static final String  DEFAULT_CHARSET    = HTTP.UTF_8;
    private String              responseCharset    = DEFAULT_CHARSET;
    private boolean             useSynchronousMode = false;
    private URI                 requestURI         = null;
    private Map<String, String> requestHeaders     = null;

    public NetResponseHandler()
    {
        postRunnable(null);
    }

    protected void postRunnable(Runnable runnable)
    {
        boolean missingLopper = null == Looper.myLooper();
        if (missingLopper)
        {
            Looper.prepare();
        }
        if (null != this.handler)
        {
            this.handler = new ResponderHandler(this);
        }
        if (null != runnable)
        {
            this.handler.post(runnable);
        }
        if (missingLopper)
        {
            Looper.loop();
        }

    }

    static class ResponderHandler extends Handler
    {
        private final WeakReference<NetResponseHandler> mResponder;

        ResponderHandler(NetResponseHandler handler)
        {
            this.mResponder = new WeakReference<NetResponseHandler>(handler);
        }

        @Override
        public void handleMessage(Message msg)
        {
            NetResponseHandler handler = this.mResponder.get();
            if (null != handler)
            {
                handler.handleMessage(msg);
            }
        }

    }

    public void onStart()
    {

    }

    public void onFinish()
    {

    }

    public void onProgress(int bytesWritten, int bvtesTotal)
    {

    }

    public abstract void onSuccess(int statusCode, Map<String, String> headers, byte[] responseBody);

    public abstract void onFailure(int statusCode, Map<String, String> headers, byte[] responseBody, Throwable error);

    public void onRetry(int retryNo)
    {
    }

    public void onCancel()
    {

    }

    @Override
    public void sendStartMessage()
    {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    @Override
    public void sendFinishMessage()
    {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }

    @Override
    public void sendFailureMessage(int statusCode, Map<String, String> headers, byte[] responseData, Throwable error)
    {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { statusCode, headers, responseData, error }));
    }

    @Override
    public void sendCancleMessage()
    {
        sendMessage(obtainMessage(CANCEL_MESSAGE, null));
    }

    @Override
    public void sendProgress(int bytesWritten, int bytesTotal)
    {
        sendMessage(obtainMessage(START_MESSAGE, new Object[] { bytesWritten, bytesTotal }));
    }

    @Override
    public void sendRetryMessage(int retryNo)
    {
        sendMessage(obtainMessage(RETRY_MESSAGE, new Object[] { retryNo }));
    }

    @Override
    public void sendSuccessMessage(int statusCode, Map<String, String> headers, byte[] responseData)
    {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { statusCode, headers, responseData }));
    }

    @Override
    public void sendResponseMessage() throws IOException
    {
    }

    private Message obtainMessage(int responseMessageId, Object responseMessage)
    {
        Message msg;
        if (this.handler != null)
        {
            msg = this.handler.obtainMessage(responseMessageId, responseMessage);
        } else
        {
            msg = Message.obtain();
            if (null != msg)
            {
                msg.what = responseMessageId;
                msg.obj = responseMessage;
            }

        }

        return msg;
    }

    private void sendMessage(Message msg)
    {
        if (this.handler == null || getUseSynchronousMode())
        {

            handleMessage(msg);
        } else
        {
            this.handler.sendMessage(msg);

        }
    }

    @SuppressWarnings("unchecked")
    protected void handleMessage(Message msg)
    {
        Object[] response;

        switch (msg.what)
        {
            case SUCCESS_MESSAGE:
                response = (Object[]) msg.obj;
                if (null != response && response.length >= 3)
                    onSuccess((Integer) response[0], (Map<String, String>) response[1], (byte[]) response[2]);
                else
                    Log.e(LOG_TAG, "SUCCESS_MESSAGE didn't got enough params");
                break;
            case FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 4)
                    onFailure((Integer) response[0], (Map<String, String>) response[1], (byte[]) response[2], (Throwable) response[3]);
                else
                    Log.e(LOG_TAG, "FAILURE_MESSAGE didn't got enough params");
                break;
            case START_MESSAGE:
                onStart();
                break;
            case FINISH_MESSAGE:
                onFinish();
                break;
            case PROGRESS_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 2)
                {
                    try
                    {
                        onProgress((Integer) response[0], (Integer) response[1]);
                    } catch (Throwable t)
                    {
                        Log.e(LOG_TAG, "custom onProgress contains an error", t);
                    }
                } else
                    Log.e(LOG_TAG, "PROGRESS_MESSAGE didn't got enough params");
                break;
            case RETRY_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length == 1)
                    onRetry((Integer) response[0]);
                else
                    Log.e(LOG_TAG, "RETRY_MESSAGE didn't get enough params");
                break;
            case CANCEL_MESSAGE:
                onCancel();
                break;

        }
    }

    @Override
    public URI getRequestURI()
    {
        return this.requestURI;
    }

    @Override
    public Map<String, String> getRequestHeaders()
    {
        return this.requestHeaders;
    }

    @Override
    public void setRequestURI(URI requestURI)
    {
        this.requestURI = requestURI;
    }

    @Override
    public void setRequestHeaders(Map<String, String> requestHeaders)
    {
        this.requestHeaders = requestHeaders;
    }

    @Override
    public boolean getUseSynchronousMode()
    {
        return useSynchronousMode;
    }

    @Override
    public void setUseSynchronousMode(boolean value)
    {
        useSynchronousMode = value;
    }

    public final String getReponseCharse()
    {
        return this.responseCharset == null ? DEFAULT_CHARSET : this.responseCharset;
    }

    public final void setReponseCharse(String reponseCharse)
    {
        this.responseCharset = reponseCharse;
    }

}
