package com.flyn.net.volcano;

import com.android.volley.Request;

public interface HttpStack
{
    public void prepare(Request<?> request);
}
