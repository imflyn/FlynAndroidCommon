package com.flyn.net.volcano;

import com.android.volley.Request;

public class HttpClientStack implements NetStack
{
    
    
    
    
    @Override
    public void prepare(Request<?> request)
    {
        
    }

    @Override
    public boolean isAbort()
    {
        return false;
    }

    @Override
    public void abort()
    {
        
    }

}
