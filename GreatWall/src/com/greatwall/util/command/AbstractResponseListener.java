package com.greatwall.util.command;

/**
 * @Title IResponseListener
 * @Description IResponseListener是数据返回的一个监听器
 */
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
