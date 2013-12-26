package com.talkingoa.android.app.ui.adapterview;

public abstract class AsyncDataExecutor
{
    public abstract Object onExecute(int paramInt1, DataHolder paramDataHolder, int paramInt2) throws Exception;

    public boolean isNotifyAsyncDataForAll()
    {
        return false;
    }
}