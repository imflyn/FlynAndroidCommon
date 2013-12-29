/* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */
package com.flyn.mvc.command;

import com.flyn.mvc.common.IResponseListener;
import com.flyn.mvc.common.Request;
import com.flyn.mvc.common.Response;

/**
 * @Title ICommand
 * @Description ICommand一个命令接口所有命令需要从此实现
 */
public interface ICommand
{
    Request getRequest();

    void setRequest(Request request);

    Response getResponse();

    void setResponse(Response response);

    void execute();

    IResponseListener getResponseListener();

    void setResponseListener(IResponseListener listener);

    void setTerminated(boolean terminated);

    boolean isTerminated();

}
