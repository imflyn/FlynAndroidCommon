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
 * A DrawableItem displays a single Drawable on the left of the itemview and a
 * description text on the right. A DrawableItem takes care of adapting itself
 * depending on the presence of its Drawable.
 * 
 * @author Cyril Mottier
 */
public class DrawableItem2 extends TextItem
{

    /**
     * The resource identifier for the Drawable.
     */
    public int drawableId;
    public int drawableId2;

    /**
     * @hide
     */
    public DrawableItem2()
    {
        this(null);
    }

    /**
     * Constructs a new DrawableItem that has no Drawable and displays the given
     * text. Used as it, a DrawableItem is very similar to a TextItem
     * 
     * @param text
     *            The text of this DrawableItem
     */
    public DrawableItem2(String text)
    {
        this(text, 0, 0);
    }

    /**
     * Constructs a new DrawableItem using the specified text and Drawable
     * 
     * @param text
     *            The text of this DrawableItem
     * @param drawableId
     *            The resource identifier of the Drawable
     */
    public DrawableItem2(String text, int drawableId)
    {
        super(text);
        this.drawableId = drawableId;
    }

    public DrawableItem2(String text, int drawableId, int drawableId2)
    {
        super(text);
        this.drawableId = drawableId;
        this.drawableId2 = drawableId2;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_drawable_item_view2, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
    {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.DrawableItem);
        drawableId = a.getResourceId(R.styleable.DrawableItem_drawable, 0);
        drawableId2 = a.getResourceId(R.styleable.DrawableItem_drawable2, 0);
        a.recycle();
    }

}
