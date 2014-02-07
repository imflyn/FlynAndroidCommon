package com.greatwall.util.command;

public final class CommandQueueManager
{
    private static CommandQueueManager instance;
    private boolean                    mInitialized = false;
    private ThreadPool                 mPool;
    private CommandQueue               mQueue;

    private CommandQueueManager()
    {
    }

    public static CommandQueueManager getInstance()
    {
        if (instance == null)
        {
            synchronized (CommandQueueManager.class)
            {
                if (instance == null)
                    instance = new CommandQueueManager();
            }

        }
        return instance;
    }

    public void initialize()
    {
        if (!this.mInitialized)
        {
            this.mQueue = new CommandQueue();
            this.mPool = ThreadPool.getInstance();

            this.mPool.start();
            this.mInitialized = true;
        }
    }

    public ICommand getNextCommand()
    {
        ICommand cmd = this.mQueue.getNextCommand();
        return cmd;
    }

    public void enqueue(ICommand cmd)
    {
        this.mQueue.enqueue(cmd);
        this.mPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                getNextCommand().execute();
            }
        });
    }
    
    public void dequeue(ICommand cmd)
    {
        this.mQueue.enqueue(cmd);
    }

    public void clear()
    {
        this.mQueue.clear();
    }

    public void shutdown()
    {
        if (this.mInitialized)
        {
            this.mQueue.clear();
            this.mPool.shutdown();
            this.mInitialized = false;
        }
    }
}
