package com.greatwall.ui;

import com.greatwall.R;

import android.os.Bundle;
import android.view.View;

public class EmptyActivity extends BaseActivity
{

    @Override
    protected int layoutId()
    {
        return 0;
    }

    @Override
    protected void initView(Bundle savedInstanceState)
    {
        View view=new View(this);
        view.setBackgroundColor(getResources().getColor(R.color.black));
        setContentView(view);
    }

    @Override
    protected void setListener()
    {

    }

}
