
package greendroid.widget.itemview;

import greendroid.widget.AsyncImageView;
import greendroid.widget.item.Item;
import greendroid.widget.item.ThumbnailItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flynAndroidCommon.R;
/**
 * View representation of the {@link ThumbnailItem}.
 * 
 * @author Cyril Mottier
 */
public class ThumbnailItemView extends RelativeLayout implements ItemView
{

    private TextView       mTextView;
    private TextView       mSubtitleView;
    private AsyncImageView mThumbnailView;

    public ThumbnailItemView(Context context)
    {
        this(context, null);
    }

    public ThumbnailItemView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ThumbnailItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void prepareItemView()
    {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mSubtitleView = (TextView) findViewById(R.id.gd_subtitle);
        mThumbnailView = (AsyncImageView) findViewById(R.id.gd_thumbnail);
    }

    @Override
    public void setObject(Item object)
    {
        final ThumbnailItem item = (ThumbnailItem) object;
        mTextView.setText(item.text);
        mSubtitleView.setText(item.subtitle);
        mThumbnailView.setDefaultImageResource(item.drawableId);
        mThumbnailView.setUrl(item.drawableURL);
    }
}
