package com.greatwall.util.command;

public abstract class AbstractResponseListener
{

    abstract void onSuccess(Response response);

    abstract void onFailure(Response response);

    void onStart()
    {

    }

    void onRuning(Response response)
    {

    }

    void onFinish()
    {

    }
}
