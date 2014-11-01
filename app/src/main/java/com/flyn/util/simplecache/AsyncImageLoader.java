package com.flyn.util.simplecache;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步加载图片资源
 */
public class AsyncImageLoader
{

    /**
     * 内存缓存
     */
    private MemoryCache mMemoryCache;
    /**
     * 文件缓存
     */
    private FileCache mFileCache;

    /**
     * 线程池
     */
    private ExecutorService mExecutorService;

    private Map<View, String> mImageViews = Collections.synchronizedMap(new WeakHashMap<View, String>());

    /**
     * 保存正在加载图片的url
     */
    private ArrayList<LoadPhotoTask> mTaskQueue = new ArrayList<LoadPhotoTask>();

    /**
     * 默认采用一个大小为5的线程池
     *
     * @param context
     * @param memoryCache 所采用的高速缓存
     * @param fileCache   所采用的文件缓存
     */
    public AsyncImageLoader(Context context)
    {
        mMemoryCache = MemoryCache.getMemoryCache(context);
        mFileCache = new FileCache();
        mExecutorService = Executors.newFixedThreadPool(6);
    }

    /**
     * 根据url加载相应的图片
     *
     * @param url
     * @param flag
     * @return 如果缓存里有则直接返回，如果没有则异步从文件或网络端获取
     */
    public void loadBitmap(View imageView, String url, int round)
    {
        if (imageView == null)
        {
            return;
        }
        mImageViews.put(imageView, url);
        enquequeLoadPhoto(url, imageView, round);
        // return bitmap;
    }

    /**
     * 加入图片下载队列
     *
     * @param url
     */
    private void enquequeLoadPhoto(String url, View imageView, int round)
    {
        // 如果任务已经存在，则不重新添加
        if (isTaskExisted(url, imageView))
        {
            return;
        }
        LoadPhotoTask task = new LoadPhotoTask(url, imageView, round);
        synchronized (mTaskQueue)
        {
            mTaskQueue.add(task);
        }
        mExecutorService.submit(task);
    }

    /**
     * 判断下载队列中是否已经存在该任务
     *
     * @param url
     * @return
     */
    private boolean isTaskExisted(String url, View view)
    {
        if (url == null)
        {
            return false;
        }
        synchronized (mTaskQueue)
        {
            int size = mTaskQueue.size();
            for (int i = 0; i < size; i++)
            {
                LoadPhotoTask task = mTaskQueue.get(i);
                if (task != null && task.getView().hashCode() == view.hashCode())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从缓存文件或者网络端获取图片
     *
     * @param url
     * @return
     */
    private Bitmap getBitmapByUrl(String url)
    {

        Bitmap b = mFileCache.getImage(url);
        if (b != null)
        {
            return b;
        }
        b = ImageDownloader.downloadBitmap(url);

        mFileCache.saveBitmap(b, url);
        return b;
    }

    /**
     * 判断该ImageView是否已经被复用
     *
     * @param imageView
     * @param url
     * @return
     */
    private boolean imageViewReused(View imageView, String url)
    {
        if (null == imageView)
        {
            return false;
        }
        String tag = mImageViews.get(imageView);
        if (tag == null || !tag.equals(url))
        {
            mImageViews.remove(imageView);
            return true;
        }
        return false;
    }

    private void removeTask(LoadPhotoTask task)
    {
        synchronized (mTaskQueue)
        {
            mTaskQueue.remove(task);
        }
    }

    /**
     * 释放资源
     */
    public void destroy()
    {
        if (null != mMemoryCache)
        {
            mMemoryCache.clearCache();
            mMemoryCache = null;
        }
        if (null != mImageViews)
        {
            mImageViews.clear();
            mImageViews = null;
        }
        if (null != mTaskQueue)
        {
            mTaskQueue.clear();
            mTaskQueue = null;
        }
        if (null != mExecutorService)
        {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }

    class LoadPhotoTask implements Runnable
    {
        private String url;
        private View imageView;
        private int round;

        LoadPhotoTask(String url, View imageView, int round)
        {
            this.url = url;
            this.imageView = imageView;
            this.round = round;
        }

        @Override
        public void run()
        {
            Bitmap bitmap = mMemoryCache.getBitmapFromCache(url);
            if (bitmap != null)
            {
                removeTask(this);
                BitmapDisplayer bd = new BitmapDisplayer(bitmap, imageView, url);
                Activity a = (Activity) imageView.getContext();
                a.runOnUiThread(bd);
                return;
            }
            if (imageViewReused(imageView, url))
            {
                removeTask(this);
                return;
            }
            Bitmap bmp = getBitmapByUrl(url);
            if (round != 0 && bmp != null)
            {
                // bmp = ImageUtils.getRoundedCornerBitmap(bmp, round);
            }
            mMemoryCache.addBitmapToCache(url, bmp);
            if (imageViewReused(imageView, url))
            {
                removeTask(this);
                return;
            }
            removeTask(this);
            BitmapDisplayer bd = new BitmapDisplayer(bmp, imageView, url);
            Activity a = (Activity) imageView.getContext();
            a.runOnUiThread(bd);

        }

        public String getUrl()
        {
            return url;
        }

        public View getView()
        {
            return imageView;
        }
    }

    /**
     * Used to display bitmap in the UI thread
     */
    class BitmapDisplayer implements Runnable
    {
        private Bitmap bitmap;
        private View imageView;
        private String url;

        public BitmapDisplayer(Bitmap b, View imageView, String url)
        {
            bitmap = b;
            this.imageView = imageView;
            this.url = url;
        }

        @Override
        public void run()
        {
            if (imageViewReused(imageView, url))
            {
                return;
            }
            if (bitmap != null)
            {
                if (imageView instanceof ImageView)
                {
                    mImageViews.remove(imageView);
                }
            }

        }
    }
}