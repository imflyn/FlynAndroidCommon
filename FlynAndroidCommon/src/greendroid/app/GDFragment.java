
package greendroid.app;

import greendroid.widget.ActionBar;
import greendroid.widget.ActionBar.Type;
import greendroid.widget.ActionBarHost;
import greendroid.widget.ActionBarItem;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.flynAndroidCommon.R;

/**
 * <p>
 * An GDActivity is a regular Activity that hosts an {@link ActionBar}. It is
 * extremely simple to use as you have nothing particular to do. Indeed, the
 * {@link ActionBar} is automatically added to your own layout when using the
 * {@link #getContentView()} method. You can also use one of the
 * setActionBarContentView utility methods. As a result, a basic GDActivity will
 * often be initialized using the following snippet of code:
 * </p>
 * 
 * <pre>
 * protected void onCreate(Bundle savedInstanceState)
 * {
 *     super.onCreate(savedInstanceState);
 * 
 *     setActionBarContentView(R.layout.main);
 * }
 * </pre>
 * <p>
 * An {@link ActionBar} is a widget that may contains actions items and a title.
 * You can also set the title putting an extra string with the key
 * {@link ActionBarActivity#GD_ACTION_BAR_TITLE} in your Intent:
 * </p>
 * 
 * <pre>
 * Intent intent = new Intent(this, MyGDActivity.class);
 * intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, &quot;Next screen title&quot;);
 * startActivity(intent);
 * </pre>
 * <p>
 * <em><strong>Note</strong>: An GDActivity automatically handle the type of the {@link ActionBar}
 * (taken from {@link ActionBar.Type}) depending on the value returned by the
 * {@link GDApplication#getHomeActivityClass()} method. However you can force the
 * type of the action bar in your constructor. Make the Activity declared in the AndroidManifest.xml
 * has at least a constructor with no arguments as required by the Android platform.</em>
 * </p>
 * 
 * <pre>
 * public MyGDActivity()
 * {
 *     super(ActionBar.Type.Dashboard);
 * }
 * </pre>
 * <p>
 * All Activities that inherits from an GDActivity are notified when an action
 * button is tapped in the
 * {@link #onHandleActionBarItemClick(ActionBarItem, int)} method. By default
 * this method does nothing but return false.
 * </p>
 * 
 * @see GDApplication#getHomeActivityClass()
 * @see ActionBarActivity#GD_ACTION_BAR_TITLE
 * @see GDFragment#setActionBarContentView(int)
 * @see GDFragment#setActionBarContentView(View)
 * @see GDFragment#setActionBarContentView(View, LayoutParams)
 * @author Cyril Mottier
 */
public class GDFragment extends Fragment implements ActionBarActivity
{

    private static final String LOG_TAG                 = GDFragment.class.getSimpleName();

    private boolean             mDefaultConstructorUsed = false;

    private Type                mActionBarType;
    private ActionBarHost       mActionBarHost;

    @Override
    public int createLayout()
    {
        switch (mActionBarType)
        {
            case Dashboard:
                return R.layout.gd_content_dashboard;
            case Empty:
                return R.layout.gd_content_empty;
            case Normal:
            default:
                return R.layout.gd_content_normal;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (mDefaultConstructorUsed)
        {
            // HACK cyril: This should have been done in the default
            // constructor. Unfortunately, the getApplication() method returns
            // null there. Hence, this has to be done here.
            if (getClass().equals(getGDApplication().getHomeActivityClass()))
            {
                mActionBarType = Type.Dashboard;
            }
        }

        onPreContentChanged();
        return inflater.inflate(createLayout(), null);
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position)
    {
        return false;
    }

    @Override
    public FrameLayout getContentView()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionBar getGDActionBar()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * The current {@link ActionBar.Type} of the hosted {@link ActionBar}
     * 
     * @return The current {@link ActionBar.Type} of the hosted
     *         {@link ActionBar}
     */
    public ActionBar.Type getActionBarType()
    {
        return mActionBarType;
    }

    /**
     * Verify the given layout contains everything needed by this Activity. A
     * GDActivity, for instance, manages an {@link ActionBarHost}. As a result
     * this method will return true of the current layout contains such a
     * widget.
     * 
     * @return true if the current layout fits to the current Activity widgets
     *         requirements
     */
    protected boolean verifyLayout()
    {
        return mActionBarHost != null;
    }

    @Override
    public GDApplication getGDApplication()
    {
        return (GDApplication) getActivity().getApplication();
    }

    @Override
    public ActionBarItem addActionBarItem(ActionBarItem item)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionBarItem addActionBarItem(ActionBarItem item, int itemId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionBarItem addActionBarItem(greendroid.widget.ActionBarItem.Type actionBarItemType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionBarItem addActionBarItem(greendroid.widget.ActionBarItem.Type actionBarItemType, int itemId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onPreContentChanged()
    {

    }

    @Override
    public void onPostContentChanged()
    {
    }

}
