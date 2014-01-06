package com.flyn.net.volcano;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

import android.content.Context;
import android.util.Log;

import com.flyn.net.asynchttp.MySSLSocketFactory;
import com.flyn.net.asynchttp.RequestHandle;

public class HttpClientStack implements NetStack
{

    private static final String                     VERSION                         = "1.0.0";
    private static final int                        DEFAULT_MAX_CONNETIONS          = 10;
    private static final int                        DEFAULT_SOCKET_TIMEOUT          = 10 * 1000;
    private static final int                        DEFAULT_MAX_RETRIES             = 3;
    private static final int                        DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    private static final int                        DEFAULT_SOCKET_BUFFER_SIZE      = 8192;
    private static final String                     HEADER_ACCEPT_ENCODING          = "Accept-Encoding";
    private static final String                     ENCODING_GZIP                   = "gzip";
    private static final String                     TAG                             = HttpClientStack.class.getName();
    private static final boolean                    fixNoHttpResponseException      = false;
    private static int                              httpPort                        = 80;
    private static int                              httpsPort                       = 443;

    private int                                     maxConnections                  = DEFAULT_MAX_CONNETIONS;
    private int                                     timeout                         = DEFAULT_SOCKET_TIMEOUT;

    private final DefaultHttpClient                 httpClient;
    private final HttpContext                       httpContext;
    private ExecutorService                         threadPool;
    private final Map<Context, List<RequestHandle>> requestMap;
    private final Map<String, String>               httpHeaderMap;
    private boolean                                 isURLEncodingEnabled            = true;

    public HttpClientStack()
    {
        BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, timeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNETIONS);

        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);// 禁用nagle算法,排除对小封包的处理(降低延迟)
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) " + "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");

        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, getDefaultSchemeRegistry());

        this.threadPool = Executors.newCachedThreadPool();
        this.requestMap = new WeakHashMap<Context, List<RequestHandle>>();
        this.httpHeaderMap = new HashMap<String, String>();
        this.httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        this.httpClient = new DefaultHttpClient(cm, httpParams);
        this.httpClient.addRequestInterceptor(new HttpRequestInterceptor()
        {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
            {
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING))
                {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
                for (String header : HttpClientStack.this.httpHeaderMap.keySet())
                {
                    request.addHeader(header, HttpClientStack.this.httpHeaderMap.get(header));
                }
            }
        });
        this.httpClient.addResponseInterceptor(new HttpResponseInterceptor()
        {

            @Override
            public void process(HttpResponse response, HttpContext context) throws HttpException, IOException
            {
                final HttpEntity entity = response.getEntity();
                if (null == entity)
                    return;

                final Header encoding = entity.getContentEncoding();
                if (null != encoding)
                {
                    for (HeaderElement element : encoding.getElements())
                    {
                        // 响应内容返回给客户端（通常是浏览器，或者HttpClient等）之前先进行压缩，以此来节省宽带占用
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP))
                        {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }

                }

            }
        });
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES, DEFAULT_RETRY_SLEEP_TIME_MILLIS));
    }

    private static SchemeRegistry getDefaultSchemeRegistry()
    {
        if (fixNoHttpResponseException)
        {
            Log.d(TAG, "Using the fix is insecure, as it doesn't verify SSL certificates.");
        }
        if (httpPort < 1)
        {
            httpPort = 80;
            Log.d(TAG, "Invalid HTTP port number specified, defaulting to 80");
        }

        if (httpsPort < 1)
        {
            httpsPort = 443;
            Log.d(TAG, "Invalid HTTPS port number specified, defaulting to 443");
        }

        // Fix to SSL flaw in API < ICS
        // See https://code.google.com/p/android/issues/detail?id=13117
        SSLSocketFactory sslSocketFactory;
        if (fixNoHttpResponseException)
            sslSocketFactory = MySSLSocketFactory.getFixedSocketFactory();
        else
            sslSocketFactory = SSLSocketFactory.getSocketFactory();

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
        schemeRegistry.register(new Scheme("https", sslSocketFactory, httpsPort));

        return schemeRegistry;
    }

    @Override
    public void prepare(Request request)
    {

    }

    @Override
    public boolean isAbort(Request request)
    {
        return false;
    }

    @Override
    public void abort(Request request)
    {

    }

    public HttpClient getHttpClient()
    {
        return this.httpClient;
    }

    public HttpContext getHttpContext()
    {
        return this.httpContext;
    }

    public void setCookieStore(CookieStore cookieStore)
    {
        this.httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public void setThreadPool(ThreadPoolExecutor threadPool)
    {
        this.threadPool = threadPool;
    }

    public void setEnableRedirects(final boolean enableRedirects)
    {
        this.httpClient.setRedirectHandler(new DefaultRedirectHandler()
        {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context)
            {
                return enableRedirects;
            }

        });
    }

    public void setUserAgent(String userAgent)
    {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }

    public int getMaxConnections()
    {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxConnections)
    {
        if (maxConnections < 1)
            maxConnections = DEFAULT_MAX_CONNETIONS;
        this.maxConnections = maxConnections;
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(this.maxConnections));

    }

    public int timeOut()
    {
        return this.timeout;
    }

    public void setTimeOut(int timeout)
    {
        if (timeout < 1000)
            this.timeout = DEFAULT_SOCKET_TIMEOUT;

        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, this.timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, this.timeout);

    }

    public void setProxy(String hostname, int port)
    {
        final HttpHost proxy = new HttpHost(hostname, port);
        final HttpParams httpParams = this.httpClient.getParams();

        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

    }

    public void setProxy(String hostname, int port, String username, String password)
    {
        this.httpClient.getCredentialsProvider().setCredentials(new AuthScope(hostname, port), new UsernamePasswordCredentials(username, password));
        final HttpHost proxy = new HttpHost(hostname, port);
        final HttpParams httpParams = this.httpClient.getParams();

        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory)
    {
        this.httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
    }
    
    public void setMaxRetriesAndTimeout(int retries,int timeout)
    {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(retries, timeout));
    }
    public void addHeader(String header,String value)
    {
        this.httpHeaderMap.put(header, value);
    }
    
    public void removeHeader(String  header)
    {
        this.httpHeaderMap.remove(header);
            
    }
    
    public void setBasicAuth(String username,String password,AuthScope authScope)
    {
        
        UsernamePasswordCredentials credentials=new UsernamePasswordCredentials(username, password);
        this.httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
        
    }
    public void setBasicAuth(String username, String password)
    {
        AuthScope scope = AuthScope.ANY;
        setBasicAuth(username, password, scope);
    }
    
    public void clearBasicAuth()
    {
        this.httpClient.getCredentialsProvider().clear();
    }
    public void cancelRequests(Context context, boolean mayInterruptIfRunning)
    {
        List<RequestHandle> requestList = this.requestMap.get(context);
        if (requestList != null)
        {
            for (RequestHandle requestHandle : requestList)
            {
                requestHandle.cancel(mayInterruptIfRunning);
            }
            this.requestMap.remove(context);
        }
    }

    private static class InflatingEntity extends HttpEntityWrapper
    {

        public InflatingEntity(HttpEntity wrapped)
        {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException
        {
            return new GZIPInputStream(this.wrappedEntity.getContent());
        }

        @Override
        public long getContentLength()
        {
            return -1;
        }

    }

}
