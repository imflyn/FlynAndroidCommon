package com.greatwall.util.command;

abstract class AbstractCommandFactory
{
    public abstract <T extends ICommand> T createCommand(Class<T> T) throws CommandException;
}
