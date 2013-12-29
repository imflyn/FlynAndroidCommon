package com.flyn.net.wifi.direct;

import android.net.wifi.ScanResult;

public class DirectAp
{
    private String     name       = null;
    private ScanResult scanResult = null;

    DirectAp(String name)
    {
        if (name == null)
            throw new NullPointerException();
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    void setScanResult(ScanResult scanResult)
    {
        if (scanResult == null)
            throw new NullPointerException();
        this.scanResult = scanResult;
    }

    ScanResult getScanResult()
    {
        return this.scanResult;
    }
}
