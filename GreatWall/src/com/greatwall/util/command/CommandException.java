package com.greatwall.util.command;

public class CommandException extends Exception
{
    private static final long serialVersionUID = 1L;

    public CommandException()
    {
        super();
    }

    public CommandException(Throwable cause)
    {
        super(cause);
    }

    public CommandException(String exceptionMessage)
    {
        super(exceptionMessage);
    }

    public CommandException(String exceptionMessage, Throwable reason)
    {
        super(exceptionMessage, reason);
    }
}
