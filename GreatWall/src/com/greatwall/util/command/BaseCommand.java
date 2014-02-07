package com.greatwall.util.command;


public abstract class BaseCommand implements ICommand
{
    private Request           mRequest;
    private Response          mResponse;
    private abstractResponseListener mResponseListener;
    private boolean           mTerminated;

    @Override
    public Request getRequest()
    {
        return mRequest;
    }

    @Override
    public void setRequest(Request request)
    {
        this.mRequest = request;
    }

    @Override
    public Response getResponse()
    {
        return mResponse;
    }

    @Override
    public void setResponse(Response response)
    {
        this.mResponse = response;
    }

    @Override
    public abstractResponseListener getResponseListener()
    {
        return mResponseListener;
    }

    @Override
    public void setResponseListener(abstractResponseListener responseListener)
    {
        this.mResponseListener = responseListener;
    }

    @Override
    public void setTerminated(boolean terminated)
    {
        this.mTerminated = terminated;
    }

    @Override
    public boolean isTerminated()
    {
        return mTerminated;
    }

}
