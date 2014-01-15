package com.flyn.net.volcano;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.SSLSocketFactory;

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

    protected ExecutorService                   threadPool;
    protected final boolean                     fixNoHttpResponseException      = false;
    protected int                               maxConnections                  = DEFAULT_MAX_CONNETIONS;
    protected int                               timeout                         = DEFAULT_SOCKET_TIMEOUT;
    protected Map<Context, List<RequestFuture>> requestMap;
    protected Map<String, String>               httpHeaderMap;
    protected boolean                           isURLEncodingEnabled            = true;

    protected abstract RequestFuture sendRequest(Context context, String contentType, IResponseHandler responseHandler, Object[] objs);

    protected abstract RequestFuture makeRequest(int method, Context context, String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    protected abstract RequestFuture get(Context context, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    protected abstract RequestFuture post(Context context, String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler);

    protected abstract RequestFuture delete(Context context, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    protected abstract RequestFuture put(Context context, String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler);

    protected abstract RequestFuture head(Context context, String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler);

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

    /**
     * Set it before request started
     * 
     * @param threadPool
     */
    public void setThreadPool(ThreadPoolExecutor threadPool)
    {
        this.threadPool = threadPool;
    }

    public int timeOut()
    {
        return this.timeout;
    }

    public abstract void setCookieStore(CookieStore cookieStore);

    public abstract void setEnableRedirects(final boolean enableRedirects);

    public abstract void setUserAgent(String userAgent);

    public abstract void setMaxConnections(int maxConnections);

    public abstract void setTimeOut(int timeout);

    public abstract void setProxy(String hostname, int port);

    public abstract void setProxy(String hostname, int port, String username, String password);

    public abstract void setSSLSocketFactory(SSLSocketFactory sslSocketFactory);

    public abstract void setMaxRetriesAndTimeout(int retries, int timeout);

    public abstract void setBasicAuth(String username, String password, AuthScope authScope);

    public abstract void setBasicAuth(String username, String password);

    public abstract void clearBasicAuth();

}
