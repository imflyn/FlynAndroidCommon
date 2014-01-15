package com.flyn.net.volcano;

import java.io.Closeable;
import java.io.IOException;

public class Utils
{
    public static void quickClose(Closeable stream)
    {
        if (null != stream)
            try
            {
                stream.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                stream = null;
            }

    }
}
