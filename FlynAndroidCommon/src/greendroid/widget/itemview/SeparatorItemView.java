package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.SeparatorItem;
import greendroid.widget.item.TextItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * View representation of the {@link SeparatorItem}.
 * 
 * @author Cyril Mottier
 */
public class SeparatorItemView extends TextView implements ItemView
{

    public SeparatorItemView(Context context)
    {
        this(context, null);
    }

    public SeparatorItemView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public SeparatorItemView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void prepareItemView()
    {
    }

    @Override
    public void setObject(Item object)
    {
        final TextItem item = (TextItem) object;
        setText(item.text);
    }

}
