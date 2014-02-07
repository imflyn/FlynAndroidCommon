package com.greatwall.util.command;

import java.util.concurrent.LinkedBlockingQueue;

public class CommandQueue
{
    private LinkedBlockingQueue<ICommand> theQueue = new LinkedBlockingQueue<ICommand>();
    private static final String           TAG      = CommandQueue.class.getName();

    public CommandQueue()
    {
    }

    public void enqueue(ICommand cmd)
    {
        theQueue.add(cmd);
    }

    public ICommand getNextCommand()
    {
        ICommand cmd = null;
        try
        {
            cmd = theQueue.take();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return cmd;
    }

    public void clear()
    {
        theQueue.clear();
    }
}
