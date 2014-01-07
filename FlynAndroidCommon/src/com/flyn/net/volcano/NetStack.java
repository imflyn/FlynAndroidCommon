package com.flyn.net.volcano;

import java.util.Map;

import android.content.Context;

public interface NetStack
{
    RequestHandle sendRequest(Context context, String contentType, IResponseHandler responseHandler, Object... objs);
    RequestHandle makeRequest(int method,Context context,String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);
    
    
    
    String getUrlWithParams(boolean shouldEncodeUrl, String url, RequestParams params);
}
