package testController;

import android.os.Bundle;

import com.greatwall.ui.BaseFragment;
import com.greatwall.ui.interfaces.BaseActivityController;
import com.greatwall.ui.interfaces.BaseUIEvent;

public class AFragment extends BaseFragment implements AControllerListener, BControllerListener
{

    @Override
    protected BaseActivityController<?> getController()
    {
        return null;
    }

    @Override
    protected int getLayoutId()
    {
        return 0;
    }

    @Override
    protected void initView(Bundle savedInstanceState)
    {

    }

    @Override
    protected void setListener()
    {

    }

    @Override
    public void onEvent(BaseUIEvent event)
    {

    }

    @Override
    public void onUI()
    {

    }

    @Override
    public void onRes()
    {
        // TODO Auto-generated method stub

    }

}
