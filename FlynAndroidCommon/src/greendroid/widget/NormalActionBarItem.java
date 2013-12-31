package greendroid.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.flynAndroidCommon.R;

/**
 * Default implementation of an {@link ActionBarItem}. A
 * {@link NormalActionBarItem} is a simple {@link ActionBarItem} containing a
 * single icon.
 * 
 * @author Cyril Mottier
 */
public class NormalActionBarItem extends ActionBarItem
{

    @Override
    protected View createItemView()
    {
        return LayoutInflater.from(mContext).inflate(R.layout.gd_action_bar_item_base, mActionBar, false);
    }

    @Override
    protected void prepareItemView()
    {
        super.prepareItemView();
        final ImageButton imageButton = (ImageButton) mItemView.findViewById(R.id.gd_action_bar_item);
        imageButton.setImageDrawable(mDrawable);
        imageButton.setContentDescription(mContentDescription);
    }

    @Override
    protected void onContentDescriptionChanged()
    {
        super.onContentDescriptionChanged();
        mItemView.findViewById(R.id.gd_action_bar_item).setContentDescription(mContentDescription);
    }

    @Override
    protected void onDrawableChanged()
    {
        super.onDrawableChanged();
        ImageButton imageButton = (ImageButton) mItemView.findViewById(R.id.gd_action_bar_item);
        imageButton.setImageDrawable(mDrawable);
    }

}
