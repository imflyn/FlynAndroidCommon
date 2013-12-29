package com.flyn.ui.theme;

import android.app.Activity;
import android.os.Bundle;

public class ThemeActivity extends Activity
{
    protected void onCreateImpl(Bundle savedInstanceState)
    {
        getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeManager.CUR_PACKAGENAME, ThemeManager.CUR_GENERALTHEME_NAME));
    }
}