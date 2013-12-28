package com.flyn.ui.adapterView;

import java.util.ArrayList;
import java.util.List;

public abstract class GroupDataHolder extends DataHolder
{
    private List<DataHolder> mChildren = null;
    private boolean          mIsExpanded;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GroupDataHolder(Object data, int asyncDataCount)
    {
        super(data, asyncDataCount);
        this.mChildren = new ArrayList();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public GroupDataHolder(Object data, int asyncDataCount, List<DataHolder> children)
    {
        super(data, asyncDataCount);
        if (children == null)
            throw new NullPointerException();
        this.mChildren = new ArrayList(children);
    }

    public boolean isExpanded()
    {
        return this.mIsExpanded;
    }

    void setExpanded(boolean isExpanded)
    {
        this.mIsExpanded = isExpanded;
    }

    public void addChild(DataHolder holder)
    {
        this.mChildren.add(holder);
    }

    public void addChild(int location, DataHolder holder)
    {
        this.mChildren.add(location, holder);
    }

    public void addChildren(List<DataHolder> holders)
    {
        this.mChildren.addAll(holders);
    }

    public void addChildren(int location, List<DataHolder> holders)
    {
        this.mChildren.addAll(location, holders);
    }

    public void removeChild(int location)
    {
        this.mChildren.remove(location);
    }

    public void removeChild(DataHolder holder)
    {
        this.mChildren.remove(holder);
    }

    public DataHolder queryChild(int location)
    {
        return (DataHolder) this.mChildren.get(location);
    }

    public int queryChild(DataHolder holder)
    {
        return this.mChildren.indexOf(holder);
    }

    public void clearChildren()
    {
        this.mChildren.clear();
    }

    public int getChildrenCount()
    {
        return this.mChildren.size();
    }
}