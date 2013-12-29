package com.flyn.net.synchttp;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpResponseResult
{
    protected URL                       responseURL     = null;
    protected int                       responseCode    = -1;
    protected Map<String, List<String>> responseHeaders = null;
    protected byte[]                    data            = null;

    public URL getResponseURL()
    {
        return this.responseURL;
    }

    public void setResponseURL(URL responseURL)
    {
        this.responseURL = responseURL;
    }

    public int getResponseCode()
    {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    public Map<String, List<String>> getResponseHeaders()
    {
        return this.responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders)
    {
        this.responseHeaders = responseHeaders;
    }

    public byte[] getData()
    {
        return this.data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public String getDataString(String charset)
    {
        if (this.data == null)
            return null;
        try
        {
            return new String(this.data, charset);
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

    }
}