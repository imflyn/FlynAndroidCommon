package com.flyn.net.wifi.direct;

import java.util.List;

public abstract interface ScanUsersCallback
{
    public abstract void onScanned(List<RemoteUser> paramList);
}
