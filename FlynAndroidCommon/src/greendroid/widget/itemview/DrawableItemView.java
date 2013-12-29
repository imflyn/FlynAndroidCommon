
package greendroid.widget.itemview;

import greendroid.widget.item.DrawableItem;
import greendroid.widget.item.Item;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flynAndroidCommon.R;
/**
 * View representation of the {@link DrawableItem}.
 * 
 * @author Cyril Mottier
 */
public class DrawableItemView extends LinearLayout implements ItemView
{

    private TextView  mTextView;
    private ImageView mImageView;

    public DrawableItemView(Context context)
    {
        this(context, null);
    }

    public DrawableItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void prepareItemView()
    {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mImageView = (ImageView) findViewById(R.id.gd_drawable);
    }

    @Override
    public void setObject(Item object)
    {
        final DrawableItem item = (DrawableItem) object;
        mTextView.setText(item.text);

        final int drawableId = item.drawableId;
        if (drawableId == 0)
        {
            mImageView.setVisibility(View.GONE);
        } else
        {
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageResource(drawableId);
        }
    }

}
