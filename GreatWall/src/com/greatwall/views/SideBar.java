package com.greatwall.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Adapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.greatwall.R;
import com.greatwall.util.DensityUtils;

public class SideBar extends View
{
    private static char[]  indexs          = new char[] { '#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private SectionIndexer sectionIndexter = null;
    private ListView       list;
    private int            color           = 0xff858c94;
    private TextView       mDialogText;
    private Paint          paint           = null;
    private WindowManager  windowManager;
    private boolean        mAttached       = false;

    public SideBar(Context context)
    {
        super(context);
        if (isInEditMode())
            return;
        init();
    }

    public SideBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        if (isInEditMode())
            return;
        init();
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (isInEditMode())
            return;

        init();
    }

    private void init()
    {
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    private void showText(String text)
    {
        if (null == mDialogText)
        {
            mDialogText = new TextView(getContext());
            mDialogText.setTextSize(80);
            // mDialogText.setBackgroundResource(R.drawable.btn_usually_selector);
            mDialogText.setGravity(Gravity.CENTER);
            mDialogText.setHeight(DensityUtils.dip2px(getContext(), 160));
            mDialogText.setWidth(DensityUtils.dip2px(getContext(), 160));
            // mDialogText.setTextColor(getResources().getColor(R.color.bg_white_light));
        }

        if (!mAttached)
        {
            mAttached = true;
            WindowManager.LayoutParams params = new LayoutParams();
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            params.alpha = 0.8f;
            params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
            params.flags = WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW;
            windowManager.addView(mDialogText, params);
        }

        mDialogText.setText(text);

    }

    private void hiddenText()
    {
        if (mAttached)
        {
            windowManager.removeView(mDialogText);
            mAttached = false;
        }
    }

    public void setListView(ListView listview)
    {
        list = listview;
        sectionIndexter = (SectionIndexer) listview.getAdapter();

    }

    public void setTextView(TextView mDialogText)
    {
        this.mDialogText = mDialogText;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {

        super.onTouchEvent(event);
        int i = (int) event.getY();

        int idx = i / (getMeasuredHeight() / indexs.length);
        if (idx >= indexs.length)
        {
            idx = indexs.length - 1;
        } else if (idx < 0)
        {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
        {

            setBackgroundResource(R.color.transparent_cover);

            showText(String.valueOf(indexs[idx]));
            int position = getSectionIndexer().getPositionForSection(indexs[idx]);

            if (position == -1)
            {
                return true;
            }
            list.setSelection(position + 1);
        } else
        {
            hiddenText();
        }
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            setBackgroundResource(android.R.color.transparent);
        }
        return true;
    }

    private SectionIndexer getSectionIndexer()
    {
        if (sectionIndexter == null)
        {
            Adapter adapter = list.getAdapter();

            if (adapter instanceof HeaderViewListAdapter)
            {
                sectionIndexter = (SectionIndexer) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            }
        }
        return sectionIndexter;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (paint == null)
        {
            paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(18 * getResources().getDisplayMetrics().density);
            paint.setStyle(Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
        }
        float widthCenter = getMeasuredWidth() / 2;
        if (indexs.length > 0)
        {
            float height = getMeasuredHeight() / indexs.length;
            for (int i = 0; i < indexs.length; i++)
            {
                canvas.drawText(String.valueOf(indexs[i]), widthCenter, (i + 1) * height, paint);
            }
        }
        super.onDraw(canvas);
    }

    public void setColor(int color)
    {
        this.color = color;
        this.invalidate();
    }
}
