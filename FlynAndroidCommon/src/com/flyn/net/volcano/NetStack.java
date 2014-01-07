package com.flyn.net.volcano;

public interface NetStack
{
    public void sendRequest(Request request);

    public boolean isAbort(Request request);

    public void abort(Request request);
}
