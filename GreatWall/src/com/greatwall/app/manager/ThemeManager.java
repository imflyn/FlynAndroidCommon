package com.greatwall.app.manager;

import android.content.Context;

import com.greatwall.sharedpreferences.AbstractSharedPreference;
import com.greatwall.sharedpreferences.SharedPreferenceFactory;

public class ThemeManager extends AppManager
{

    private static ThemeManager   instance = new ThemeManager();
    private ThemeSharedPreference mSharedPreference;

    private ThemeManager()
    {
        super();
    }

    public static ThemeManager getInstance()
    {
        return instance;
    }

    @Override
    public void onInit()
    {
        this.mSharedPreference = (ThemeSharedPreference) SharedPreferenceFactory.getSharedPreference(mContext, ThemeSharedPreference.class);
    }

    @Override
    public void onClear()
    {

    }

    @Override
    public void onClose()
    {
        this.mSharedPreference = null;
    }

    public int getCurrentThemeStyle()
    {
        return this.mSharedPreference.read(ThemeSharedPreference.DEFAULT_STOREKEY, android.R.style.Theme_Translucent_NoTitleBar);

    }

    public void updateThemeStyle(int styleCode)
    {
        this.mSharedPreference.write(ThemeSharedPreference.DEFAULT_STOREKEY, styleCode);
    }

    private static class ThemeSharedPreference extends AbstractSharedPreference
    {
        private final static String   DEFAULT_STORENAME = "ThemeStyle";
        protected final static String DEFAULT_STOREKEY  = "THEME_STYLE_ID";

        public ThemeSharedPreference(Context context)
        {
            super(context, DEFAULT_STORENAME);
        }

    }

}
