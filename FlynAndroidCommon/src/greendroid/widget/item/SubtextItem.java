
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
 * <p>
 * A SubtextItem is really similar to a {@link SubtitleItem} as it displays two
 * pieces of text. The only difference between those two items are the number of
 * lines occupied by the content.
 * </p>
 * <ul>
 * <li>SubtextItem: subtext is displayed on several lines</li>
 * <li>{@link SubtitleItem}: subtitle is displayed on a single line</li>
 * </ul>
 * <p>
 * A SubtextItem may generally used to display a description text. As a result,
 * it is disabled by default
 * </p>
 * 
 * @author Cyril Mottier
 */
public class SubtextItem extends TextItem
{

    /**
     * The string that will be displayed above the title of the item (possibly
     * on several lines).
     */
    public String subtext;

    /**
     * @hide
     */
    public SubtextItem()
    {
        this(null);
    }

    /**
     * @hide
     */
    public SubtextItem(String text)
    {
        this(text, null);
    }

    /**
     * Constructs a new SubtextItem
     * 
     * @param text
     *            The main text for this item
     * @param subtext
     *            The subtext
     */
    public SubtextItem(String text, String subtext)
    {
        super(text);
        this.subtext = subtext;
        enabled = false;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_subtext_item_view, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
    {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.SubtextItem);
        subtext = a.getString(R.styleable.SubtextItem_subtext);
        a.recycle();
    }

}
