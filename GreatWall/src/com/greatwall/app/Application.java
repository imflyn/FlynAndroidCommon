package com.greatwall.app;


public class Application extends android.app.Application
{

    // singleton
    private static Application mContext                = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = this;
    }

    public static Application getInstance()
    {
        if (mContext == null)
            throw new IllegalStateException();
        return mContext;
    }

   
}
