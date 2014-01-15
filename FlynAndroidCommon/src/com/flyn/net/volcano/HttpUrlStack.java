package com.flyn.net.volcano;

import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;

public class HttpUrlStack extends NetStack
{
    public HttpUrlStack()
    {
        
    }

    @Override
    protected RequestFuture sendRequest(Context context, String contentType, IResponseHandler responseHandler, Object[] objs)
    {
        return null;
    }

    @Override
    protected RequestFuture makeRequest(int method, Context context, String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        return null;
    }

    @Override
    public void setCookieStore(CookieStore cookieStore)
    {

    }

    @Override
    public void setEnableRedirects(boolean enableRedirects)
    {

    }

    @Override
    public void setUserAgent(String userAgent)
    {

    }

    @Override
    public void setMaxConnections(int maxConnections)
    {

    }

    @Override
    public void setTimeOut(int timeout)
    {

    }

    @Override
    public void setProxy(String hostname, int port)
    {

    }

    @Override
    public void setProxy(String hostname, int port, String username, String password)
    {

    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory)
    {

    }

    @Override
    public void setMaxRetriesAndTimeout(int retries, int timeout)
    {

    }

    @Override
    public void setBasicAuth(String username, String password, AuthScope authScope)
    {

    }

    @Override
    public void setBasicAuth(String username, String password)
    {

    }

    @Override
    public void clearBasicAuth()
    {

    }

    @Override
    protected RequestFuture get(Context context, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        return null;
    }

    @Override
    protected RequestFuture post(Context context, String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        return null;
    }

    @Override
    protected RequestFuture delete(Context context, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        return null;
    }

    @Override
    protected RequestFuture put(Context context, String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        return null;
    }

    @Override
    protected RequestFuture head(Context context, String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        return null;
    }

}
