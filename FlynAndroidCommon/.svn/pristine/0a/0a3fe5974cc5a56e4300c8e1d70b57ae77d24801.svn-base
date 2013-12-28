/* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */
package com.flyn.mvc.command;

import com.flyn.mvc.common.IResponseListener;
import com.flyn.mvc.common.Request;
import com.flyn.mvc.common.Response;

public class IdentityCommand extends Command
{
    @Override
    protected void executeCommand()
    {
        Request request = getRequest();
        Response response = new Response();
        response.setTag(request.getTag());
        response.setData(request.getData());
        response.setActivityKey(request.getActivityKey());
        response.setActivityKeyResID(request.getActivityKeyResID());
        setResponse(response);
        notifyListener(true);
    }

    protected void notifyListener(boolean success)
    {
        IResponseListener responseListener = getResponseListener();
        if (responseListener != null)
        {
            sendMessage(command_success);
        }
    }
}
