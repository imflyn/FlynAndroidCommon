
package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.LongTextItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * View representation of the {@link LongTextItem}.
 * 
 * @author Cyril Mottier
 */
public class LongTextItemView extends TextView implements ItemView
{

    public LongTextItemView(Context context)
    {
        this(context, null);
    }

    public LongTextItemView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public LongTextItemView(Context context, AttributeSet attrs, int defStyle)
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
        setText(((LongTextItem) item).text);
    }

}
