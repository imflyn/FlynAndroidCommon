package com.greatwall.util.command;


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

    abstractResponseListener getResponseListener();

    void setResponseListener(abstractResponseListener listener);

    void setTerminated(boolean terminated);

    boolean isTerminated();

}
