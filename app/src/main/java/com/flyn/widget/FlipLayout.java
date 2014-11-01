package com.flyn.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.flyn.util.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FlipLayout extends ViewGroup
{
    private static final int SNAP_VELOCITY = 600;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mInterceptedX;
    private int mCurScreen = -1;
    private int mTempCurScreen = -1;
    private boolean mIsRequestScroll = false;
    private boolean mScrollWhenSameScreen = false;

    private List<View> mNoGoneChildren = new ArrayList();

    private boolean mIsFlingOutOfRangeBreak = false;

    private boolean mShouldResetIsFlingOutOfRangeBreak = true;

    private boolean mIsFlingChangedWhenPressed = false;
    private boolean mIsIntercepted = false;
    private OnFlingListener listener;
    private boolean mCheckIndexForFragment = false;

    public FlipLayout(Context context)
    {
        this(context, null, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        this.mScroller = new Scroller(context);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int childLeft = 0;
        int childWidth = r - l;
        int childHeight = b - t;
        int noGoneChildCount = this.mNoGoneChildren.size();
        for (int i = 0; i < noGoneChildCount; i++)
        {
            View noGonechildView = this.mNoGoneChildren.get(i);
            noGonechildView.layout(childLeft, 0, childLeft + childWidth, childHeight);
            childLeft += childWidth;
        }

        boolean curScrollWhenSameScreen = this.mScrollWhenSameScreen;
        this.mScrollWhenSameScreen = false;
        if (this.mTempCurScreen == -1)
        {
            this.mTempCurScreen = 0;
            scrollTo(this.mTempCurScreen * getWidth(), 0);
            this.mCurScreen = this.mTempCurScreen;
            if (this.listener != null)
            {
                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        FlipLayout.this.listener.onFlingChanged(FlipLayout.this.mNoGoneChildren.get(FlipLayout.this.mTempCurScreen), FlipLayout.this.mTempCurScreen);
                    }
                });
            }
        } else
        {
            if (this.mTempCurScreen >= noGoneChildCount)
            {
                this.mScroller.forceFinished(true);
                int mTempCurScreenCopy = this.mTempCurScreen;
                this.mTempCurScreen = this.mCurScreen;
                throw new IllegalStateException("cur screen is out of range:" + mTempCurScreenCopy + "!");
            }

            if (this.mTempCurScreen == this.mCurScreen)
            {
                if (changed)
                {
                    this.mScroller.forceFinished(true);
                    scrollTo(this.mTempCurScreen * getWidth(), 0);
                } else if (curScrollWhenSameScreen)
                {
                    int scrollX = getScrollX();
                    int delta = this.mTempCurScreen * getWidth() - scrollX;
                    this.mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 2);
                    invalidate();
                }

            } else if (this.mIsRequestScroll)
            {
                int scrollX = getScrollX();
                int delta = this.mTempCurScreen * getWidth() - scrollX;
                this.mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 2);
                this.mTempCurScreen = this.mCurScreen;
                invalidate();
            } else
            {
                this.mScroller.forceFinished(true);
                scrollTo(this.mTempCurScreen * getWidth(), 0);
                this.mCurScreen = this.mTempCurScreen;
                if (this.listener != null)
                {
                    new Handler().post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            FlipLayout.this.listener.onFlingChanged(FlipLayout.this.mNoGoneChildren.get(FlipLayout.this.mTempCurScreen), FlipLayout.this.mTempCurScreen);
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != 1073741824)
        {
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != 1073741824)
        {
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        }

        this.mNoGoneChildren.clear();
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View childView = getChildAt(i);
            if (childView.getVisibility() == 8)
            {
                continue;
            }
            childView.measure(widthMeasureSpec, heightMeasureSpec);
            this.mNoGoneChildren.add(childView);
        }

        if (this.mNoGoneChildren.size() == 0)
        {
            throw new IllegalStateException("FlipLayout must have one NO-GONE child at least!");
        }
        if (this.mCheckIndexForFragment)
        {
            Comparator com = new Comparator()
            {
                @Override
                public int compare(Object view, Object view2)
                {
                    if ((!(view instanceof ViewGroup)) || (!(view2 instanceof ViewGroup)))
                    {
                        throw new IllegalStateException("if you call setCheckIndexForFragment(true),the child should only be added by Fragment");
                    }
                    ViewGroup viewWrap = (ViewGroup) view;
                    ViewGroup viewWrap2 = (ViewGroup) view2;
                    if ((viewWrap.getChildCount() != 1) || (viewWrap2.getChildCount() != 1))
                    {
                        throw new IllegalStateException("if you call setCheckIndexForFragment(true),the child should only be added by Fragment");
                    }
                    Object tag = viewWrap.getChildAt(0).getTag();
                    Object tag2 = viewWrap2.getChildAt(0).getTag();
                    if ((!(tag instanceof Integer)) || (!(tag2 instanceof Integer)))
                    {
                        throw new IllegalStateException("if you call setCheckIndexForFragment(true),should use setTag(tag) to set index in Fragment’s onCreateView(inflater,container,savedInstanceState)");
                    }
                    int index = ((Integer) tag).intValue();
                    int index2 = ((Integer) tag2).intValue();
                    if (index == index2)
                    {
                        return 0;
                    }
                    return index > index2 ? 1 : -1;
                }

            };
            Collections.sort(this.mNoGoneChildren, com);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setToScreen(int whichScreen)
    {
        if (whichScreen < 0)
        {
            throw new IllegalArgumentException("whichScreen should equals or great than zero.");
        }
        if (whichScreen == this.mCurScreen)
        {
            return;
        }
        this.mTempCurScreen = whichScreen;
        this.mIsRequestScroll = false;
        requestLayout();
    }

    public void scrollToScreen(int whichScreen)
    {
        if (whichScreen < 0)
        {
            throw new IllegalArgumentException("whichScreen should equals or great than zero.");
        }
        if (whichScreen == this.mCurScreen)
        {
            return;
        }
        this.mTempCurScreen = whichScreen;
        this.mIsRequestScroll = true;
        requestLayout();
    }

    public int getCurScreen()
    {
        return this.mCurScreen;
    }

    protected boolean checkFlingWhenScroll()
    {
        int scrollX = getScrollX();
        if (scrollX < 0)
        {
            if ((this.listener != null) && (!this.mIsFlingOutOfRangeBreak))
            {
                this.mIsFlingOutOfRangeBreak = this.listener.onFlingOutOfRange(false, -scrollX);
            }
            return false;
        }

        int noGoneChildCount = this.mNoGoneChildren.size();
        int screenWidth = getWidth();
        int maxScrollX = (noGoneChildCount - 1) * screenWidth;
        if (scrollX > maxScrollX)
        {
            if ((this.listener != null) && (!this.mIsFlingOutOfRangeBreak))
            {
                this.mIsFlingOutOfRangeBreak = this.listener.onFlingOutOfRange(true, scrollX - maxScrollX);
            }
            return false;
        }

        int destScreen = (scrollX + screenWidth / 2) / screenWidth;
        if (destScreen != this.mCurScreen)
        {
            this.mCurScreen = (this.mTempCurScreen = destScreen);
            if (this.listener != null)
            {
                this.listener.onFlingChanged(this.mNoGoneChildren.get(destScreen), destScreen);
            }
            return true;
        }
        return false;
    }

    @Override
    public void computeScroll()
    {
        if (this.mScroller.computeScrollOffset())
        {
            scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            postInvalidate();
            checkFlingWhenScroll();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!this.mIsIntercepted)
        {
            onInterceptTouchEventImpl(event);
            if (!this.mIsIntercepted)
            {
                return true;
            }
        }
        if (this.mVelocityTracker == null)
        {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);

        int action = event.getAction();
        float x = event.getX();
        switch (action)
        {
            case 0:
                LogManager.i(FlipLayout.class, "event down!");
                this.mScroller.forceFinished(true);
                return true;
            case 2:
                this.mScroller.forceFinished(true);
                if (this.mShouldResetIsFlingOutOfRangeBreak)
                {
                    this.mIsFlingOutOfRangeBreak = false;
                    this.mShouldResetIsFlingOutOfRangeBreak = false;
                }
                int deltaX = (int) (this.mInterceptedX - x);
                this.mInterceptedX = x;
                scrollBy(deltaX, 0);
                if (checkFlingWhenScroll())
                {
                    this.mIsFlingChangedWhenPressed = true;
                }
                return true;
            case 1:
            case 3:
                LogManager.i(FlipLayout.class, "event up/cancel!");
                this.mIsIntercepted = false;
                this.mShouldResetIsFlingOutOfRangeBreak = true;
                if (this.mIsFlingChangedWhenPressed)
                {
                    this.mIsFlingChangedWhenPressed = false;
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    this.mScrollWhenSameScreen = true;
                    requestLayout();
                } else
                {
                    this.mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityX = (int) this.mVelocityTracker.getXVelocity();
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    LogManager.i(FlipLayout.class, "velocityX:" + velocityX);
                    if ((velocityX > 600) && (this.mCurScreen > 0))
                    {
                        LogManager.i(FlipLayout.class, "snap left");
                        scrollToScreen(this.mCurScreen - 1);
                    } else if ((velocityX < -600) && (this.mCurScreen < this.mNoGoneChildren.size() - 1))
                    {
                        LogManager.i(FlipLayout.class, "snap right");
                        scrollToScreen(this.mCurScreen + 1);
                    } else
                    {
                        this.mScrollWhenSameScreen = true;
                        requestLayout();
                    }
                }
                return true;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return onInterceptTouchEventImpl(ev);
    }

    private boolean onInterceptTouchEventImpl(MotionEvent ev)
    {
        int action = ev.getAction();
        float x = ev.getX();
        float y = ev.getY();

        switch (action)
        {
            case 0:
                this.mLastMotionX = x;
                this.mLastMotionY = y;
                boolean isFinished = this.mScroller.isFinished();
                if (isFinished)
                {
                    return false;
                }

                this.mInterceptedX = x;
                this.mIsIntercepted = true;
                return true;
            case 2:
                float xDistence = Math.abs(x - this.mLastMotionX);
                float yDistence = Math.abs(y - this.mLastMotionY);
                if (xDistence > this.mTouchSlop)
                {
                    double angle = Math.toDegrees(Math.atan(yDistence / xDistence));
                    if (angle <= 45.0D)
                    {
                        this.mInterceptedX = x;
                        this.mIsIntercepted = true;
                        return true;
                    }
                }
                return false;
            case 1:
            case 3:
                return false;
        }
        return false;
    }

    public void setOnFlingListener(OnFlingListener listener)
    {
        this.listener = listener;
    }

    public void setCheckIndexForFragment(boolean checkIndexForFragment)
    {
        this.mCheckIndexForFragment = checkIndexForFragment;
    }

    public static abstract interface OnFlingListener
    {
        public abstract void onFlingChanged(View paramView, int paramInt);

        public abstract boolean onFlingOutOfRange(boolean paramBoolean, int paramInt);
    }
}