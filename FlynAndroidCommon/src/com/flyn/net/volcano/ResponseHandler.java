package com.flyn.net.volcano;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.protocol.HTTP;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class ResponseHandler implements IResponseHandler
{
    private static final String TAG            = "ResponseHandler";

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

    public ResponseHandler()
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
        private final WeakReference<ResponseHandler> mResponder;

        ResponderHandler(ResponseHandler handler)
        {
            this.mResponder = new WeakReference<ResponseHandler>(handler);
        }

        @Override
        public void handleMessage(Message msg)
        {
            ResponseHandler handler = this.mResponder.get();
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
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] { bytesWritten, bytesTotal }));
    }

    @Override
    public void sendRetryMessage(int retryNo)
    {
        sendMessage(obtainMessage(RETRY_MESSAGE, new Object[] { retryNo }));
    }

    @Override
    public void sendSuccessMessage(int statusCode, Map<String, String> headers, byte[] responseData)
    {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] { statusCode, headers, responseData }));
    }

    @Override
    public void sendResponseMessage(HttpResponse response) throws IOException
    {
        if (!Thread.currentThread().isInterrupted())
        {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == HttpStatus.SC_NOT_MODIFIED)
            {
                // 还未加入Cache缓存处理,加入后可以添加具体处理逻辑
            }

            byte[] responseData;

            if (response.getEntity() != null)
                responseData = entityToBytes(response.getEntity());
            else
                responseData = new byte[0];

            if (!Thread.currentThread().isInterrupted())
            {
                if (statusCode >= 200 && statusCode < 300)
                    sendSuccessMessage(statusCode, convertHeaders(response.getAllHeaders()), responseData);
                else
                    sendFailureMessage(statusCode, convertHeaders(response.getAllHeaders()), responseData, new HttpResponseException(statusCode, statusLine.getReasonPhrase()));
            }
        }
    }

    private Map<String, String> convertHeaders(Header[] headers)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Header header : headers)
        {
            map.put(header.getName(), header.getValue());
        }
        return map;
    }

    // protected byte[] entityToBytes(HttpEntity entity) throws IOException
    // {
    //
    // byte[] responseData = null;
    //
    // InputStream inStream = entity.getContent();
    // if (inStream != null)
    // {
    // long contentLength = entity.getContentLength();
    // if (contentLength > Integer.MAX_VALUE)
    // {
    // throw new
    // IllegalArgumentException("HttpEntity is too large to be buffered.");
    // }
    // int buffersize = (contentLength < 0) ? BUFFER_SIZE : (int) contentLength;
    //
    // try
    // {
    // ByteArrayBuffer buffer = new ByteArrayBuffer(buffersize);
    //
    // byte[] temp = new byte[BUFFER_SIZE];
    // int l, count = 0;
    // try
    // {
    // while ((l = inStream.read(temp)) != -1 &&
    // !Thread.currentThread().isInterrupted())
    // {
    // count += l;
    // buffer.append(temp, 0, l);
    //
    // if (contentLength>=0&&((count / (contentLength / 100)) % 10 == 0))
    // sendProgress(count, (int) contentLength);
    // }
    // } finally
    // {
    // inStream.close();
    // }
    // responseData = buffer.toByteArray();
    // } catch (OutOfMemoryError e)
    // {
    // System.gc();
    // throw new IOException("Data too large to get in memory.");
    // }
    // }
    //
    // return responseData;
    // }

    protected byte[] entityToBytes(HttpEntity entity) throws IOException
    {

        byte[] responseData = null;

        InputStream inStream = entity.getContent();
        if (inStream != null)
        {
            long contentLength = entity.getContentLength();
            if (contentLength > Integer.MAX_VALUE)
            {
                throw new IllegalArgumentException("HttpEntity is too large to be buffered.");
            }

            ByteArrayPool mPool = new ByteArrayPool(BUFFER_SIZE);
            PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) contentLength);
            byte[] buffer = null;
            try
            {
                buffer = mPool.getBuf(1024);
                int count;
                while ((count = inStream.read(buffer)) != -1 && !Thread.currentThread().isInterrupted())
                {
                    bytes.write(buffer, 0, count);
                    if (contentLength >= 0 && ((count / (contentLength / 100)) % 10 == 0))
                        sendProgress(count, (int) contentLength);
                }

                responseData = bytes.toByteArray();
            } catch (OutOfMemoryError e)
            {
                System.gc();
                throw new IOException("Data too large to get in memory.");
            } finally
            {
                try
                {
                    //释放http连接所占用的资源
                    entity.consumeContent();
                } catch (IOException e)
                {
                    Log.e(ResponseHandler.class.getName(), "Error occured when calling consumingContent", e);
                }
                mPool.returnBuf(buffer);
                bytes.close();
            }
        }

        return responseData;
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
            handleMessage(msg);
        else
            this.handler.sendMessage(msg);
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
                    Log.e(TAG, "SUCCESS_MESSAGE didn't got enough params");
                break;
            case FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 4)
                    onFailure((Integer) response[0], (Map<String, String>) response[1], (byte[]) response[2], (Throwable) response[3]);
                else
                    Log.e(TAG, "FAILURE_MESSAGE didn't got enough params");
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
                        Log.e(TAG, "custom onProgress contains an error", t);
                    }
                } else
                    Log.e(TAG, "PROGRESS_MESSAGE didn't got enough params");
                break;
            case RETRY_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length == 1)
                    onRetry((Integer) response[0]);
                else
                    Log.e(TAG, "RETRY_MESSAGE didn't get enough params");
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
        return this.useSynchronousMode;
    }

    @Override
    public void setUseSynchronousMode(boolean value)
    {
        this.useSynchronousMode = value;
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
