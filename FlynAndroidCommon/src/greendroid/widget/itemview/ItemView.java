package greendroid.widget.itemview;

import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;

/**
 * <p>
 * An ItemView defines several methods necessary to the {@link ItemAdapter} in
 * order to process {@link Item}s.
 * </p>
 * <p>
 * When developing your own ItemViews, make sure they all implement this
 * interface.
 * </p>
 * 
 * @author Cyril Mottier
 */
public interface ItemView
{

    /**
     * Called by the {@link ItemAdapter} the first time the ItemView is created.
     * This is usually a good time to keep references on sub-Views.
     */
    void prepareItemView();

    /**
     * Called by the {@link ItemAdapter} whenever an ItemView is displayed on
     * screen. This may occur at the first display time or when the ItemView is
     * reused by the ListView.
     * 
     * @param item
     *            The {@link Item} containing date used to populate this
     *            ItemView
     */
    void setObject(Item item);

}
