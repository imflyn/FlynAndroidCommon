package com.greatwall.app;

import java.util.Stack;

import android.app.Activity;
import android.content.Context;

import com.greatwall.ui.EmptyActivity;

public class ActivityManager implements AppManager
{

    private static ActivityManager instance;
    private Stack<Activity>        mActivityStack;
    private  Application      mContext;

    private ActivityManager()
    {
        
    }

    /** 单一实例 */
    public static ActivityManager getInstance()
    {
        if (instance == null)
        {
            synchronized (ActivityManager.class)
            {
                if (instance == null)
                {
                    instance = new ActivityManager();
                }
            }
        }
        return instance;
    }

    public Stack<Activity> getActivityStack()
    {
        if (mActivityStack == null)
        {
            mActivityStack = new Stack<Activity>();
        }
        return mActivityStack;
    }

    /** 添加Activity到堆栈 */
    public void addActivity(Activity activity)
    {

        getActivityStack().add(activity);
    }

    /** 获取当前Activity（堆栈中最后一个压入的） */
    public Activity currentActivity()
    {
        Activity activity = getActivityStack().lastElement();
        return activity;
    }

    /** 结束当前Activity（堆栈中最后一个压入的） */
    public void finishActivity()
    {
        Activity activity = getActivityStack().lastElement();
        finishActivity(activity);
    }

    /** 结束指定的Activity */
    public void finishActivity(Activity activity)
    {
        if (activity != null)
        {
            activity.finish();
            removeActivity(activity);
        }
    }

    /** 移除指定的Activity */
    public void removeActivity(Activity activity)
    {
        mActivityStack.remove(activity);
        activity = null;
    }

    /** 结束指定类名的Activity */
    public void finishActivity(Class<?> cls)
    {
        for (Activity activity : getActivityStack())
        {
            if (activity.getClass().equals(cls))
            {
                finishActivity(activity);
            }
        }
    }

    public Activity getActivity(Class<?> cls)
    {
        for (Activity activity : getActivityStack())
        {
            if (activity.getClass().equals(cls))
            {
                return activity;
            }
        }
        return new EmptyActivity();
    }

    /**
     * 获取所有运行中的activity
     * 
     * @return
     */
    public Stack<Activity> getAllActivity()
    {
        return getActivityStack();
    }

    /** 结束所有Activity */
    public void finishAllActivity()
    {
        Stack<Activity> stack = getActivityStack();

        for (int i = 0, size = stack.size(); i < size; i++)
        {
            if (null != stack.get(i))
            {
                stack.get(i).finish();
            }
        }
        stack.clear();
    }


    @Override
    public void onClose()
    {
        finishAllActivity();
        android.app.ActivityManager activityMgr = (android.app.ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        activityMgr.restartPackage(mContext.getPackageName());
    }

    @Override
    public void onInit()
    {
        mContext = Application.getInstance();
        
    }

}
