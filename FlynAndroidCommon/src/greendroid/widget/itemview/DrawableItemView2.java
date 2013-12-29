
package greendroid.widget.itemview;

import greendroid.widget.item.DrawableItem;
import greendroid.widget.item.DrawableItem2;
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
public class DrawableItemView2 extends LinearLayout implements ItemView
{

    private TextView  mTextView;
    private ImageView mImageView;
    private ImageView mImageView2;

    public DrawableItemView2(Context context)
    {
        this(context, null);
    }

    public DrawableItemView2(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void prepareItemView()
    {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mImageView = (ImageView) findViewById(R.id.gd_drawable);
        mImageView2 = (ImageView) findViewById(R.id.gd_drawable2);
    }

    @Override
    public void setObject(Item object)
    {
        final DrawableItem2 item = (DrawableItem2) object;
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
        final int drawableId2 = item.drawableId2;
        if (drawableId2 == 0)
        {
            mImageView2.setVisibility(View.GONE);
        } else
        {
            mImageView2.setVisibility(View.VISIBLE);
            mImageView2.setImageResource(drawableId2);
        }
    }

}
