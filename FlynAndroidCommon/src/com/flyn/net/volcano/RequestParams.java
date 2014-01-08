package com.flyn.net.volcano;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.protocol.HTTP;

/**
 * 装载http请求参数(可以是字符串或文件)的容器
 * 
 * @author V
 * 
 */
public abstract class RequestParams
{
    protected ConcurrentHashMap<String, String>        urlParams;
    protected ConcurrentHashMap<String, FileWrapper>   fileParams;
    protected ConcurrentHashMap<String, Object>        urlParamsWithObjects;
    protected ConcurrentHashMap<String, StreamWrapper> streamParams;
    protected boolean                                  isRepeatable;
    protected boolean                                  useJsonStreamer;
    protected String                                   contentEncoding = HTTP.UTF_8;

    /**
     * 设置编码
     * 
     * @param encoding
     */
    public void setContentEncoding(final String encoding)
    {
        if (encoding != null)
            this.contentEncoding = encoding;
        else
            new NullPointerException("encoding is null ");
    }

    /**
     * 构造一个空的实例
     */
    public RequestParams()
    {
        this((Map<String, String>) null);
    }

    /**
     * 构造一个包含字符串键值对map的实例
     */
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

    /**
     * 构造一个只包含一组字符串键值对的map的实例
     */
    @SuppressWarnings("serial")
    public RequestParams(final String key, final String value)
    {
        this(new HashMap<String, String>()
        {
            {
                put(key, value);
            }
        });
    }

    /**
     * 构造一个包含多个字符串键值对的map的实例
     * 
     * @param keysAndValues
     */
    public RequestParams(Object... keysAndValues)
    {
        init();
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException("Supplied arguments must be even");
        for (int i = 0; i < len; i += 2)
        {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
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

    /**
     * 添加请求参数
     * 
     * @param key
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void add(String key, String value)
    {
        if (key != null && value != null)
        {
            Object params = this.urlParamsWithObjects.get(key);
            if (params == null)
            {
                params = new HashSet<String>();
                this.put(key, params);
            }
            if (params instanceof List)
            {
                ((List<Object>) params).add(value);
            } else if (params instanceof Set)
            {
                ((Set<Object>) params).add(value);
            }
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

    protected abstract String getParamString();

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

    public byte[] getBody(IResponseHandler progressHandler) throws IOException
    {
        if (this.useJsonStreamer)
        {
            return createJsonStreamData();
        } else if (this.streamParams.isEmpty() && this.fileParams.isEmpty())
        {
            return createFormData();
        } else
        {
            return createMultipartData(progressHandler);
        }
    }

    protected abstract byte[] createJsonStreamData();

    protected abstract byte[] createFormData();

    protected abstract byte[] createMultipartData(IResponseHandler progressHandler);
}
