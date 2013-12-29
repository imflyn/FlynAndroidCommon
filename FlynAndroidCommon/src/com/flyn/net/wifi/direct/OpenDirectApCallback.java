package com.flyn.net.wifi.direct;

public abstract interface OpenDirectApCallback
{
    public abstract void onOpen();

    public abstract void onError(Exception paramException);
}
