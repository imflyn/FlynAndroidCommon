package com.greatwall.util.command;

abstract class AbstractCommandFactory
{
    public abstract <T extends ICommand> T createCommond(Class<T> T);
}
