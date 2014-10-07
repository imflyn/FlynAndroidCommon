package com.flyn.util.xmlpull;

import java.util.ArrayList;
import java.util.List;

public class Element
{
    private String tag = "";
    private String text = "";
    private List<String[]> attributes = new ArrayList<String[]>();
    private List<Element> children = new ArrayList<Element>();

    public Element(String tag)
    {
        if (tag == null)
        {
            throw new NullPointerException();
        }
        this.tag = tag;
    }

    public String getTag()
    {
        return this.tag;
    }

    public Element setTag(String tag)
    {
        if (tag == null)
        {
            throw new NullPointerException();
        }
        this.tag = tag;
        return this;
    }

    public String getText()
    {
        return this.text;
    }

    public Element setText(String text)
    {
        if (text == null)
        {
            throw new NullPointerException();
        }
        this.text = text;
        return this;
    }

    public List<String[]> getAttributes()
    {
        return this.attributes;
    }

    public List<Element> getChildren()
    {
        return this.children;
    }

    public boolean isLeaf()
    {
        return this.children.size() == 0;
    }
}
