package com.greatwall.views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.greatwall.R;
import com.greatwall.util.DensityUtils;

public class TopTipToast extends Toast
{

    public TopTipToast(Context context)
    {
        super(context);

    }

    public static Toast makeToast(Context context, CharSequence text, int duration)
    {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.toast_toptip, null);
        view.setMinimumWidth(dm.widthPixels);// 设置控件最小宽度为手机屏幕宽度
        TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
        tv_content.setText("  " + text);

        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(duration);
        toast.setGravity(Gravity.TOP, 0, DensityUtils.dip2px(context, 45));

        return toast;
    }

    public static Toast makeToast(Context context, int resId, int duration)
    {
        return makeToast(context, context.getString(resId), duration);
    }

}
