package com.flyn.net.wifi.direct;

import java.nio.channels.SelectionKey;

public class TransferEntity
{
    int state = 1;
    private RemoteUser remoteUser;
    private String sendPath;
    private String savingPath;
    private long size;
    private boolean isSender;
    private String extraDescription;
    private Object tag;
    private SelectionKey transferKey;

    public RemoteUser getRemoteUser()
    {
        return this.remoteUser;
    }

    void setRemoteUser(RemoteUser remoteUser)
    {
        if (remoteUser == null)
        {
            throw new NullPointerException();
        }
        this.remoteUser = remoteUser;
    }

    public String getSendPath()
    {
        return this.sendPath;
    }

    void setSendPath(String sendPath)
    {
        if (sendPath == null)
        {
            throw new NullPointerException();
        }
        this.sendPath = sendPath;
    }

    public String getSavingPath()
    {
        return this.savingPath;
    }

    void setSavingPath(String savingPath)
    {
        if (savingPath == null)
        {
            throw new NullPointerException();
        }
        this.savingPath = savingPath;
    }

    public long getSize()
    {
        return this.size;
    }

    void setSize(long size)
    {
        if (size < 0L)
        {
            throw new IllegalArgumentException("size could not less than zero.");
        }
        this.size = size;
    }

    public boolean isSender()
    {
        return this.isSender;
    }

    void setSender(boolean isSender)
    {
        this.isSender = isSender;
    }

    public String getExtraDescription()
    {
        return this.extraDescription;
    }

    void setExtraDescription(String extraDescription)
    {
        this.extraDescription = extraDescription;
    }

    public Object getTag()
    {
        return this.tag;
    }

    public void setTag(Object tag)
    {
        if (tag == null)
        {
            throw new NullPointerException();
        }
        this.tag = tag;
    }

    SelectionKey getSelectionKey()
    {
        return this.transferKey;
    }

    void setSelectionKey(SelectionKey transferKey)
    {
        if (transferKey == null)
        {
            throw new NullPointerException();
        }
        this.transferKey = transferKey;
    }
}
