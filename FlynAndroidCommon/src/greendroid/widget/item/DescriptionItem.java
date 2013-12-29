
package greendroid.widget.item;

import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.view.ViewGroup;

import com.flynAndroidCommon.R;

/**
 * A description item displays a text on several lines. The default
 * implementation makes it disabled.
 * 
 * @author Cyril Mottier
 */
public class DescriptionItem extends TextItem
{

    /**
     * @hide
     */
    public DescriptionItem()
    {
        this(null);
    }

    /**
     * Creates a new DescriptionItem with the given description.
     * 
     * @param description
     *            The description for the current item.
     */
    public DescriptionItem(String description)
    {
        super(description);
        enabled = false;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_description_item_view, parent);
    }

}
