package com.flyn.util.xmlpull;

import android.util.Xml;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class SimpleDomManager
{
    public static String serializeDom(List<Element> dom, boolean containsXmlHead)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String encoding = "utf-8";
        try
        {
            serializeDom(dom, containsXmlHead, output, encoding);
            return new String(output.toByteArray(), encoding);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    public static void serializeDom(List<Element> dom, boolean containsXmlHead, OutputStream output, String encoding) throws IOException
    {
        if ((dom == null) || (output == null) || (encoding == null))
            throw new NullPointerException();
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(output, encoding);
        if (containsXmlHead)
            serializer.startDocument(encoding, Boolean.valueOf(true));
        serializeDomImpl(serializer, dom);
        serializer.endDocument();
    }

    private static void serializeDomImpl(XmlSerializer serializer, List<Element> dom) throws IOException
    {
        for (Element element : dom)
        {
            String tag = element.getTag();
            serializer.startTag(null, tag);
            List<String[]> attrs = element.getAttributes();
            for (String[] attr : attrs)
            {
                serializer.attribute(null, attr[0], attr[1]);
            }
            if (element.isLeaf())
            {
                serializer.text(element.getText());
            } else
            {
                serializeDomImpl(serializer, element.getChildren());
            }
            serializer.endTag(null, tag);
        }
    }
}