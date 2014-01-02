package com.flyn.net.volcano;


public class Response
{
        
    public interface ErrorListener
    {
        public void onErrorResponse(HttpError error);
    }
}
