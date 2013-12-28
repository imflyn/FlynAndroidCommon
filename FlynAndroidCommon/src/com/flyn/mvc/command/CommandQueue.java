/* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */
package com.flyn.mvc.command;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Title CommandQueue
 * @Description CommandQueue维护一个Command
 */
public class CommandQueue
{
    private LinkedBlockingQueue<ICommand> theQueue = new LinkedBlockingQueue<ICommand>();
    private static final String           TAG      = "CommandQueue";

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
