package com.flyn.util;

import android.content.Context;

/**
 * @Title DensityUtils
 * @Description DensityUtils是一个像素与dp转换的工具
 */
public class DensityUtils
{
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * 
     * @param context
     * @param dpValue
     *            dp值
     * @return 返回像素值
     */
    public static int dip2px(Context context, float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * 
     * @param context
     * @param pxValue
     *            像素值
     * @return 返回dp值
     */
    public static int px2dip(Context context, float pxValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
