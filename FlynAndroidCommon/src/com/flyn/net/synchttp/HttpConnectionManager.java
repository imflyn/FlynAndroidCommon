package com.flyn.net.synchttp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.net.NetworkInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.flyn.net.NetManager;
import com.flyn.net.URLManager;
import com.flyn.telephone.TelephoneMgr;
import com.flyn.util.FileUtil;
import com.flyn.util.Logger;

public final class HttpConnectionManager
{
    public static final String HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_REQUEST_CONNECTION      = "Connection";
    public static final String HEADER_REQUEST_CACHE_CONTROL   = "Cache-Control";
    public static final String HEADER_REQUEST_ACCEPT_CHARSET  = "Accept-Charset";
    public static final String HEADER_REQUEST_CONTENT_TYPE    = "Content-Type";
    public static final String HEADER_REQUEST_CONTENT_LENGTH  = "Content-Length";
    public static final String HEADER_REQUEST_USER_AGENT      = "User-Agent";
    public static final String HEADER_REQUEST_COOKIE          = "Cookie";
    public static final String HEADER_RESPONSE_CONTENT_TYPE   = "Content-Type";
    public static final String HEADER_RESPONSE_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_RESPONSE_LOCATION       = "Location";
    public static final String HEADER_RESPONSE_SET_COOKIE     = "Set-Cookie";
    public static final int    REDIRECT_MAX_COUNT             = 10;
    public static final int    CMWAP_CHARGEPAGE_MAX_COUNT     = 3;
    private static Context     appContext                     = null;
    private static boolean     acceptCookie                   = true;
    private static boolean     useConcatURLModeWhenCMWap      = false;
    private static boolean     ignoreChargePageWhenCMWap      = false;

    public static void bindApplicationContext(Context context)
    {
        context = context.getApplicationContext();
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        cookieManager.removeExpiredCookie();
        appContext = context;
    }

    public static void setAcceptCookie(boolean accept)
    {
        acceptCookie = accept;
    }

    public static void setUseConcatURLModeWhenCMWap(boolean useConcatURLModeWhenCMWap)
    {
        HttpConnectionManager.useConcatURLModeWhenCMWap = useConcatURLModeWhenCMWap;
    }

    public static void ignoreChargePageWhenCMWap(boolean ignore)
    {
        ignoreChargePageWhenCMWap = ignore;
    }

