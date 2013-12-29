package com.flyn.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

public class TabLayout extends ViewGroup
{
    public static final String      HEAD_POSITION_TOP      = "top";
    public static final String      HEAD_POSITION_BOTTOM   = "bottom";
    public static final String      HEAD_POSITION_LEFT     = "left";
    public static final String      HEAD_POSITION_RIGHT    = "right";
    protected Class<?>              tabClass               = Button.class;
    protected String                headPosition           = "top";
    protected int                   selectedTabIndex       = -1;
    protected int                   tempSelectedTabIndex   = -1;

    protected List<View>            tabs                   = new ArrayList();
    protected Map<Integer, View>    items                  = new HashMap();

    protected OnTabChangedListener  mOnTabChangedListener  = null;
    protected OnAddFragmentListener mOnAddFragmentListener = null;

    public TabLayout(Context context)
    {
        this(context, null, 0);
    }

    public TabLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (attrs != null)
        {
            String tabClassName = attrs.getAttributeValue(null, "tabClass");
            String headPosition = attrs.getAttributeValue(null, "headPosition");
            String selectedTab = attrs.getAttributeValue(null, "selectedTab");
            try
            {
                if (tabClassName != null)
                    setTabClass(Class.forName(tabClassName));
            } catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            if (headPosition != null)
                setHeadPosition(headPosition);
            if (selectedTab != null)
                setSelectedTab(Integer.parseInt(selectedTab));
        }
    }

    public void setTabClass(Class<?> tabClass)
    {
        if (tabClass == null)
            throw new NullPointerException();
        this.tabClass = tabClass;
    }

    public void setHeadPosition(String headPosition)
    {
        if (headPosition == null)
            throw new NullPointerException();
        if ((!headPosition.equals("top")) && (!headPosition.equals("bottom")) && (!headPosition.equals("left")) && (!headPosition.equals("right")))
            throw new IllegalArgumentException("headPosition is invalid!");
        this.headPosition = headPosition;
    }

    protected ViewGroup refreshLayout()
    {
        if (getChildCount() != 2)
            throw new IllegalStateException("TabLayout can only contains two children(head and content)!");
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        if (!(child1 instanceof ViewGroup))
            throw new IllegalStateException("TabLayout’s all children should be a ViewGroup!");
        if (!(child2 instanceof ViewGroup))
            throw new IllegalStateException("TabLayout’s all children should be a ViewGroup!");
        ViewGroup head = null;
        ViewGroup content = null;
        if ((this.headPosition.equals("top")) || (this.headPosition.equals("left")))
        {
            head = (ViewGroup) child1;
            content = (ViewGroup) child2;
        } else if ((this.headPosition.equals("bottom")) || (this.headPosition.equals("right")))
        {
            head = (ViewGroup) child2;
            content = (ViewGroup) child1;
        }
        this.tabs.clear();
        refreshTabs(head);
        int tabSize = this.tabs.size();
        int contentSize = content.getChildCount();
        if (this.mOnAddFragmentListener == null)
        {
            while (tabSize > contentSize)
            {
                ((View) this.tabs.remove(tabSize - 1)).setOnClickListener(null);
                tabSize = this.tabs.size();
            }
            this.items.clear();
            for (int i = 0; i < contentSize; i++)
            {
                this.items.put(Integer.valueOf(i), content.getChildAt(i));
            }
        } else
        {
            Iterator entrys = this.items.values().iterator();
            while (entrys.hasNext())
            {
                View entry = (View) entrys.next();
                if (entry != null)
                    entrys.remove();
            }
            for (int i = 0; i < contentSize; i++)
            {
                View child = content.getChildAt(i);
                if (!(child instanceof ViewGroup))
                    throw new IllegalStateException("in fragment mode,content children can only be created by Fragment");
                ViewGroup childGroup = (ViewGroup) child;
                if (childGroup.getChildCount() != 1)
                    throw new IllegalStateException("in fragment mode,content children can only be created by Fragment");
                Object indexObj = childGroup.getChildAt(0).getTag();
                if (!(indexObj instanceof Integer))
                    throw new IllegalStateException(
                            "in fragment mode,you should use setTag(tag) to set tab index in Fragment’s onCreateView(inflater,container,savedInstanceState) when onAddFragment is called back");
                int index = ((Integer) indexObj).intValue();
                if ((index < 0) || (index >= tabSize))
                    throw new IllegalStateException("the tab index setting by setTag(tag) is out of tab size");
                if (this.items.get(Integer.valueOf(index)) != null)
                    throw new IllegalStateException("the tab index setting by setTag(tag) already exists");
                this.items.put(Integer.valueOf(index), child);
            }
        }
        return content;
    }

    protected void refreshTabs(View view)
    {
        if (view.getClass().equals(this.tabClass))
        {
            this.tabs.add(view);
            final int index = this.tabs.size() - 1;
            view.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    TabLayout.this.setSelectedTab(index);
                }
            });
        } else if ((view instanceof ViewGroup))
        {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++)
            {
                refreshTabs(vg.getChildAt(i));
            }
        }
    }

    protected void changeTabWhenLayout(final int index, boolean isIndexChanged)
    {
        for (int i = 0; i < this.tabs.size(); i++)
        {
            View tabView = (View) this.tabs.get(i);
            View contentView = (View) this.items.get(Integer.valueOf(i));
            if (index == i)
            {
                if ((tabView instanceof CompoundButton))
                    ((CompoundButton) tabView).setChecked(true);
                contentView.setVisibility(0);
            } else
            {
                if ((tabView instanceof CompoundButton))
                    ((CompoundButton) tabView).setChecked(false);
                if (contentView != null)
                    contentView.setVisibility(8);
            }
        }
        if (isIndexChanged)
        {
            this.selectedTabIndex = index;
            if (this.mOnTabChangedListener != null)
            {
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        TabLayout.this.mOnTabChangedListener.onTabChanged((View) TabLayout.this.tabs.get(index), (View) TabLayout.this.items.get(Integer.valueOf(index)), index);
                    }
                });
            }
        }
    }

    public void setSelectedTab(int index)
    {
        if (index < 0)
            throw new IllegalArgumentException("index should equals or great than zero.");
        if (index == this.selectedTabIndex)
            return;
        this.tempSelectedTabIndex = index;
        requestLayout();
    }

    public int getSelectedTabIndex()
    {
        return this.selectedTabIndex;
    }

    public List<View> getTabs()
    {
        List returnCopy = new ArrayList(this.tabs.size());
        for (View view : this.tabs)
        {
            returnCopy.add(view);
        }
        return returnCopy;
    }

    public int getTabCount()
    {
        return this.tabs.size();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if ((this.headPosition.equals("top")) || (this.headPosition.equals("bottom")))
        {
            int firstHeight = 0;
            if (child1.getVisibility() != 8)
            {
                int child1Width = child1.getMeasuredWidth();
                int child1Height = child1.getMeasuredHeight();
                child1.layout(paddingLeft, paddingTop, paddingLeft + child1Width, paddingTop + child1Height);
                firstHeight = child1Height;
            }
            if (child2.getVisibility() != 8)
            {
                child2.layout(paddingLeft, paddingTop + firstHeight, paddingLeft + child2.getMeasuredWidth(), paddingTop + firstHeight + child2.getMeasuredHeight());
            }
        } else if ((this.headPosition.equals("left")) || (this.headPosition.equals("right")))
        {
            int firstWidth = 0;
            if (child1.getVisibility() != 8)
            {
                int child1Width = child1.getMeasuredWidth();
                int child1Height = child1.getMeasuredHeight();
                child1.layout(paddingLeft, paddingTop, paddingLeft + child1Width, paddingTop + child1Height);
                firstWidth = child1Width;
            }
            if (child2.getVisibility() != 8)
            {
                child2.layout(paddingLeft + firstWidth, paddingTop, paddingLeft + firstWidth + child2.getMeasuredWidth(), paddingTop + child2.getMeasuredHeight());
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if ((widthMode != 1073741824) || (heightMode != 1073741824))
            throw new IllegalStateException("TabLayout only can run at EXACTLY mode!");
        int wrapWidthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int wrapHeightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = wrapWidthSize - getPaddingLeft() - getPaddingRight();
        int heightSize = wrapHeightSize - getPaddingTop() - getPaddingBottom();

        ViewGroup content = refreshLayout();

        int tabSize = this.tabs.size();
        if (this.tempSelectedTabIndex == -1)
        {
            if (tabSize > 0)
            {
                View view = (View) this.items.get(Integer.valueOf(0));
                if (view == null)
                {
                    if (!this.items.containsKey(Integer.valueOf(0)))
                    {
                        this.mOnAddFragmentListener.onAddFragment(0, content);
                        this.items.put(Integer.valueOf(0), null);
                    }
                } else
                {
                    this.tempSelectedTabIndex = 0;
                    changeTabWhenLayout(this.tempSelectedTabIndex, true);
                }
            }
        } else
        {
            if (this.tempSelectedTabIndex >= tabSize)
            {
                int tempSelectedTabIndexCopy = this.tempSelectedTabIndex;
                this.tempSelectedTabIndex = this.selectedTabIndex;
                throw new IllegalStateException("tab index is out of range:" + tempSelectedTabIndexCopy + "!");
            }

            View view = (View) this.items.get(Integer.valueOf(this.tempSelectedTabIndex));
            if (view == null)
            {
                if (!this.items.containsKey(Integer.valueOf(this.tempSelectedTabIndex)))
                {
                    this.mOnAddFragmentListener.onAddFragment(this.tempSelectedTabIndex, content);
                    this.items.put(Integer.valueOf(this.tempSelectedTabIndex), null);
                }
            } else
            {
                changeTabWhenLayout(this.tempSelectedTabIndex, this.tempSelectedTabIndex != this.selectedTabIndex);
            }

        }

        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        if (this.headPosition.equals("top"))
        {
            int remainHeight = heightSize;
            if (child1.getVisibility() != 8)
            {
                ViewGroup.LayoutParams lp = child1.getLayoutParams();
                if ((lp.height == -1) || (lp.height == -2))
                {
                    child1.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824), View.MeasureSpec.makeMeasureSpec(heightSize, -2147483648));
                } else
                {
                    child1.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824), View.MeasureSpec.makeMeasureSpec(lp.height, 1073741824));
                }
                remainHeight = heightSize - child1.getMeasuredHeight();
                if (remainHeight < 0)
                    remainHeight = 0;
            }
            if (child2.getVisibility() != 8)
            {
                child2.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824), View.MeasureSpec.makeMeasureSpec(remainHeight, 1073741824));
            }
        } else if (this.headPosition.equals("bottom"))
        {
            int remainHeight = heightSize;
            if (child2.getVisibility() != 8)
            {
                ViewGroup.LayoutParams lp = child2.getLayoutParams();
                if ((lp.height == -1) || (lp.height == -2))
                {
                    child2.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824), View.MeasureSpec.makeMeasureSpec(heightSize, -2147483648));
                } else
                {
                    child2.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824), View.MeasureSpec.makeMeasureSpec(lp.height, 1073741824));
                }
                remainHeight = heightSize - child2.getMeasuredHeight();
                if (remainHeight < 0)
                    remainHeight = 0;
            }
            if (child1.getVisibility() != 8)
            {
                child1.measure(View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824), View.MeasureSpec.makeMeasureSpec(remainHeight, 1073741824));
            }
        } else if (this.headPosition.equals("left"))
        {
            int remainWidth = widthSize;
            if (child1.getVisibility() != 8)
            {
                ViewGroup.LayoutParams lp = child1.getLayoutParams();
                if ((lp.width == -1) || (lp.width == -2))
                {
                    child1.measure(View.MeasureSpec.makeMeasureSpec(widthSize, -2147483648), View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
                } else
                {
                    child1.measure(View.MeasureSpec.makeMeasureSpec(lp.width, 1073741824), View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
                }
                remainWidth = widthSize - child1.getMeasuredWidth();
                if (remainWidth < 0)
                    remainWidth = 0;
            }
            if (child2.getVisibility() != 8)
            {
                child2.measure(View.MeasureSpec.makeMeasureSpec(remainWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
            }
        } else if (this.headPosition.equals("right"))
        {
            int remainWidth = widthSize;
            if (child2.getVisibility() != 8)
            {
                ViewGroup.LayoutParams lp = child2.getLayoutParams();
                if ((lp.width == -1) || (lp.width == -2))
                {
                    child2.measure(View.MeasureSpec.makeMeasureSpec(widthSize, -2147483648), View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
                } else
                {
                    child2.measure(View.MeasureSpec.makeMeasureSpec(lp.width, 1073741824), View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
                }
                remainWidth = widthSize - child2.getMeasuredWidth();
                if (remainWidth < 0)
                    remainWidth = 0;
            }
            if (child1.getVisibility() != 8)
            {
                child1.measure(View.MeasureSpec.makeMeasureSpec(remainWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824));
            }
        }
        setMeasuredDimension(wrapWidthSize, wrapHeightSize);
    }

    public void setOnTabChangedListener(OnTabChangedListener mOnTabChangedListener)
    {
        this.mOnTabChangedListener = mOnTabChangedListener;
    }

    public void setUseFragmentMode(OnAddFragmentListener mOnAddFragmentListener)
    {
        this.mOnAddFragmentListener = mOnAddFragmentListener;
    }

    public static abstract interface OnAddFragmentListener
    {
        public abstract void onAddFragment(int paramInt, ViewGroup paramViewGroup);
    }

    public static abstract interface OnTabChangedListener
    {
        public abstract void onTabChanged(View paramView1, View paramView2, int paramInt);
    }
}