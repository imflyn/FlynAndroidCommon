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

    /**
     * 从队列中获取Command
     * 
     * @return TAICommand
     */
    public ICommand getNextCommand()
    {
        ICommand cmd = queue.getNextCommand();
        return cmd;
    }

    /** 添加Command到队列中 */
    public void enqueue(ICommand cmd)
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

    /** 清除队列 */
    public void clear()
    {
        queue.clear();
    }

    /** 关闭队列 */
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
