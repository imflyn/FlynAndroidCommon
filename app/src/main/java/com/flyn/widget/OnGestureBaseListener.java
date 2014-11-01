package com.flyn.widget;

import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class OnGestureBaseListener implements GestureDetector.OnGestureListener
{

    protected int swipeMinDistance = 120;
    protected int swipeMinVelocity = 200;

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        float xDistance = e2.getX() - e1.getX();
        float yDistance = e2.getY() - e1.getY();
        if ((Math.abs(xDistance) >= this.swipeMinDistance) && (Math.abs(velocityX) >= this.swipeMinVelocity))
        {
            if (xDistance >= 0.0F)
            {
                return onSwipeRight(e1, e2, velocityX, velocityY);
            }
            return onSwipeLeft(e1, e2, velocityX, velocityY);
        }
        if ((Math.abs(yDistance) >= this.swipeMinDistance) && (Math.abs(velocityY) >= this.swipeMinVelocity))
        {
            if (yDistance >= 0.0F)
            {
                return onSwipeBottom(e1, e2, velocityX, velocityY);
            }
            return onSwipeTop(e1, e2, velocityX, velocityY);
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return false;
    }

    public abstract boolean onSwipeLeft(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2);

    public abstract boolean onSwipeRight(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2);

    public abstract boolean onSwipeTop(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2);

    public abstract boolean onSwipeBottom(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2);
}