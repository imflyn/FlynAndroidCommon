package com.flyn.net.volcano;

import com.android.volley.Request;

public interface NetStack
{
    public void prepare(Request<?> request);

    public boolean isAbort();

    public void abort();
}
