package greendroid.widget;

import android.view.LayoutInflater;
import android.view.View;

import com.flynAndroidCommon.R;

/**
 * An extension of a {@link NormalActionBarItem} that supports a loading states.
 * When in loading state, a {@link LoaderActionBarItem} display an indeterminate
 * circular ProgressBar. This item is very handful with application fetching
 * data from the network or performing long background tasks.
 * 
 * @author Cyril Mottier
 */
public class LoaderActionBarItem extends NormalActionBarItem
{

    private boolean mLoading;
    private View    mButton;
    private View    mProgressBar;

    public LoaderActionBarItem()
    {
        mLoading = false;
    }

    @Override
    protected View createItemView()
    {
        return LayoutInflater.from(mContext).inflate(R.layout.gd_action_bar_item_loader, mActionBar, false);
    }

    @Override
    protected void prepareItemView()
    {
        super.prepareItemView();
        mButton = mItemView.findViewById(R.id.gd_action_bar_item);
        mProgressBar = mItemView.findViewById(R.id.gd_action_bar_item_progress_bar);
    }

    @Override
    protected void onItemClicked()
    {
        super.onItemClicked();
        setLoading(true);
    }

    /**
     * Sets the loading state of this {@link LoaderActionBarItem}.
     * 
     * @param loading
     *            The new loading state. If true, an indeterminate ProgressBar
     *            is displayed. When false (default value) the
     *            {@link ActionBarItem} behaves exactly like a regular
     *            {@link NormalActionBarItem}.
     */
    public void setLoading(boolean loading)
    {
        if (loading != mLoading)
        {
            mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            mButton.setVisibility(loading ? View.GONE : View.VISIBLE);
            mLoading = loading;
        }
    }
}
