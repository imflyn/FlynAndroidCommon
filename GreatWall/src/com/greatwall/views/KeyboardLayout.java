package com.greatwall.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.greatwall.util.LogManager;

public class KeyboardLayout extends RelativeLayout
{

    private onSizeChangedListener mChangedListener;
    private boolean               mShowKeyboard = false;

    public KeyboardLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public KeyboardLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public KeyboardLayout(Context context)
    {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogManager.i(KeyboardLayout.class, "onMeasure-----------");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
        LogManager.i(KeyboardLayout.class, "onLayout-----------");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        LogManager.d(KeyboardLayout.class, "--------------------------------------------------------------");
        LogManager.d(KeyboardLayout.class, "w----" + w + "\n" + "h-----" + h + "\n" + "oldW-----" + oldw + "\noldh----" + oldh);
        if (null != mChangedListener && 0 != oldw && 0 != oldh)
        {
            if (h < oldh)
            {
                mShowKeyboard = true;
            } else
            {
                mShowKeyboard = false;
            }
            mChangedListener.onChanged(mShowKeyboard);
            LogManager.d(KeyboardLayout.class, "mShowKeyboard-----      " + mShowKeyboard);
        }
    }

    public void setOnSizeChangedListener(onSizeChangedListener listener)
    {
        mChangedListener = listener;
    }

    interface onSizeChangedListener
    {

        void onChanged(boolean showKeyboard);
    }

}
