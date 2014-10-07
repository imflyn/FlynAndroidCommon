package com.flyn.net.wifi.direct;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;

public class RemoteUser
{
    int state = 1;
    private String name = null;
    private String ip = null;
    private SelectionKey key = null;
    private List<TransferEntity> transfers = new LinkedList<TransferEntity>();
    private long refreshTime;

    RemoteUser(String name)
    {
        if (name == null)
        {
            throw new NullPointerException();
        }
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    String getIp()
    {
        return this.ip;
    }

    void setIp(String ip)
    {
        if (ip == null)
        {
            throw new NullPointerException();
        }
        this.ip = ip;
    }

    SelectionKey getKey()
    {
        return this.key;
    }

    void setKey(SelectionKey key)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        this.key = key;
    }

    List<TransferEntity> getTransfers()
    {
        return this.transfers;
    }

    long getRefreshTime()
    {
        return this.refreshTime;
    }

    void setRefreshTime(long refreshTime)
    {
        this.refreshTime = refreshTime;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof RemoteUser))
        {
            return false;
        }
        RemoteUser input = (RemoteUser) o;
        return getIp().equals(input.getIp());
    }
}
