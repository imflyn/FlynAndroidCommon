package com.greatwall.util.command;

import java.util.concurrent.LinkedBlockingQueue;

public class CommandQueue
{
    private LinkedBlockingQueue<ICommand> mQueue = new LinkedBlockingQueue<ICommand>();

    public void enqueue(ICommand cmd)
    {
        this.mQueue.add(cmd);
    }

    public ICommand getNextCommand()
    {
        ICommand cmd = null;
        try
        {
            cmd = this.mQueue.take();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return cmd;
    }

    public void clear()
    {
        this.mQueue.clear();
    }
}
