package com.greatwall.util.command;

public interface ICommand
{
    Request getRequest();

    void setRequest(Request request);

    Response getResponse();

    void setResponse(Response response);

    void execute();

    AbstractResponseListener getResponseListener();

    void setResponseListener(AbstractResponseListener listener);

    boolean isTerminated();

    void setTerminated(boolean terminated);

}
