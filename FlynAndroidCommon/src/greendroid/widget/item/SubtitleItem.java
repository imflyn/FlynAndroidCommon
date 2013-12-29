
package greendroid.widget.item;

import greendroid.widget.itemview.ItemView;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.flynAndroidCommon.R;
/**
 * An {@link Item} that contains two Strings : a text and a subtitle. The
 * representation of this {@link Item} is a view containing two lines of text.
 * If you want to be sure, the subtitle can occupy more than a single line,
 * please use a {@link SubtextItem}
 * 
 * @author Cyril Mottier
 */
public class SubtitleItem extends TextItem
{

    /**
     * The subtitle of this item
     */
    public String subtitle;

    /**
     * @hide
     */
    public SubtitleItem()
    {
    }

    /**
     * Construct a new SubtitleItem with the specified text and subtitle.
     * 
     * @param text
     *            The text for this item
     * @param subtitle
     *            The item's subtitle
     */
    public SubtitleItem(String text, String subtitle)
    {
        super(text);
        this.subtitle = subtitle;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_subtitle_item_view, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
    {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.SubtitleItem);
        subtitle = a.getString(R.styleable.SubtitleItem_subtitle);
        a.recycle();
    }
}
