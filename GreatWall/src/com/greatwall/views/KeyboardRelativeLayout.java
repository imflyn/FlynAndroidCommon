package com.greatwall.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.greatwall.util.LogManager;

/**
 * 键盘弹起关闭监听的布局
 * @author V
 *
 */
public class KeyboardRelativeLayout extends RelativeLayout
{

    private onSizeChangedListener mChangedListener;
    private boolean               mShowKeyboard = false;

    public KeyboardRelativeLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;
    }

    public KeyboardRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public KeyboardRelativeLayout(Context context)
    {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        LogManager.d(KeyboardRelativeLayout.class, "w----" + w + "  h-----" + h + "  oldW-----" + oldw + "  oldh----" + oldh);
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
            LogManager.d(KeyboardRelativeLayout.class, "mShowKeyboard-----      " + mShowKeyboard);
        }
    }

    public void setOnSizeChangedListener(onSizeChangedListener listener)
    {
        mChangedListener = listener;
    }

    public interface onSizeChangedListener
    {

        void onChanged(boolean showKeyboard);
    }

}
