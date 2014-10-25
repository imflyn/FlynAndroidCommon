package com.greatwall.ui;

public abstract class BaseEvent
{
    public int eventCode;

    public BaseEvent(int eventCode)
    {
        super();
        this.eventCode = eventCode;
    }
}
