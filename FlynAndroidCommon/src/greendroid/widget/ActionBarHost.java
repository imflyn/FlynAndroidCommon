
package greendroid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.flynAndroidCommon.R;

public class ActionBarHost extends LinearLayout
{

    private ActionBar   mActionBar;
    private FrameLayout mContentView;

    public ActionBarHost(Context context)
    {
        this(context, null);
    }

    public ActionBarHost(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mActionBar = (ActionBar) findViewById(R.id.gd_action_bar);
        if (mActionBar == null || !(mActionBar instanceof ActionBar))
        {
            throw new IllegalArgumentException("No ActionBar with the id R.id.gd_action_bar found in the layout.");
        }

        mContentView = (FrameLayout) findViewById(R.id.gd_action_bar_content_view);
        if (mContentView == null || !(mContentView instanceof FrameLayout))
        {
            throw new IllegalArgumentException("No FrameLayout with the id R.id.gd_action_bar_content_view found in the layout.");
        }
    }

    public ActionBar getActionBar()
    {
        return mActionBar;
    }

    public FrameLayout getContentView()
    {
        return mContentView;
    }

}
