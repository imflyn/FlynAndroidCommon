package greendroid.widget.item;

import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.view.ViewGroup;

import com.flynAndroidCommon.R;

/**
 * A LongTextItem is very similar to a regular {@link TextItem}. The only
 * difference is it may display the text on several lines.
 * 
 * @author Cyril Mottier
 */
public class LongTextItem extends TextItem
{

    /**
     * @hide
     */
    public LongTextItem()
    {
        this(null);
    }

    /**
     * Create a new LongTextItem
     * 
     * @param text
     *            The text being used in this LongTextItem
     */
    public LongTextItem(String text)
    {
        super(text);
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_long_text_item_view, parent);
    }

}
