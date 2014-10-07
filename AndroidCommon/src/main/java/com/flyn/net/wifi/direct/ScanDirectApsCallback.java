package com.flyn.net.wifi.direct;

import java.util.List;

public abstract interface ScanDirectApsCallback
{
    public abstract void onScanned(List<DirectAp> paramList);

    public abstract void onError();
}
