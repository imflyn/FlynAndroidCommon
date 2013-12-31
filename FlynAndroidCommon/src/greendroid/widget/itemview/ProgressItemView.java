package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.ProgressItem;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flynAndroidCommon.R;

/**
 * View representation of the {@link ProgressItem}.
 * 
 * @author Cyril Mottier
 */
public class ProgressItemView extends FrameLayout implements ItemView
{

    private ProgressBar mProgressBar;
    private TextView    mTextView;

    public ProgressItemView(Context context)
    {
        this(context, null);
    }

    public ProgressItemView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ProgressItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void prepareItemView()
    {
        mProgressBar = (ProgressBar) findViewById(R.id.gd_progress_bar);
        mTextView = (TextView) findViewById(R.id.gd_text);
    }

    @Override
    public void setObject(Item object)
    {
        final ProgressItem item = (ProgressItem) object;
        mProgressBar.setVisibility(item.isInProgress ? View.VISIBLE : View.GONE);
        mTextView.setText(item.text);
    }

}
