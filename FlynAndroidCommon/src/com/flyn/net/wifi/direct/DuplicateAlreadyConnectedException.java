package com.flyn.net.wifi.direct;

import java.io.IOException;

public class DuplicateAlreadyConnectedException extends IOException
{
    private static final long serialVersionUID = 1L;

    public DuplicateAlreadyConnectedException()
    {
    }

    public DuplicateAlreadyConnectedException(String message)
    {
        super(message);
    }
}
