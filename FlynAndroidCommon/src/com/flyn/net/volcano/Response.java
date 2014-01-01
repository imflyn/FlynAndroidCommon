package com.flyn.net.volcano;

import com.android.volley.VolleyError;

public class Response
{
        
    public interface ErrorListener
    {
        
        public void onErrorResponse(VolleyError error);
    }
}
