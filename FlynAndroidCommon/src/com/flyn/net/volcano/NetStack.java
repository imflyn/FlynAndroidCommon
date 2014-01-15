package com.flyn.net.volcano;

import java.util.List;
import java.util.Map;

import android.content.Context;

public abstract class NetStack
{
    protected static final int                  DEFAULT_MAX_CONNETIONS          = 10;
    protected static final int                  DEFAULT_SOCKET_TIMEOUT          = 10 * 1000;
    protected static final int                  DEFAULT_MAX_RETRIES             = 3;
    protected static final int                  DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    protected static final int                  DEFAULT_SOCKET_BUFFER_SIZE      = 8192;
    protected static final String               HEADER_ACCEPT_ENCODING          = "Accept-Encoding";
    protected static final String               ENCODING_GZIP                   = "gzip";
    protected static int                        httpPort                        = 80;
    protected static int                        httpsPort                       = 443;

    protected final boolean                     fixNoHttpResponseException      = false;
    protected int                               maxConnections                  = DEFAULT_MAX_CONNETIONS;
    protected int                               timeout                         = DEFAULT_SOCKET_TIMEOUT;
    protected Map<Context, List<RequestFuture>> requestMap;
    protected Map<String, String>               httpHeaderMap;
    protected boolean                           isURLEncodingEnabled            = true;

    abstract RequestFuture sendRequest(Context context, String contentType, IResponseHandler responseHandler, Object[] objs);

    abstract RequestFuture makeRequest(int method, Context context, String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    public int getMaxConnections()
    {
        return this.maxConnections;
    }

    public void addHeader(String header, String value)
    {
        this.httpHeaderMap.put(header, value);
    }

    public void removeHeader(String header)
    {
        this.httpHeaderMap.remove(header);

    }

    public void setURLEncodingEnabled(boolean isURLEncodingEnabled)
    {
        this.isURLEncodingEnabled = isURLEncodingEnabled;
    }

    public void cancelRequests(Context context, boolean mayInterruptIfRunning)
    {

        List<RequestFuture> requestList = this.requestMap.get(context);
        if (requestList != null)
        {
            for (RequestFuture requestHandle : requestList)
            {
                requestHandle.cancel(mayInterruptIfRunning);
            }
            this.requestMap.remove(context);
        }
    }
    public int timeOut()
    {
        return this.timeout;
    }

  

}
