package com.flyn.net.volcano;


public interface NetStack
{
    public void prepare(Request request);

    public boolean isAbort(Request request);

    public void abort(Request request);
}
