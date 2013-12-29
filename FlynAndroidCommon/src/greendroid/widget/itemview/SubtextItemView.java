
package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.SubtextItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flynAndroidCommon.R;
/**
 * View representation of the {@link SubtextItem}.
 * 
 * @author Cyril Mottier
 */
public class SubtextItemView extends LinearLayout implements ItemView
{

    private TextView mTextView;
    private TextView mSubtextView;

    public SubtextItemView(Context context)
    {
        this(context, null);
    }

    public SubtextItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void prepareItemView()
    {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mSubtextView = (TextView) findViewById(R.id.gd_subtext);
    }

    @Override
    public void setObject(Item object)
    {
        final SubtextItem item = (SubtextItem) object;
        mTextView.setText(item.text);
        mSubtextView.setText(item.subtext);
    }

}
