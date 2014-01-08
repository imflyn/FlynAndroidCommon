package com.flyn.net.volcano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * HttpClient方法的请求参数容器
 * 
 * @author V
 * 
 */
public class HttpClientRequestParams extends RequestParams
{

    @Override
    protected List<BasicNameValuePair> getParamsList()
    {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet())
        {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        lparams.addAll(getParamsList(null, urlParamsWithObjects));

        return lparams;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<BasicNameValuePair> getParamsList(String key, Object value)
    {
        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        if (value instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>) value;
            List<String> list = new ArrayList<String>(map.keySet());
            Collections.sort(list);
            for (String nestedKey : list)
            {
                Object nesetedValue = map.get(nestedKey);

                if (null != nesetedValue)
                {
                    params.addAll(getParamsList(key == null ? nestedKey : String.format("%s[%s]", key, nestedKey), nesetedValue));
                }
            }

        } else if (value instanceof List)
        {
            List<Object> list = (List<Object>) value;
            for (Object nestedValue : list)
            {
                params.addAll(getParamsList(String.format("%s[]", key), nestedValue));
            }
        } else if (value instanceof Object[])
        {
            Object[] array = (Object[]) value;
            for (Object nestedValue : array)
            {
                params.addAll(getParamsList(String.format("%s[]", key), nestedValue));
            }
        } else if (value instanceof Set)
        {
            Set<Object> set = (Set<Object>) value;
            for (Object nestedValue : set)
            {
                params.addAll(getParamsList(key, nestedValue));
            }
        } else if (value instanceof String)
        {
            params.add(new BasicNameValuePair(key, (String) value));
        } else if (value instanceof String)
        {
            params.add(new BasicNameValuePair(key, (String) value));
        }

        return params;
    }

    protected String getParamString()
    {
        return URLEncodedUtils.format(getParamsList(), contentEncoding);
    }

    @Override
    protected byte[] createJsonStreamData()
    {
        return null;
    }

    @Override
    protected byte[] createFormData()
    {
        return null;
    }

    @Override
    protected byte[] createMultipartData(IResponseHandler progressHandler)
    {
        return null;
    }

}
