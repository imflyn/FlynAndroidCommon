package com.flyn.net.volcano;

import java.util.Map;

import android.content.Context;

public abstract class NetStack
{
    abstract RequestFuture sendRequest(Context context, String contentType, IResponseHandler responseHandler, Object[] objs);

    abstract RequestFuture makeRequest(int method, Context context, String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    public static String getUrlWithParams(boolean shouldEncodeUrl, String url, RequestParams params)
    {
        if (shouldEncodeUrl)
        {
            url = url.replace(" ", "%20");
        }
        if (null != params)
        {
            String paramString = params.getParamString();
            if (!url.contains("?"))
            {
                url += "?" + paramString;
            } else
            {
                url += "&" + paramString;
            }
        }
        return url;
    }
}
