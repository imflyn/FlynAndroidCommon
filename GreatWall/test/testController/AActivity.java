package testController;

import android.os.Bundle;

import com.greatwall.ui.BaseActivity;
import com.greatwall.ui.interfaces.BaseUIEvent;

public class AActivity extends BaseActivity implements AControllerListener
{

    @Override
    protected AActivityController getController()
    {
        return (AActivityController) controller;
    }

    @Override
    protected int layoutId()
    {
        return 0;
    }

    @Override
    protected void findViews()
    {

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

}
