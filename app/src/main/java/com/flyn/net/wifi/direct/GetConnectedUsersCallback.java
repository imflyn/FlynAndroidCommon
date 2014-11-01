package com.flyn.net.wifi.direct;

import java.util.List;

public abstract interface GetConnectedUsersCallback
{
    public abstract void onGet(List<RemoteUser> paramList);
}
