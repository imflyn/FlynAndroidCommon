
package greendroid.app;

import greendroid.util.Config;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBar.Type;
import greendroid.widget.ActionBarHost;
import greendroid.widget.ActionBarItem;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.flynAndroidCommon.R;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

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
 * @see GDSlidingActivity#setActionBarContentView(int)
 * @see GDSlidingActivity#setActionBarContentView(View)
 * @see GDSlidingActivity#setActionBarContentView(View, LayoutParams)
 * @author Cyril Mottier
 */
public class GDSlidingActivity extends SlidingActivity implements ActionBarActivity
{

    private static final String LOG_TAG                 = GDSlidingActivity.class.getSimpleName();

    private boolean             mDefaultConstructorUsed = false;

    private Type                mActionBarType;
    private ActionBarHost       mActionBarHost;

    /**
     * <p>
     * Default constructor.
     * </p>
     * <p>
     * <em><strong>Note</strong>: This constructor should never be used manually.
     * In order to instantiate an Activity you should let the Android system do
     * it for you by calling startActivity(Intent)</em>
     * </p>
     */
    public GDSlidingActivity()
    {
        this(Type.Normal);
        mDefaultConstructorUsed = true;
    }

    /**
     * <p>
     * Create a new Activity with an {@link ActionBar} of the given type.
     * </p>
     * <p>
     * <em><strong>Note</strong>: This constructor should never be used manually.
     * In order to instantiate an Activity you should let the Android system do
     * it for you by calling startActivity(Intent)</em>
     * </p>
     * 
     * @param actionBarType
     *            The {@link ActionBar.Type} for this Activity
     */
    public GDSlidingActivity(ActionBar.Type actionBarType)
    {
        super();
        mActionBarType = actionBarType;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        ensureLayout();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        ensureLayout();
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

    /**
     * Call this method to ensure a layout has already been inflated and
     * attached to the top-level View of this Activity.
     */
    protected void ensureLayout()
    {
        if (!verifyLayout())
        {
            int resId = createLayout();
            try
            {
                setContentView(resId);
            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
        return (GDApplication) getApplication();
    }

    @Override
    public void onContentChanged()
    {
        super.onContentChanged();

        onPreContentChanged();
        onPostContentChanged();
    }

    @Override
    public void onPreContentChanged()
    {
        mActionBarHost = (ActionBarHost) findViewById(R.id.gd_action_bar_host);
        if (mActionBarHost == null)
        {
            throw new RuntimeException("Your content must have an ActionBarHost whose id attribute is R.id.gd_action_bar_host");
        }
        mActionBarHost.getActionBar().setOnActionBarListener(mActionBarListener);
    }

    @Override
    public void onPostContentChanged()
    {

        boolean titleSet = false;

        final Intent intent = getIntent();
        if (intent != null)
        {
            String title = intent.getStringExtra(ActionBarActivity.GD_ACTION_BAR_TITLE);
            if (title != null)
            {
                titleSet = true;
                setTitle(title);
            }
        }

        if (!titleSet)
        {
            // No title has been set via the Intent. Let's look in the
            // ActivityInfo
            try
            {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
                if (activityInfo.labelRes != 0)
                {
                    setTitle(activityInfo.labelRes);
                }
            } catch (NameNotFoundException e)
            {
                // Do nothing
            }
        }

        final int visibility = intent.getIntExtra(ActionBarActivity.GD_ACTION_BAR_VISIBILITY, View.VISIBLE);
        getGDActionBar().setVisibility(visibility);
    }

    @Override
    public void setTitle(CharSequence title)
    {
        getGDActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId)
    {
        setTitle(getString(titleId));
    }

    @Override
    public ActionBar getGDActionBar()
    {
        ensureLayout();
        return mActionBarHost.getActionBar();
    }

    @Override
    public ActionBarItem addActionBarItem(ActionBarItem item)
    {
        return getGDActionBar().addItem(item);
    }

    @Override
    public ActionBarItem addActionBarItem(ActionBarItem item, int itemId)
    {
        return getGDActionBar().addItem(item, itemId);
    }

    @Override
    public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType)
    {
        return getGDActionBar().addItem(actionBarItemType);
    }

    @Override
    public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType, int itemId)
    {
        return getGDActionBar().addItem(actionBarItemType, itemId);
    }

    @Override
    public FrameLayout getContentView()
    {
        ensureLayout();
        return mActionBarHost.getContentView();
    }

    /**
     * <p>
     * Set the activity content from a layout resource. The resource will be
     * inflated, adding all top-level views to the activity.
     * </p>
     * <p>
     * This method is an equivalent to setContentView(int) that automatically
     * wraps the given layout in an {@link ActionBarHost} if needed..
     * </p>
     * 
     * @param resID
     *            Resource ID to be inflated.
     * @see #setActionBarContentView(View)
     * @see #setActionBarContentView(View, LayoutParams)
     */
    public void setActionBarContentView(int resID)
    {
        final FrameLayout contentView = getContentView();
        contentView.removeAllViews();
        LayoutInflater.from(this).inflate(resID, contentView);
    }

    /**
     * <p>
     * Set the activity content to an explicit view. This view is placed
     * directly into the activity's view hierarchy. It can itself be a complex
     * view hierarchy.
     * </p>
     * <p>
     * This method is an equivalent to setContentView(View, LayoutParams) that
     * automatically wraps the given layout in an {@link ActionBarHost} if
     * needed.
     * </p>
     * 
     * @param view
     *            The desired content to display.
     * @param params
     *            Layout parameters for the view.
     * @see #setActionBarContentView(View)
     * @see #setActionBarContentView(int)
     */
    public void setActionBarContentView(View view, LayoutParams params)
    {
        final FrameLayout contentView = getContentView();
        contentView.removeAllViews();
        contentView.addView(view, params);
    }

    /**
     * <p>
     * Set the activity content to an explicit view. This view is placed
     * directly into the activity's view hierarchy. It can itself be a complex
     * view hierarchy.
     * </p>
     * <p>
     * This method is an equivalent to setContentView(View) that automatically
     * wraps the given layout in an {@link ActionBarHost} if needed.
     * </p>
     * 
     * @param view
     *            The desired content to display.
     * @see #setActionBarContentView(int)
     * @see #setActionBarContentView(View, LayoutParams)
     */
    public void setActionBarContentView(View view)
    {
        final FrameLayout contentView = getContentView();
        contentView.removeAllViews();
        contentView.addView(view);
    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position)
    {
        return false;
    }

    private OnActionBarListener mActionBarListener = new OnActionBarListener()
                                                   {
                                                       @Override
                                                    public void onActionBarItemClicked(int position)
                                                       {
                                                           if (position == OnActionBarListener.HOME_ITEM)
                                                           {

                                                               final GDApplication app = getGDApplication();
                                                               switch (mActionBarType)
                                                               {
                                                                   case Normal:
                                                                       final Class<?> klass = app.getHomeActivityClass();
                                                                       if (klass != null && !klass.equals(GDSlidingActivity.this.getClass()))
                                                                       {
                                                                           if (Config.GD_INFO_LOGS_ENABLED)
                                                                           {
                                                                               Log.i(LOG_TAG, "Going back to the home activity");
                                                                           }
                                                                           Intent homeIntent = new Intent(GDSlidingActivity.this, klass);
                                                                           homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                           startActivity(homeIntent);
                                                                       }
                                                                       break;
                                                                   case Dashboard:
                                                                       final Intent appIntent = app.getMainApplicationIntent();
                                                                       if (appIntent != null)
                                                                       {
                                                                           if (Config.GD_INFO_LOGS_ENABLED)
                                                                           {
                                                                               Log.i(LOG_TAG, "Launching the main application Intent");
                                                                           }
                                                                           startActivity(appIntent);
                                                                       }
                                                                       break;
                                                               }

                                                           } else
                                                           {
                                                               if (!onHandleActionBarItemClick(getGDActionBar().getItem(position), position))
                                                               {
                                                                   if (Config.GD_WARNING_LOGS_ENABLED)
                                                                   {
                                                                       Log.w(LOG_TAG, "Click on item at position " + position + " dropped down to the floor");
                                                                   }
                                                               }
                                                           }
                                                       }
                                                   };

}
