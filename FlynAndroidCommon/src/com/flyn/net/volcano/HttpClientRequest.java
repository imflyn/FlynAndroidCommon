package com.flyn.net.volcano;

import java.io.IOException;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

public class HttpClientRequest extends Request
{
    private final AbstractHttpClient client;
    private final HttpContext        context;
    private final HttpUriRequest     request;

    public HttpClientRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, IResponseHandler responseHandler)
    {
        super(responseHandler);
        this.client = client;
        this.context = context;
        this.request = request;
    }

    @Override
    protected void makeRequest() throws IOException
    {

    }

    @Override
    protected void makeRequestWithRetries() throws IOException
    {

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        this.isCancelled = true;
        if (mayInterruptIfRunning && this.request != null && !this.request.isAborted())
        {
            this.request.abort();
        }
        return isCancelled();
    }

}
