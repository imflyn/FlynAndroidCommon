package com.greatwall.util.command;

abstract class AbstractCommondFactory
{
    public abstract <T extends ICommand> T createCommond(Class<T> T);
}
