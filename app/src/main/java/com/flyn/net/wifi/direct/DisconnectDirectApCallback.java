package com.flyn.net.wifi.direct;

public abstract interface DisconnectDirectApCallback
{
    public abstract void onDisconnected(DirectAp paramDirectAp);

    public abstract void onError(DirectAp paramDirectAp, Exception paramException);
}
