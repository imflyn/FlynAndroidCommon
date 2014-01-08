package com.flyn.net.volcano;


/**
 * HttpClient方法的请求参数容器
 * 
 * @author V
 * 
 */
public class HttpClientRequestParams extends RequestParams
{


    @Override
    protected byte[] createJsonStreamData()
    {
        return null;
    }

    @Override
    protected byte[] createNormalData()
    {
            return getParamString().getBytes();
    }

    @Override
    protected byte[] createMultipartData(IResponseHandler progressHandler)
    {
        return null;
    }

}
