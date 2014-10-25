package com.greatwall.views;

import android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.greatwall.util.DensityUtils;

public class WindowToast
{
    private static final int TOAST_CANCEL = 0;
    private static final int TOAST_SHOW = 1;
    private static WindowToast window;
    private TextView tv;
    private WindowManager mWindowManager;
    private int mBackGroundResourceId = com.greatwall.R.drawable.toast_bg;
    private int mBackGroundColor = 0;
    private Handler mHandler;
    private Context mContext;

    public static WindowToast getInstance()
    {
        if (null == window)
        {
            synchronized (WindowToast.class)
            {
                if (null == window)
                {
                    window = new WindowToast();
                    window.init();
                }
            }
        }
        return window;
    }

    @SuppressLint("HandlerLeak")
    private void init()
    {
        if (Looper.myLooper() != Looper.getMainLooper())
        {
            throw new RuntimeException("Cannot instantiate outside UI thread.");
        }

        mContext = com.greatwall.app.Application.getInstance();

        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case TOAST_SHOW:
                        show((String) (msg.obj));
                        break;
                    case TOAST_CANCEL:
                        cancelCurrentToast();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void showMessage(final String msg)
    {
        if (msg == null || msg.trim().length() == 0)
        {
            return;
        }
        mHandler.obtainMessage(TOAST_SHOW, msg).sendToTarget();
    }

    public void showMessage(final int msg)
    {
        mHandler.obtainMessage(TOAST_SHOW, mContext.getString(msg)).sendToTarget();
    }

    private void show(String msg)
    {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (tv == null)
        {
            tv = new TextView(mContext);

            tv.setText(msg);
            tv.setTextSize(18);
            tv.setTextColor(mContext.getResources().getColor(R.color.white));
            if (mBackGroundResourceId > 0)
            {
                tv.setBackgroundResource(mBackGroundResourceId);
            }

            if (mBackGroundColor != 0)
            {
                tv.setBackgroundColor(mBackGroundColor);
            }
            tv.setPadding(5, 5, 5, 5);
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        if (tv.getParent() == null)
        {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.gravity = Gravity.BOTTOM;
            params.alpha = 0.85f;
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.height = DensityUtils.dip2px(48);
            params.width = DensityUtils.getScreenWidth(mContext) - DensityUtils.dip2px(32);
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.format = PixelFormat.TRANSLUCENT;
            params.verticalMargin = 0.17f;
            params.windowAnimations = R.style.Animation_Toast;
            mWindowManager.addView(tv, params);
            mHandler.sendEmptyMessageDelayed(TOAST_CANCEL, 2000);
        } else
        {
            mHandler.removeMessages(TOAST_CANCEL);
            mHandler.sendEmptyMessageDelayed(TOAST_CANCEL, 2000);
        }
    }

    public void cancelCurrentToast()
    {
        if (tv != null && tv.getParent() != null)
        {
            mWindowManager.removeView(tv);
            mWindowManager = null;
            tv = null;
        }
    }

    public void setBackGroundResourceId(int mBackGroundResourceId)
    {
        this.mBackGroundResourceId = mBackGroundResourceId;
        this.mBackGroundColor = 0;
    }

    public void setBackGroundColor(int color)
    {
        this.mBackGroundColor = color;
        this.mBackGroundResourceId = -1;
    }

}