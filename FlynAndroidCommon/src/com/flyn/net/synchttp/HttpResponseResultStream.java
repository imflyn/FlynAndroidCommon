package com.flyn.net.synchttp;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HttpResponseResultStream extends HttpResponseResult
{
    protected InputStream       resultStream = null;
    protected HttpURLConnection httpURLConn  = null;

    public InputStream getResultStream()
    {
        return this.resultStream;
    }

    public void setResultStream(InputStream resultStream)
    {
        this.resultStream = resultStream;
    }

    public HttpURLConnection getHttpURLConn()
    {
        return this.httpURLConn;
    }

    public void setHttpURLConn(HttpURLConnection httpURLConn)
    {
        this.httpURLConn = httpURLConn;
    }

    public void generateData() throws IOException
    {
        try
        {
            BufferedInputStream buffInput = new BufferedInputStream(this.resultStream);
            ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
            byte[] b = new byte[2048];
            int len;
            while ((len = buffInput.read(b)) > 0)
            {
                tempOutput.write(b, 0, len);
            }
            setData(tempOutput.toByteArray());
        } finally
        {
            close();
        }
    }

    public void close() throws IOException
    {
        try
        {
            if (this.resultStream != null)
                this.resultStream.close();
        } finally
        {
            if (this.httpURLConn != null)
                this.httpURLConn.disconnect();
        }
    }
}