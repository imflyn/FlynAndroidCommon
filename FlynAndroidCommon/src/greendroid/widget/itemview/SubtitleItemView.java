
package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.SubtitleItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flynAndroidCommon.R;
/**
 * View representation of the {@link SubtitleItem}.
 * 
 * @author Cyril Mottier
 */
public class SubtitleItemView extends LinearLayout implements ItemView
{

    private TextView mTextView;
    private TextView mSubtitleView;

    public SubtitleItemView(Context context)
    {
        this(context, null);
    }

    public SubtitleItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void prepareItemView()
    {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mSubtitleView = (TextView) findViewById(R.id.gd_subtitle);
    }

    @Override
    public void setObject(Item object)
    {
        final SubtitleItem item = (SubtitleItem) object;
        mTextView.setText(item.text);
        mSubtitleView.setText(item.subtitle);
    }

}
