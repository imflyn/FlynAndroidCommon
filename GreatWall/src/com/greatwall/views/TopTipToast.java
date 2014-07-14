package com.greatwall.views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.greatwall.R;
import com.greatwall.app.Application;
import com.greatwall.util.DensityUtils;

/**
 * 顶部toast
 * 
 * @author V
 *
 */
public class TopTipToast extends Toast
{

    public TopTipToast(Context context)
    {
        super(context);

    }

    public static Toast makeToast(CharSequence text, int top)
    {
        Context context = Application.getInstance();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.toast_toptip, null);
        view.setMinimumWidth(dm.widthPixels);// 设置控件最小宽度为手机屏幕宽度
        TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
        tv_content.setText("  " + text);

        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setGravity(Gravity.TOP, 0, top);

        return toast;
    }

    public static Toast makeToast(int resId, int top)
    {
        return makeToast(Application.getInstance().getString(resId), top);
    }

    public static Toast makeToast(CharSequence text)
    {
        return makeToast(text, DensityUtils.dip2px(Application.getInstance(), 48));
    }

    public static Toast makeToast(int resId)
    {
        return makeToast(Application.getInstance().getString(resId));
    }

}
