package greendroid.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

/**
 * <p>
 * The base implementation of an Adapter to use with a {@link PagedView}.
 * Clients may create classes that extends from this base implementation. The
 * work consists on overriding the {@link PagedAdapter#getCount()} and
 * {@link PagedAdapter#getView(int, View, ViewGroup)} methods.
 * </p>
 * 
 * @author Cyril Mottier
 */
public abstract class PagedAdapter implements Adapter
{

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public abstract int getCount();

    @Override
    public abstract Object getItem(int position);

    @Override
    public abstract long getItemId(int position);

    @Override
    public boolean hasStableIds()
    {
        throw new UnsupportedOperationException("hasStableIds(int) is not supported in the context of a SwipeAdapter");
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public final int getItemViewType(int position)
    {
        throw new UnsupportedOperationException("getItemViewType(int) is not supported in the context of a SwipeAdapter");
    }

    @Override
    public final int getViewTypeCount()
    {
        throw new UnsupportedOperationException("getViewTypeCount() is not supported in the context of a SwipeAdapter");
    }

    @Override
    public final boolean isEmpty()
    {
        throw new UnsupportedOperationException("isEmpty() is not supported in the context of a SwipeAdapter");
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged()
    {
        mDataSetObservable.notifyChanged();
    }

}
