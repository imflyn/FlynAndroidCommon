package testController;

import android.support.v4.app.Fragment;

import com.greatwall.ui.interfaces.BaseFragmentController;

public class AFragmentController extends BaseFragmentController<AFragment>
{

    public AFragmentController(Fragment fragment)
    {
        super(fragment);

        mContext.onRes();
        mContext.onUI();
    }

}
