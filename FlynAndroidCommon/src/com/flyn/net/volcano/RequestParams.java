package com.flyn.net.volcano;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;

public abstract class RequestParams
{
    protected ConcurrentHashMap<String, String>        urlParams;
    protected ConcurrentHashMap<String, FileWrapper>   fileParams;
    protected ConcurrentHashMap<String, Object>        urlParamsWithObjects;
    protected ConcurrentHashMap<String, StreamWrapper> streamParams;
    protected boolean                                  isRepeatable;
    protected boolean                                  useJsonStreamer;
    protected String                                   contentEncoding = HTTP.UTF_8;

    public void setContentEncoding(final String encoding)
    {
        if (encoding != null)
            this.contentEncoding = encoding;
        else
            new NullPointerException("encoding is null ");
    }

    public RequestParams()
    {
        this((Map<String, String>) null);
    }

    public RequestParams(Map<String, String> source)
    {
        init();
        if (source != null)
        {
            for (Map.Entry<String, String> entry : source.entrySet())
            {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public RequestParams(final String key, final String value)
    {
        this(new HashMap<String, String>()
        {
            {
                put(key, value);
            }
        });
    }

    public void setHttpEntityIsRepeatable(boolean isRepeatable)
    {
        this.isRepeatable = isRepeatable;
    }

    public void setUseJsonStreamer(boolean useJsonStreamer)
    {
        this.useJsonStreamer = useJsonStreamer;
    }

    public void init()
    {
        this.urlParams = new ConcurrentHashMap<String, String>();
        this.fileParams = new ConcurrentHashMap<String, FileWrapper>();
        this.streamParams = new ConcurrentHashMap<String, StreamWrapper>();
        this.urlParamsWithObjects = new ConcurrentHashMap<String, Object>();
    }

    public void put(String key, String value)
    {
        if (key != null && value != null)
        {
            this.urlParams.put(key, value);
        }
    }

    public void put(String key, Object value)
    {
        if (key != null && value != null)
        {
            this.urlParamsWithObjects.put(key, value);
        }
    }

    public void put(String key, File file) throws FileNotFoundException
    {
        put(key, file, null);
    }

    public void put(String key, File file, String contentType) throws FileNotFoundException
    {
        if (file == null || !file.exists())
            throw new FileNotFoundException("wrong file argument was passed");
        if (key != null)
        {
            this.fileParams.put(key, new FileWrapper(file, contentType));
        }
    }

    public void put(String key, InputStream stream, String name, String contentType)
    {
        if (key != null && stream != null)
        {
            this.streamParams.put(key, new StreamWrapper(stream, name, contentType));
        }
    }

    public void remove(String key)
    {
        this.urlParams.remove(key);
        this.fileParams.remove(key);
        this.streamParams.remove(key);
        this.urlParamsWithObjects.remove(key);
    }

    protected abstract List<?> getParamsList();

    protected abstract List<?> getParamsList(String key, Object value);

    public static class FileWrapper
    {
        public File   file;
        public String contentType;

        public FileWrapper(File file, String contentType)
        {
            this.file = file;
            this.contentType = contentType;
        }
    }

    public static class StreamWrapper
    {
        public InputStream inputStream;
        public String      name;
        public String      contentType;

        public StreamWrapper(InputStream inputStream, String name, String contentType)
        {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
        }
    }

    public HttpEntity getEntity(ResponseHandlerInterface progressHandler) throws IOException
    {
        if (useJsonStreamer)
        {
            return createJsonStreamerEntity();
        } else if (streamParams.isEmpty() && fileParams.isEmpty())
        {
            return createFormEntity();
        } else
        {
            return createMultipartEntity(progressHandler);
        }
    }

    public abstract HttpEntity createJsonStreamerEntity();

    public abstract HttpEntity createFormEntity();

    public abstract HttpEntity createMultipartEntity(ResponseHandlerInterface progressHandler);
}
