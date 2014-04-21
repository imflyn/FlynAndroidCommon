package com.flyn.memory;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.flyn.util.LogManager;

public final class MemoryManager
{
    public static BitmapFactory.Options createSampleSizeOptions(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels)
    {
        BitmapFactory.Options returnOptions = new BitmapFactory.Options();
        returnOptions.inDither = false;
        returnOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        returnOptions.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
        return returnOptions;
    }

    public static BitmapFactory.Options createSampleSizeOptions(int inSampleSize)
    {
        BitmapFactory.Options returnOptions = new BitmapFactory.Options();
        returnOptions.inDither = false;
        returnOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        returnOptions.inSampleSize = inSampleSize;
        return returnOptions;
    }

    public static BitmapFactory.Options createPurgeableOptions()
    {
        BitmapFactory.Options returnOptions = new BitmapFactory.Options();
        returnOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        returnOptions.inPurgeable = true;
        returnOptions.inInputShareable = true;
        return returnOptions;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels)
    {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8)
        {
            roundedSize = 1;
            while (roundedSize < initialSize)
            {
                roundedSize <<= 1;
            }
        } else
        {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels)
    {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = maxNumOfPixels == -1 ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = minSideLength == -1 ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound)
        {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1))
        {
            return 1;
        }
        if (minSideLength == -1)
        {
            return lowerBound;
        }

        return upperBound;
    }

    public static void releaseBitmaps(AdapterView<? extends Adapter> view, ReleaseBitmapsCallback callback)
    {
        int count = view.getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = view.getChildAt(i);
            if (callback.isHeaderOrFooter(child))
                continue;
            callback.releaseBitmaps(child);
        }
    }

    public static void recycleBitmaps(View view, RecycleBitmapsCallback callback)
    {
        if ((view == null) || (callback == null))
            throw new NullPointerException();
        View child;
        if ((view instanceof ViewGroup))
        {
            ViewGroup container = (ViewGroup) view;
            if (callback.isContinue(container))
            {
                for (int i = 0; i < container.getChildCount(); i++)
                {
                    child = container.getChildAt(i);
                    recycleBitmaps(child, callback);
                }
            }
        } else
        {
            List<Bitmap> bitmaps = callback.select(view);
            if (bitmaps != null)
            {
                for (Bitmap bitmap : bitmaps)
                {
                    bitmap.recycle();
                }
            }
        }
    }

    public static boolean isLowMemory(Context context)
    {
        ActivityManager actMgr = (ActivityManager) context.getSystemService("activity");
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        actMgr.getMemoryInfo(memoryInfo);
        LogManager.i(MemoryManager.class, "Avail Memory=" + (memoryInfo.availMem >> 20) + "M");
        LogManager.i(MemoryManager.class, "threshold=" + (memoryInfo.threshold >> 20) + "M");
        LogManager.i(MemoryManager.class, "Is Low Memory=" + memoryInfo.lowMemory);
        return memoryInfo.lowMemory;
    }

    public static abstract interface RecycleBitmapsCallback
    {
        public abstract boolean isContinue(ViewGroup paramViewGroup);

        public abstract List<Bitmap> select(View paramView);
    }

    public static abstract interface ReleaseBitmapsCallback
    {
        public abstract boolean isHeaderOrFooter(View paramView);

        public abstract void releaseBitmaps(View paramView);
    }
}
