
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
 * A TextItem is a very basic item that only contains a single text. The text
 * will be displayed on a single line on screen.
 * 
 * @author Cyril Mottier
 */
public class TextItem extends Item
{

    /**
     * The item's text.
     */
    public String text;

    /**
     * @hide
     */
    public TextItem()
    {
    }

    /**
     * Create a new TextItem with the specified text.
     * 
     * @param text
     *            The text used to create this item.
     */
    public TextItem(String text)
    {
        this.text = text;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_text_item_view, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
    {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.TextItem);
        text = a.getString(R.styleable.TextItem_text);
        a.recycle();
    }

}
