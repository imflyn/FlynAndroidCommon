package com.greatwall.ui.interfaces;

public abstract class BaseUIEvent
{
    public int eventCode;

    public BaseUIEvent(int eventCode)
    {
        this.eventCode = eventCode;
    }

}
