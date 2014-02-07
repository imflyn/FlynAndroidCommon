package com.greatwall.util.command;

public final class CommandQueueManager
{
    private static CommandQueueManager instance;
    private boolean                    initialized = false;
    private ThreadPool                 pool;
    private CommandQueue               queue;
    private static final String        TAG         = CommandQueueManager.class.getName();

    private CommandQueueManager()
    {
    }

    public static CommandQueueManager getInstance()
    {
        if (instance == null)
        {
            instance = new CommandQueueManager();
        }
        return instance;
    }

    public void initialize()
    {
        if (!initialized)
        {
            queue = new CommandQueue();
            pool = ThreadPool.getInstance();

            pool.start();
            initialized = true;
        }
    }

    public ICommand getNextCommand()
    {
        ICommand cmd = queue.getNextCommand();
        return cmd;
    }

    public void addQueue(ICommand cmd)
    {
        queue.enqueue(cmd);
        pool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                getNextCommand().execute();
            }
        });
    }

    public void clear()
    {
        queue.clear();
    }

    public void shutdown()
    {
        if (initialized)
        {
            queue.clear();
            pool.shutdown();
            initialized = false;
        }
    }
}
