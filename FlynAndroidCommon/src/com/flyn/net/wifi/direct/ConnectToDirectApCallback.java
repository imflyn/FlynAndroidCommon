package com.flyn.net.wifi.direct;

public abstract interface ConnectToDirectApCallback
{
    public abstract void onConnected(DirectAp paramDirectAp, RemoteUser paramRemoteUser);

    public abstract void onError(DirectAp paramDirectAp);
}
