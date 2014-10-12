package com.greatwall.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView.LayoutParams;
import android.widget.FrameLayout;

/**
 * 解决android2.2 adjustResize但是Layout没执行onSizeChenged的bug
 */
public class AndroidBug5497Workaround
{

    // For more information, see
    // https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that
    // already has its content view set.

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private AndroidBug5497Workaround(Activity activity)
    {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);

        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            public void onGlobalLayout()
            {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    public static void assistActivity(Activity activity, int wantedHeight)
    {
        new AndroidBug5497Workaround(activity);
    }

    private void possiblyResizeChildOfContent()
    {
        // 键盘弹起后布局高度
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious)
        {
            // 总高度
            int screenHeight = mChildOfContent.getRootView().getHeight();
            // 键盘高度
            int keyboardHeight = screenHeight - usableHeightNow;
            if (keyboardHeight > (screenHeight / 4))
            {
                // keyboard probably just became visible
                // 这个地方需要根据自己的需要动态设置高度
                frameLayoutParams.height = screenHeight - keyboardHeight;
            } else
            {
                // keyboard probably just became hidden
                frameLayoutParams.height = LayoutParams.MATCH_PARENT;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight()
    {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

}
