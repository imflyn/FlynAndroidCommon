package com.greatwall.util.command;

import java.lang.ref.WeakReference;

public abstract class BaseCommand implements ICommand
{
    private WeakReference<Request> mRequest;
    private WeakReference<Response> mResponse;
    private WeakReference<AbstractResponseListener> mResponseListener;
    private boolean mTerminated;

    @Override
    public Request getRequest()
    {
        return this.mRequest.get();
    }

    @Override
    public void setRequest(Request request)
    {
        this.mRequest = new WeakReference<Request>(request);
    }

    @Override
    public Response getResponse()
    {
        return this.mResponse.get();
    }

    @Override
    public void setResponse(Response response)
    {
        this.mResponse = new WeakReference<Response>(response);
    }

    @Override
    public AbstractResponseListener getResponseListener()
    {
        return this.mResponseListener.get();
    }

    @Override
    public void setResponseListener(AbstractResponseListener responseListener)
    {
        this.mResponseListener = new WeakReference<AbstractResponseListener>(responseListener);
    }

    @Override
    public boolean isTerminated()
    {
        return this.mTerminated;
    }

    @Override
    public void setTerminated(boolean terminated)
    {
        this.mTerminated = terminated;
    }

}
