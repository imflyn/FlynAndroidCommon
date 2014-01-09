package com.flyn.net.volcano;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HttpClient方法的请求参数容器
 * 
 * @author V
 * 
 */
public class HttpClientRequestParams extends RequestParams
{

    @Override
    protected byte[] createNormalData()
    {
        return getParamString().getBytes();
    }

    @Override
    protected byte[] createMultipartData(IResponseHandler progressHandler) throws IOException
    {
        MultiByteParser parser = new MultiByteParser(progressHandler);

        for (ConcurrentHashMap.Entry<String, String> entry : this.urlParams.entrySet())
        {
            parser.addPart(entry.getKey(), entry.getValue());
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : this.fileParams.entrySet())
        {
            FileWrapper fileWrapper = entry.getValue();
            parser.addPart(entry.getKey(), fileWrapper.file, fileWrapper.contentType);
        }

        for (Entry<String, StreamWrapper> entry : this.streamParams.entrySet())
        {
            StreamWrapper streamWrapper = entry.getValue();
            if (null != streamWrapper.inputStream)
                parser.addPart(entry.getKey(), streamWrapper.name, streamWrapper.inputStream, streamWrapper.contentType);
        }

        return parser.getData();
    }

}
