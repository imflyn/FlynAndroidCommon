package com.flyn.net.wifi.direct;

import java.io.IOException;

public class SpaceNotEnoughException extends IOException
{
    private static final long serialVersionUID = 1L;

    public SpaceNotEnoughException()
    {
    }

    public SpaceNotEnoughException(String message)
    {
        super(message);
    }
}
