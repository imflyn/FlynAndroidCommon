package com.greatwall.util.command;

public abstract class AbstractResponseListener
{

    public abstract void onSuccess(Response response);

    public abstract void onFailure(Response response);

    public void onStart()
    {

    }

    public void onRuning(Response response)
    {

    }

    public void onFinish()
    {

    }
}
