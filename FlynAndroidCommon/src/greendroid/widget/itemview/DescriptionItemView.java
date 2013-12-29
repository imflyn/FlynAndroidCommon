
package greendroid.widget.itemview;

import greendroid.widget.item.DescriptionItem;
import greendroid.widget.item.Item;
import greendroid.widget.item.TextItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * View representation of the {@link DescriptionItem}.
 * 
 * @author Cyril Mottier
 */
public class DescriptionItemView extends TextView implements ItemView
{

    public DescriptionItemView(Context context)
    {
        this(context, null);
    }

    public DescriptionItemView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public DescriptionItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void prepareItemView()
    {
    }

    @Override
    public void setObject(Item item)
    {
        setText(((TextItem) item).text);
    }

}
