package com.flyn.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.lang.reflect.Field;

public abstract class DialogManager
{

    public static AlertDialog setNotAutoDismiss(final AlertDialog dialog)
    {
        try
        {
            Field field = AlertDialog.class.getDeclaredField("mAlert");
            field.setAccessible(true);

            Object obj = field.get(dialog);
            field = obj.getClass().getDeclaredField("mHandler");
            field.setAccessible(true);

            field.set(obj, new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    switch (msg.what)
                    {
                        case -1:
                            ((DialogInterface.OnClickListener) msg.obj).onClick(dialog, msg.what);
                    }
                }
            });
            return dialog;
        } catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static AlertDialog.Builder createAlertDialogBuilder(Context context, String title, String[] buttons, DialogInterface.OnClickListener onClickListener, boolean cancelable)
    {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        if (title != null)
        {
            ab.setTitle(title);
        }
        if (buttons != null)
        {
            if (buttons.length >= 1)
            {
                ab.setPositiveButton(buttons[0], onClickListener);
            }
            if (buttons.length >= 2)
            {
                ab.setNeutralButton(buttons[1], onClickListener);
            }
            if (buttons.length >= 3)
            {
                ab.setNegativeButton(buttons[2], onClickListener);
            }
        }
        ab.setCancelable(cancelable);
        return ab;
    }

    public static AlertDialog.Builder createAlertDialogBuilder(Context context, int titleId, int[] buttonIds, DialogInterface.OnClickListener onClickListener, boolean cancelable)
    {
        String title = null;
        if (titleId != -1)
        {
            title = context.getString(titleId);
        }
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return createAlertDialogBuilder(context, title, buttons, onClickListener, cancelable);
    }

    public static AlertDialog showAlertDialog(Context context, String title, String msg, String[] buttons, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        AlertDialog.Builder ab = createAlertDialogBuilder(context, title, buttons, onClickListener, cancelable);
        if (msg != null)
        {
            ab.setMessage(msg);
        }
        AlertDialog dialog = ab.show();
        if (isNotAutoDismiss)
        {
            dialog = setNotAutoDismiss(dialog);
        }
        return dialog;
    }

    public static AlertDialog showAlertDialog(Context context, int titleId, int msgId, int[] buttonIds, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
        {
            title = context.getString(titleId);
        }
        String msg = null;
        if (msgId != -1)
        {
            msg = context.getString(msgId);
        }
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showAlertDialog(context, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static AlertDialog showAlertDialog(Context context, String title, View view, String[] buttons, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        AlertDialog.Builder ab = createAlertDialogBuilder(context, title, buttons, onClickListener, cancelable);
        if (view != null)
        {
            ab.setView(view);
        }
        AlertDialog dialog = ab.show();
        if (isNotAutoDismiss)
        {
            dialog = setNotAutoDismiss(dialog);
        }
        return dialog;
    }

    public static AlertDialog showAlertDialog(Context context, int titleId, View view, int[] buttonIds, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
        {
            title = context.getString(titleId);
        }
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showAlertDialog(context, title, view, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String msg, String[] buttons, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        return showProgressDialog(context, -1, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ProgressDialog showProgressDialog(Context context, int titleId, int msgId, int[] buttonIds, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
        {
            title = context.getString(titleId);
        }
        String msg = null;
        if (msgId != -1)
        {
            msg = context.getString(msgId);
        }
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showProgressDialog(context, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ProgressDialog showProgressDialog(Context context, int theme, String title, String msg, String[] buttons, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        ProgressDialog pd = null;
        if (theme == -1)
        {
            pd = new ProgressDialog(context);
        } else
        {
            pd = new ProgressDialog(context, theme);
        }
        if (title != null)
        {
            pd.setTitle(title);
        }
        if (msg != null)
        {
            pd.setMessage(msg);
        }
        if (buttons != null)
        {
            if (buttons.length >= 1)
            {
                pd.setButton(-1, buttons[0], onClickListener);
            }
            if (buttons.length >= 2)
            {
                pd.setButton(-3, buttons[1], onClickListener);
            }
            if (buttons.length >= 3)
            {
                pd.setButton(-2, buttons[2], onClickListener);
            }
        }
        pd.setCancelable(cancelable);
        pd.show();
        if (isNotAutoDismiss)
        {
            pd = (ProgressDialog) setNotAutoDismiss(pd);
        }
        return pd;
    }

    public static ProgressDialog showProgressDialog(Context context, int theme, int titleId, int msgId, int[] buttonIds, DialogInterface.OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
        {
            title = context.getString(titleId);
        }
        String msg = null;
        if (msgId != -1)
        {
            msg = context.getString(msgId);
        }
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showProgressDialog(context, theme, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

}