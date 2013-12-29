
package greendroid.widget.itemview;

import greendroid.widget.item.Item;
import greendroid.widget.item.TextItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * View representation of the {@link TextItem}.
 * 
 * @author Cyril Mottier
 */
public class TextItemView extends TextView implements ItemView
{

    public TextItemView(Context context)
    {
        this(context, null);
    }

    public TextItemView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TextItemView(Context context, AttributeSet attrs, int defStyle)
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
        setText(((TextItem) object).text);
    }

}