    public static HttpResponseResultStream doGetForStream(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders) throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            httpConn = openConnection(url, "GET", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, null);
            HttpResponseResultStream result = new HttpResponseResultStream();
            result.setResponseURL(httpConn.getURL());
            int rspCode = httpConn.getResponseCode();
            result.setResponseCode(rspCode);
            result.setResponseHeaders(httpConn.getHeaderFields());
            input = httpConn.getInputStream();
            result.setResultStream(input);
            result.setHttpURLConn(httpConn);
            return result;
        } catch (IOException e)
        {
            try
            {
                if (input != null)
                    input.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
        }
        return null;
    }

    public static HttpResponseResultStream doGet(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders) throws IOException
    {
        HttpResponseResultStream result = doGetForStream(url, followRedirects, connOrReadTimeout, requestHeaders);
        result.generateData();
        return result;
    }

    public static HttpResponseResultStream doPostForStream(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, Map<String, String> postParams,
            String postParamsEnc) throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        try
        {
            if (requestHeaders == null)
                requestHeaders = new HashMap<String, List<String>>();
            List<String> contentTypes = new ArrayList<String>();
            contentTypes.add("application/x-www-form-urlencoded");
            requestHeaders.put("Content-Type", contentTypes);
            InputStream paramsData = null;
            if (postParams != null)
            {
                String postParamsStr = URLManager.concatParams(postParams, postParamsEnc);
                paramsData = new ByteArrayInputStream(postParamsStr.getBytes());
            }
            httpConn = openConnection(url, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, paramsData);
            HttpResponseResultStream result = new HttpResponseResultStream();
            result.setResponseURL(httpConn.getURL());
            int rspCode = httpConn.getResponseCode();
            result.setResponseCode(rspCode);
            result.setResponseHeaders(httpConn.getHeaderFields());
            input = httpConn.getInputStream();
            result.setResultStream(input);
            result.setHttpURLConn(httpConn);
            return result;
        } catch (IOException e)
        {
            try
            {
                if (input != null)
                    input.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
        }
        return null;

    }

    public static HttpResponseResult doPost(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, Map<String, String> postParams, String postParamsEnc)
            throws IOException
    {
        HttpResponseResultStream result = doPostForStream(url, followRedirects, connOrReadTimeout, requestHeaders, postParams, postParamsEnc);
        result.generateData();
        return result;
    }

    public static HttpResponseResultStream doPostForStream(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, InputStream postData)
            throws IOException
    {
        HttpURLConnection httpConn = null;
        InputStream input = null;
        HttpResponseResultStream result = null;
        try
        {
            if (requestHeaders == null)
                requestHeaders = new HashMap<String, List<String>>();
            List<String> contentTypes = new ArrayList<String>();
            contentTypes.add("application/octet-stream");
            requestHeaders.put("Content-Type", contentTypes);
            if (postData == null)
                postData = new ByteArrayInputStream(new byte[0]);
            httpConn = openConnection(url, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, postData);
            result = new HttpResponseResultStream();
            result.setResponseURL(httpConn.getURL());
            int rspCode = httpConn.getResponseCode();
            result.setResponseCode(rspCode);
            result.setResponseHeaders(httpConn.getHeaderFields());
            input = httpConn.getInputStream();
            result.setResultStream(input);
            result.setHttpURLConn(httpConn);

        } catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                if (input != null)
                    input.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
        }
        return result;
    }

    public static HttpResponseResult doPost(String url, boolean followRedirects, int connOrReadTimeout, Map<String, List<String>> requestHeaders, InputStream postData) throws IOException
    {
        HttpResponseResultStream result = doPostForStream(url, followRedirects, connOrReadTimeout, requestHeaders, postData);
        result.generateData();
        return result;
    }

    private static HttpURLConnection openConnection(String url, String method, boolean followRedirects, int connOrReadTimeout, int currentRedirectCount, int currentCMWapChargePageCount,
            Map<String, List<String>> requestHeaders, InputStream postData) throws IOException
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        if (currentRedirectCount < 0)
            throw new IllegalArgumentException("current redirect count can not set to below zero");
        if (currentRedirectCount > 10)
            throw new IOException("too many redirect times");
        if (currentCMWapChargePageCount < 0)
            throw new IllegalArgumentException("current cmwap charge page count can not set to below zero");
        if (currentCMWapChargePageCount > 3)
            throw new IOException("too many showing cmwap charge page times");
        URL originalURL = new URL(url);
        URL myURL = originalURL;
        String concatHost = null;
        java.net.Proxy proxy = null;
        NetworkInfo curNetwork = NetManager.getActiveNetworkInfo(appContext);
        if (curNetwork != null)
        {
            if (curNetwork.getType() == 0)
            {
                if ((useConcatURLModeWhenCMWap) && ("CMWAP".equals(NetManager.getNetworkDetailType(curNetwork))))
                {
                    concatHost = myURL.getAuthority();
                    String myURLStr = "http://10.0.0.172".concat(myURL.getPath());
                    String query = myURL.getQuery();
                    if (query != null)
                        myURLStr = myURLStr.concat("?").concat(query);
                    myURL = new URL(myURLStr);
                } else
                {
                    String host = android.net.Proxy.getDefaultHost();
                    int port = android.net.Proxy.getDefaultPort();
                    if ((host != null) && (port != -1))
                    {
                        if (TelephoneMgr.isOPhone20())
                        {
                            String detailType = NetManager.getNetworkDetailType(curNetwork);
                            if (("CMWAP".equals(detailType)) || ("UNIWAP".equals(detailType)) || ("CTWAP".equals(detailType)))
                            {
                                InetSocketAddress inetAddress = new InetSocketAddress(host, port);
                                Proxy.Type proxyType = Proxy.Type.valueOf(myURL.getProtocol().toUpperCase());
                                proxy = new java.net.Proxy(proxyType, inetAddress);
                            }
                        } else
                        {
                            InetSocketAddress inetAddress = new InetSocketAddress(host, port);
                            Proxy.Type proxyType = Proxy.Type.valueOf(myURL.getProtocol().toUpperCase());
                            proxy = new java.net.Proxy(proxyType, inetAddress);
                        }
                    }
                }
            }
        }
        HttpURLConnection httpConn = null;
        OutputStream output = null;
        try
        {
            Logger.logI(HttpConnectionManager.class, "request url ".concat(myURL.toString()).concat("..."));
            if ("https".equals(myURL.getProtocol()))
            {
                SSLContext sslCont = SSLContext.getInstance("TLS");
                sslCont.init(null, new TrustManager[] { new MyX509TrustManager() }, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier(myURL.getHost()));
                if (proxy == null)
                    httpConn = (HttpsURLConnection) myURL.openConnection();
                else
                {
                    httpConn = (HttpsURLConnection) myURL.openConnection(proxy);
                }
            } else if (proxy == null)
            {
                httpConn = (HttpURLConnection) myURL.openConnection();
            } else
            {
                httpConn = (HttpURLConnection) myURL.openConnection(proxy);
            }
            httpConn.setRequestMethod(method);
            HttpURLConnection.setFollowRedirects(false);
            httpConn.setInstanceFollowRedirects(false);
            httpConn.setDoInput(true);
            if (method.equalsIgnoreCase("POST"))
                httpConn.setDoOutput(true);
            else
                httpConn.setDoOutput(false);
            httpConn.setReadTimeout(connOrReadTimeout);
            httpConn.setConnectTimeout(connOrReadTimeout);
            if (concatHost != null)
            {
                httpConn.addRequestProperty("X-Online-Host", concatHost);
            }
            if (requestHeaders != null)
            {
                Iterator keys = requestHeaders.keySet().iterator();
                while (keys.hasNext())
                {
                    String key = (String) keys.next();
                    List<String> values = (List<String>) requestHeaders.get(key);
                    for (String value : values)
                    {
                        httpConn.addRequestProperty(key, value);
                    }
                }
            }
            String cookies = getCookies(url);
            if (cookies != null)
            {
                Logger.logI(HttpConnectionManager.class, "set cookies(" + cookies + ") to url " + url);
                httpConn.setRequestProperty("Cookie", cookies);
            }
            if ((method.equalsIgnoreCase("POST")) && (postData != null))
            {
                output = httpConn.getOutputStream();
                FileUtil.readAndWrite(postData, output, 2048);
                output.close();
            }
            if (acceptCookie)
            {
                Map headerFields = httpConn.getHeaderFields();
                if (headerFields != null)
                    addCookies(url, headerFields);
            }
            int rspCode = httpConn.getResponseCode();
            if ((rspCode == 301) || (rspCode == 302) || (rspCode == 303))
            {
                if (!followRedirects)
                {
                    return httpConn;
                }
                String location = httpConn.getHeaderField("Location");
                if (location == null)
                    throw new IOException("redirects failed:could not find the location header");
                if (location.toLowerCase().indexOf(originalURL.getProtocol() + "://") < 0)
                    location = originalURL.getProtocol() + "://" + originalURL.getHost() + location;
                httpConn.disconnect();
                Logger.logI(HttpConnectionManager.class, "follow redirects...");
                currentRedirectCount++;
                return openConnection(location, "GET", followRedirects, connOrReadTimeout, currentRedirectCount, currentCMWapChargePageCount, requestHeaders, null);
            }
            if (rspCode >= 400)
            {
                throw new IOException("requesting returns error http code:" + rspCode);
            }

            if (((concatHost != null) || (proxy != null)) && (!ignoreChargePageWhenCMWap))
            {
                String contentType = httpConn.getHeaderField("Content-Type");
                if ((contentType != null) && (contentType.indexOf("vnd.wap.wml") != -1))
                {
                    InputStream input = null;
                    try
                    {
                        input = httpConn.getInputStream();
                        BufferedInputStream buffInput = new BufferedInputStream(input);
                        ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
                        byte[] b = new byte[2048];
                        int len;
                        while ((len = buffInput.read(b)) > 0)
                        {
                            tempOutput.write(b, 0, len);
                        }
                        String wmlStr = new String(tempOutput.toByteArray(), "UTF-8");
                        Logger.logI(HttpConnectionManager.class, "parse the cmwap charge page...(utf-8 content:".concat(wmlStr).concat(")"));

                        String parseURL = null;
                        try
                        {
                            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                            XmlPullParser xmlParser = factory.newPullParser();
                            xmlParser.setInput(new StringReader(wmlStr));
                            boolean onEnterForward = false;
                            int eventType = xmlParser.getEventType();
                            while (eventType != 1)
                            {
                                switch (eventType)
                                {
                                    case 2:
                                        String tagName = xmlParser.getName().toLowerCase();
                                        if ("onevent".equals(tagName))
                                        {
                                            String s = xmlParser.getAttributeValue(null, "type").toLowerCase();
                                            if (!"onenterforward".equals(s))
                                                break;
                                            onEnterForward = true;
                                        } else
                                        {
                                            if (!"go".equals(tagName))
                                                break;
                                            if (!onEnterForward)
                                                break;
                                            parseURL = xmlParser.getAttributeValue(null, "href");
                                        }
                                }

                                if (parseURL != null)
                                    break;
                                eventType = xmlParser.next();
                            }
                        } catch (Exception e)
                        {
                            Logger.logW(HttpConnectionManager.class, "parse cmwap charge page failed", e);
                        }
                        if ((parseURL == null) || (parseURL.equals("")))
                        {
                            Logger.logW(HttpConnectionManager.class, "could not parse url from cmwap charge page,would use the original url to try again...");
                            parseURL = url;
                        }
                        currentCMWapChargePageCount++;
                        HttpURLConnection localHttpURLConnection1 = openConnection(parseURL, method, followRedirects, connOrReadTimeout, currentRedirectCount, currentCMWapChargePageCount,
                                requestHeaders, postData);
                        return localHttpURLConnection1;
                    } finally
                    {
                        try
                        {
                            if (input != null)
                                input.close();
                        } finally
                        {
                            httpConn.disconnect();
                        }
                    }
                }
            }
            return httpConn;
        } catch (IOException e)
        {
            try
            {
                if (output != null)
                    output.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
            throw e;
        } catch (Exception e)
        {
            try
            {
                if (output != null)
                    output.close();
            } finally
            {
                if (httpConn != null)
                    httpConn.disconnect();
            }
        }
        return null;
    }

    public static void removeCookies(String url)
    {
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        if (cookies == null)
            return;
        String[] cookieArr = cookies.split(";");
        String expires = new Date(0L).toGMTString();
        URL curUrl = null;
        try
        {
            curUrl = new URL(url);
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        String main = curUrl.getProtocol() + "://" + curUrl.getAuthority();
        String[] paths = curUrl.getPath().split("/");
        for (int i = 1; i < paths.length; i++)
        {
            main = main + "/" + paths[i];
            String[] arrayOfString;
            i = (arrayOfString = cookieArr).length;
            for (int str1 = 0; str1 < i; str1++)
            {
                String cookie = arrayOfString[str1];

                cookieManager.setCookie(main, cookie.trim() + "; expires=" + expires);
            }
        }
        main = main + "/";
        String[] arrayOfString = cookieArr;
        for (int i = 0; i < arrayOfString.length; i++)
        {
            String cookie = arrayOfString[i];

            cookieManager.setCookie(main, cookie.trim() + "; expires=" + expires);
        }
        if (!TelephoneMgr.isAndroid4Above())
            CookieSyncManager.getInstance().sync();
    }

    public static String getCookies(String url)
    {
        if (!acceptCookie)
            return null;
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        return CookieManager.getInstance().getCookie(url);
    }

    public static void addCookie(String url, String cookie)
    {
        if (!acceptCookie)
            return;
        if (appContext == null)
            throw new IllegalStateException("call bindApplicationContext(context) first,this method can be called only once");
        CookieManager.getInstance().setCookie(url, cookie);
        if (!TelephoneMgr.isAndroid4Above())
            CookieSyncManager.getInstance().sync();
    }

    private static void addCookies(String url, Map<String, List<String>> responseHeaders)
    {
        List<String> cookies = (List<String>) responseHeaders.get("Set-Cookie".toLowerCase());
        if (cookies != null)
        {
            CookieManager cookieManager = CookieManager.getInstance();
            boolean shouldSync = false;
            for (String cookie : cookies)
            {
                if (cookie == null)
                    continue;
                shouldSync = true;
                Logger.logI(HttpConnectionManager.class, "got cookie(" + cookie + ") from url " + url);
                cookieManager.setCookie(url, cookie);
            }

            if ((shouldSync) && (!TelephoneMgr.isAndroid4Above()))
                CookieSyncManager.getInstance().sync();
        }
    }

    private static class MyHostnameVerifier implements HostnameVerifier
    {
        private String hostname;

        public MyHostnameVerifier(String hostname)
        {
            this.hostname = hostname;
        }

        public boolean verify(String hostname, SSLSession session)
        {
            return this.hostname.equals(hostname);
        }
    }

    private static class MyX509TrustManager implements X509TrustManager
    {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
        {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
        {
        }

        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }
    }
}