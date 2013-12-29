package com.flyn.net.wifi.direct;

public abstract interface CloseCallback
{
    public abstract void onClosed();

    public abstract void onError(Exception paramException);
}
