/* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */
package com.flyn.mvc.command;

import com.flyn.mvc.common.IResponseListener;
import com.flyn.mvc.common.Request;
import com.flyn.mvc.common.Response;

public abstract class BaseCommand implements ICommand
{
    private Request           request;
    private Response          response;
    private IResponseListener responseListener;
    private boolean           terminated;

    @Override
    public Request getRequest()
    {
        return request;
    }

    @Override
    public void setRequest(Request request)
    {
        this.request = request;
    }

    @Override
    public Response getResponse()
    {
        return response;
    }

    @Override
    public void setResponse(Response response)
    {
        this.response = response;
    }

    @Override
    public IResponseListener getResponseListener()
    {
        return responseListener;
    }

    @Override
    public void setResponseListener(IResponseListener responseListener)
    {
        this.responseListener = responseListener;
    }

    @Override
    public void setTerminated(boolean terminated)
    {
        this.terminated = terminated;
    }

    @Override
    public boolean isTerminated()
    {
        return terminated;
    }

}
