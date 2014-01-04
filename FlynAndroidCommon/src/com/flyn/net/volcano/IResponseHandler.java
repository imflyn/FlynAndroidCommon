package com.flyn.net.volcano;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public interface IResponseHandler
{
    void sendResponseMessage() throws IOException;

    void sendStartMessage();

    void sendFinishMessage();

    void sendProgress(int bytesWritten, int bytesTotal);

    void sendCancleMessage();

    void sendSuccessMessage(int statusCode, Map<String, String> headers, byte[] responseData);

    void sendFailureMessage(int statusCode, Map<String, String> headers, byte[] responseData, Throwable error);

    void sendRetryMessage(int retryNo);

    public URI getRequestURI();

    public void setRequestURI(URI uri);

    public Map<String, String> getRequestHeaders();

    public void setRequestHeaders(Map<String, String> requestHeaders);

    void setUseSynchronousMode(boolean useSynchronousMode);

    boolean getUseSynchronousMode();

}
