package testController;

import android.app.Activity;

import com.greatwall.ui.interfaces.BaseActivityController;

public class AActivityController extends BaseActivityController<AActivity>
{

    public AActivityController(Activity context)
    {
        super(context);

        mContext.onUI();
        mContext.onEvent(null);
        mContext.runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub

            }
        });
    }

}
