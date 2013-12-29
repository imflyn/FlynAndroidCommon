package com.flyn.ui.theme;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThemeFactory implements LayoutInflater.Factory
{
    private static String[]                          ANDROID_VIEW_FULLNAME_PREFIX = { "android.widget.", "android.webkit.", "android.view." };
    private static ThemeFactory                      factory                      = null;

    private String                                   packageName                  = null;
    private String                                   generalThemeName             = null;
    private Resources                                packageRes                   = null;
    private HashMap<String, HashMap<String, String>> stylesMap                    = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, String>                  generalThemeMap              = null;

    private boolean                                  shouldApplyTheme             = true;

    public static ThemeFactory createOrUpdateInstance(Context context, String packageName, String generalThemeName)
    {
        if (factory == null)
            factory = new ThemeFactory();
        if (context == null)
            throw new NullPointerException();
        factory.update(context, packageName, generalThemeName);
        return factory;
    }

    private void update(Context context, String packageName, String generalThemeName)
    {
        if ((this.packageName == null) && (packageName == null))
            return;
        if ((this.packageName != null) && (this.packageName.equals(packageName)))
        {
            if ((this.generalThemeName == null) && (generalThemeName == null))
                return;
            if ((this.generalThemeName != null) && (this.generalThemeName.equals(generalThemeName)))
                return;
        }
        this.packageName = packageName;
        this.generalThemeName = generalThemeName;
        this.packageRes = null;
        this.stylesMap.clear();
        this.generalThemeMap = null;
        try
        {
            loadStyles(context);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void loadStyles(Context context) throws PackageManager.NameNotFoundException, XmlPullParserException, IOException
    {
        if (this.packageName == null)
            return;
        PackageManager pm = context.getPackageManager();
        this.packageRes = pm.getResourcesForApplication(this.packageName);

        int stylesId = this.packageRes.getIdentifier("styles", "xml", this.packageName);
        if (stylesId > 0)
        {
            XmlResourceParser parser = this.packageRes.getXml(stylesId);
            int eventType = parser.getEventType();
            HashMap style = null;
            while (eventType != 1)
            {
                switch (eventType)
                {
                    case 0:
                        break;
                    case 2:
                        String tagName = parser.getName();
                        if (tagName.equals("style"))
                        {
                            style = new HashMap();
                            this.stylesMap.put(parser.getAttributeValue(null, "name"), style);
                        } else
                        {
                            if (!tagName.equals("item"))
                                break;
                            style.put(parser.getAttributeValue(null, "name"), parser.nextText());
                        }
                        break;
                    case 4:
                        break;
                    case 1:
                    case 3:
                }
                eventType = parser.next();
            }
            if (this.generalThemeName != null)
            {
                this.generalThemeMap = ((HashMap) this.stylesMap.get(this.generalThemeName));
            }
        }
    }

    public View onCreateView(String name, Context context, AttributeSet attrs)
    {
        if (this.shouldApplyTheme)
        {
            if (this.packageName == null)
                return null;
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = null;
            for (int i = 0; i < ANDROID_VIEW_FULLNAME_PREFIX.length;)
            {
                try
                {
                    view = inflater.createView(name, ANDROID_VIEW_FULLNAME_PREFIX[i], attrs);
                } catch (ClassNotFoundException localClassNotFoundException)
                {
                    i++;
                }

            }

            if (view == null)
                return null;
            applyTheme(context, view);
            applyThemeFromLayout(context, view, attrs);
            return view;
        }
        return null;
    }

    void setApplyTheme(boolean shouldApplyTheme)
    {
        this.shouldApplyTheme = shouldApplyTheme;
    }

    boolean getApplyTheme()
    {
        return this.shouldApplyTheme;
    }

    private void applyTheme(Context context, View view)
    {
        if (this.generalThemeMap == null)
            return;
        Class viewClass = view.getClass();

        while (viewClass != null)
        {
            String pName = viewClass.getPackage().getName();
            pName = pName.concat(".");
            boolean isOK = false;
            for (int i = 0; i < ANDROID_VIEW_FULLNAME_PREFIX.length; i++)
            {
                if (!pName.equals(ANDROID_VIEW_FULLNAME_PREFIX[i]))
                    continue;
                isOK = true;
                break;
            }

            if (isOK)
                break;
            viewClass = viewClass.getSuperclass();
        }
        if (viewClass != null)
        {
            String className = viewClass.getSimpleName();
            char firstChar = className.charAt(0);
            className = className.replace(firstChar, Character.toLowerCase(firstChar));
            String itemName = "android:".concat(className).concat("Style");
            String itemValue = (String) this.generalThemeMap.get(itemName);
            if (itemValue != null)
            {
                if (itemValue.startsWith("@style/"))
                {
                    String style = itemValue.substring("@style/".length());
                    applyStyle(context, view, style);
                }
            }
        }
    }

    private void applyThemeFromLayout(Context context, View view, AttributeSet paramAttributeSet)
    {
        int count = paramAttributeSet.getAttributeCount();
        for (int p = 0; p < count; p++)
        {
            String name = paramAttributeSet.getAttributeName(p);
            String value = paramAttributeSet.getAttributeValue(p);
            if (value.startsWith("?"))
            {
                if (this.generalThemeMap == null)
                    continue;
                String key = null;
                if (value.startsWith("?attr/"))
                    key = value.substring("?attr/".length());
                else
                    key = value.substring(1);
                value = (String) this.generalThemeMap.get(key);
                if (value == null)
                    continue;
            }
            if (value.startsWith("@style/"))
            {
                String style = value.substring("@style/".length());
                applyStyle(context, view, style);
            } else
            {
                if (!value.startsWith("@"))
                    continue;
                int resId = 0;
                try
                {
                    resId = Integer.valueOf(value.substring(1)).intValue();
                } catch (NumberFormatException localNumberFormatException)
                {
                }
                if (resId <= 0)
                    continue;
                if (name.equals("background"))
                {
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    int i = view.getPaddingLeft();
                    int j = view.getPaddingTop();
                    int k = view.getPaddingRight();
                    int m = view.getPaddingBottom();
                    view.setBackgroundDrawable(d);
                    view.setPadding(i, j, k, m);
                } else if (name.equals("padding"))
                {
                    int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                    if (dimen < 0)
                        continue;
                    view.setPadding(dimen, dimen, dimen, dimen);
                } else if (name.equals("paddingLeft"))
                {
                    int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                    if (dimen < 0)
                        continue;
                    view.setPadding(dimen, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                } else if (name.equals("paddingTop"))
                {
                    int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                    if (dimen < 0)
                        continue;
                    view.setPadding(view.getPaddingLeft(), dimen, view.getPaddingRight(), view.getPaddingBottom());
                } else if (name.equals("paddingRight"))
                {
                    int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                    if (dimen < 0)
                        continue;
                    view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), dimen, view.getPaddingBottom());
                } else if (name.equals("paddingBottom"))
                {
                    int dimen = getPackageDimensionPixelSize(context.getResources().getResourceEntryName(resId));
                    if (dimen < 0)
                        continue;
                    view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), dimen);
                } else if (name.equals("textColor"))
                {
                    if (!(view instanceof TextView))
                        continue;
                    ColorStateList c = getPackageColor(context.getResources().getResourceEntryName(resId));
                    if (c == null)
                        continue;
                    ((TextView) view).setTextColor(c);
                } else if (name.equals("divider"))
                {
                    if (!(view instanceof ListView))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((ListView) view).setDivider(d);
                } else if (name.equals("groupIndicator"))
                {
                    if (!(view instanceof ExpandableListView))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((ExpandableListView) view).setGroupIndicator(d);
                } else if (name.equals("childDivider"))
                {
                    if (!(view instanceof ExpandableListView))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((ExpandableListView) view).setChildDivider(d);
                } else if (name.equals("src"))
                {
                    if (!(view instanceof ImageView))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((ImageView) view).setImageDrawable(d);
                } else if (name.equals("progressDrawable"))
                {
                    if (!(view instanceof ProgressBar))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((ProgressBar) view).setProgressDrawable(d);
                } else if (name.equals("listSelector"))
                {
                    if (!(view instanceof AbsListView))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((AbsListView) view).setSelector(d);
                } else if (name.equals("cacheColorHint"))
                {
                    if (!(view instanceof AbsListView))
                        continue;
                    ColorStateList c = getPackageColor(context.getResources().getResourceEntryName(resId));
                    if (c == null)
                        continue;
                    ((AbsListView) view).setCacheColorHint(c.getDefaultColor());
                } else
                {
                    if (!name.equals("drawableRight"))
                        continue;
                    if (!(view instanceof TextView))
                        continue;
                    Drawable d = getPackageDrawable(context.getResources().getResourceEntryName(resId));
                    if (d == null)
                        continue;
                    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                }
            }
        }
    }

    private void applyStyle(Context context, View view, String styleName)
    {
        HashMap style = (HashMap) this.stylesMap.get(styleName);
        if (style != null)
        {
            String value = (String) style.get("android:background");
            if (value != null)
            {
                if (value.startsWith("@drawable/"))
                {
                    value = value.substring("@drawable/".length());
                    Drawable d = getPackageDrawable(value);
                    if (d != null)
                    {
                        int i = view.getPaddingLeft();
                        int j = view.getPaddingTop();
                        int k = view.getPaddingRight();
                        int m = view.getPaddingBottom();
                        view.setBackgroundDrawable(d);
                        view.setPadding(i, j, k, m);
                    }
                } else if (value.startsWith("#"))
                {
                    int color = Color.parseColor(value);
                    ColorDrawable cd = new ColorDrawable(color);
                    int i = view.getPaddingLeft();
                    int j = view.getPaddingTop();
                    int k = view.getPaddingRight();
                    int m = view.getPaddingBottom();
                    view.setBackgroundDrawable(cd);
                    view.setPadding(i, j, k, m);
                }
            }
            value = (String) style.get("android:padding");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(d, d, d, d);
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException1)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException2)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex)).intValue();
                        } catch (NumberFormatException localNumberFormatException3)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(d, d, d, d);
                    }
                }
            }
            value = (String) style.get("android:paddingLeft");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(d, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException4)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException5)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex)).intValue();
                        } catch (NumberFormatException localNumberFormatException6)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(d, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    }
                }
            }
            value = (String) style.get("android:paddingTop");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), d, view.getPaddingRight(), view.getPaddingBottom());
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException7)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException8)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex)).intValue();
                        } catch (NumberFormatException localNumberFormatException9)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), d, view.getPaddingRight(), view.getPaddingBottom());
                    }
                }
            }
            value = (String) style.get("android:paddingRight");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), d, view.getPaddingBottom());
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException10)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException11)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex)).intValue();
                        } catch (NumberFormatException localNumberFormatException12)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), d, view.getPaddingBottom());
                    }
                }
            }
            value = (String) style.get("android:paddingBottom");
            if (value != null)
            {
                if (value.startsWith("@dimen/"))
                {
                    value = value.substring("@dimen/".length());
                    int d = getPackageDimensionPixelSize(value);
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), d);
                    }
                } else
                {
                    int dipIndex = -1;
                    int dpIndex = -1;
                    int pxIndex = -1;
                    int d = -1;
                    if ((dipIndex = value.indexOf("dip")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dipIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException13)
                        {
                        }
                    } else if ((dpIndex = value.indexOf("dp")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, dpIndex)).intValue();
                            float scale = context.getResources().getDisplayMetrics().density;
                            d = (int) (d * scale + 0.5F);
                        } catch (NumberFormatException localNumberFormatException14)
                        {
                        }
                    } else if ((pxIndex = value.indexOf("px")) != -1)
                    {
                        try
                        {
                            d = Integer.valueOf(value.substring(0, pxIndex)).intValue();
                        } catch (NumberFormatException localNumberFormatException15)
                        {
                        }
                    }
                    if (d >= 0)
                    {
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), d);
                    }
                }
            }
            if ((view instanceof TextView))
            {
                TextView textView = (TextView) view;
                value = (String) style.get("android:textColor");
                if (value != null)
                {
                    if (value.startsWith("#"))
                    {
                        textView.setTextColor(Color.parseColor(value));
                    } else if (value.startsWith("@color/"))
                    {
                        int k = "@color/".length();
                        value = value.substring(k);
                        ColorStateList c = getPackageColor(value);
                        if (c != null)
                            textView.setTextColor(c);
                    }
                }
                value = (String) style.get("android:textSize");
                if (value != null)
                {
                    if (value.indexOf("sp") != -1)
                    {
                        float size = Float.valueOf(value.replace("sp", "")).floatValue();
                        textView.setTextSize(2, size);
                    } else if (value.indexOf("dip") != -1)
                    {
                        float size = Float.valueOf(value.replace("dip", "")).floatValue();
                        textView.setTextSize(1, size);
                    }
                }
                value = (String) style.get("android:textColorHighlight");
                if (value != null)
                {
                    if (value.startsWith("#"))
                    {
                        textView.setHighlightColor(Color.parseColor(value));
                    }
                }
                if ((textView instanceof CompoundButton))
                {
                    CompoundButton cb = (CompoundButton) textView;
                    value = (String) style.get("android:button");
                    if (value != null)
                    {
                        if (value.startsWith("@drawable/"))
                        {
                            value = value.substring("@drawable/".length());
                            Drawable d = getPackageDrawable(value);
                            if (d != null)
                                cb.setButtonDrawable(d);
                        }
                    }
                }
            } else if ((view instanceof ListView))
            {
                ListView lv = (ListView) view;
                value = (String) style.get("android:divider");
                if (value != null)
                {
                    if (value.startsWith("@drawable/"))
                    {
                        value = value.substring("@drawable/".length());
                        Drawable d = getPackageDrawable(value);
                        if (d != null)
                            lv.setDivider(d);
                    }
                }
                value = (String) style.get("android:listSelector");
                if (value != null)
                {
                    if (value.startsWith("@drawable/"))
                    {
                        value = value.substring("@drawable/".length());
                        Drawable d = getPackageDrawable(value);
                        if (d != null)
                            lv.setSelector(d);
                    }
                }
                value = (String) style.get("android:cacheColorHint");
                if (value != null)
                {
                    if (value.startsWith("@color/"))
                    {
                        value = value.substring("@color/".length());
                        ColorStateList c = getPackageColor(value);
                        if (c != null)
                            lv.setCacheColorHint(c.getDefaultColor());
                    }
                }
            }
        }
    }

    private Drawable getPackageDrawable(String drawableName)
    {
        if (this.packageName == null)
            return null;
        int drawableId = this.packageRes.getIdentifier(drawableName, "drawable", this.packageName);
        if (drawableId > 0)
        {
            return this.packageRes.getDrawable(drawableId);
        }
        return null;
    }

    private int getPackageDimensionPixelSize(String dimensionName)
    {
        if (this.packageName == null)
            return -1;
        int dimenId = this.packageRes.getIdentifier(dimensionName, "dimen", this.packageName);
        if (dimenId > 0)
        {
            return this.packageRes.getDimensionPixelSize(dimenId);
        }
        return -1;
    }

    private ColorStateList getPackageColor(String colorName)
    {
        if (this.packageName == null)
            return null;
        int colorId = this.packageRes.getIdentifier(colorName, "color", this.packageName);
        if (colorId > 0)
        {
            return this.packageRes.getColorStateList(colorId);
        }
        return null;
    }
}