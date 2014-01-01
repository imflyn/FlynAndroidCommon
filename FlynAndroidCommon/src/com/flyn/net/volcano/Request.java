package com.flyn.net.volcano;


public class Request<T> implements Comparable<Request<T>>
{
    public static final int TYPE_HTTPCLIENT=0x01;
    public static final int TYPE_HTTPCONNECTION=0x02;
    
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    private  int                    mMethod;
    private  String                 mUrl;
    
    
    
    public interface Method
    {
        int DEPRECATED_GET_OR_POST = -1;
        int GET                    = 0;
        int POST                   = 1;
        int PUT                    = 2;
        int DELETE                 = 3;
    }


    @Override
    public int compareTo(Request<T> another)
    {
        return 0;
    }
    
}
