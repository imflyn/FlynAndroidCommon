
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
 * Progress indicator that displays a centered text with a circular and
 * indeterminate ProgressBar when something is in progress.
 * 
 * @author Cyril Mottier
 */
public class ProgressItem extends TextItem
{

    private static final boolean DEFAULT_IS_IN_PROGRESS = false;

    /**
     * The state of this item. When set to true, a circular progress bar
     * indicates something is going on/being computed.
     */
    public boolean               isInProgress;

    /**
     * @hide
     */
    public ProgressItem()
    {
        this(null);
    }

    /**
     * Create a ProgressItem with the given text. By default, the circular
     * progress bar is not visible ... which indicates nothing is currently in
     * progress.
     * 
     * @param text
     *            The text for this item
     */
    public ProgressItem(String text)
    {
        this(text, DEFAULT_IS_IN_PROGRESS);
    }

    /**
     * Create a ProgressItem with the given text and state.
     * 
     * @param text
     *            The text for this item
     * @param isInProgress
     *            The state for this item
     */
    public ProgressItem(String text, boolean isInProgress)
    {
        super(text);
        this.isInProgress = isInProgress;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent)
    {
        return createCellFromXml(context, R.layout.gd_progress_item_view, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException
    {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.ProgressItem);
        isInProgress = a.getBoolean(R.styleable.ProgressItem_isInProgress, DEFAULT_IS_IN_PROGRESS);
        a.recycle();
    }

}