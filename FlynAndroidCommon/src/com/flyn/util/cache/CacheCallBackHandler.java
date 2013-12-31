package com.flyn.util.cache;

/**
 * @Description 缓存结果的回调类
 */
public class CacheCallBackHandler<T>
{
    /**
     * 缓存运行开始
     * 
     * @param t
     *            响应的对象
     * @param data
     *            数据唯一标识
     */
    public void onStart(T t, Object data)
    {
    }

    /**
     * 缓存运行开始
     * 
     * @param t
     *            响应的对象
     * @param data
     *            数据唯一标识
     * @param inputStream
     *            标识对应的响应数据
     */
    public void onSuccess(T t, Object data, byte[] buffer)
    {
    }

    /**
     * 缓存运行失败
     * 
     * @param t
     *            响应的对象
     * @param data
     *            数据唯一标识
     */
    public void onFailure(T t, Object data)
    {

    }
}
