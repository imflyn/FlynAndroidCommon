package com.flyn.net.wifi.direct;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;

public class RemoteUser
{
    private String               name      = null;
    private String               ip        = null;
    private SelectionKey         key       = null;
    private List<TransferEntity> transfers = new LinkedList();
    private long                 refreshTime;
    int                          state     = 1;

    RemoteUser(String name)
    {
        if (name == null)
            throw new NullPointerException();
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    void setIp(String ip)
    {
        if (ip == null)
            throw new NullPointerException();
        this.ip = ip;
    }

    String getIp()
    {
        return this.ip;
    }

    void setKey(SelectionKey key)
    {
        if (key == null)
            throw new NullPointerException();
        this.key = key;
    }

    SelectionKey getKey()
    {
        return this.key;
    }

    List<TransferEntity> getTransfers()
    {
        return this.transfers;
    }

    void setRefreshTime(long refreshTime)
    {
        this.refreshTime = refreshTime;
    }

    long getRefreshTime()
    {
        return this.refreshTime;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RemoteUser))
            return false;
        RemoteUser input = (RemoteUser) o;
        return getIp().equals(input.getIp());
    }
}
