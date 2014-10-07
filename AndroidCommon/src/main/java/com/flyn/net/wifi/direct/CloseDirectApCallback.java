package com.flyn.net.wifi.direct;

public abstract interface CloseDirectApCallback
{
    public abstract void onClosed();

    public abstract void onError(Exception paramException);
}
