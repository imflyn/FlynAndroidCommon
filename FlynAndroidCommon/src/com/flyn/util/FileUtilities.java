package com.flyn.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public abstract class FileUtilities
{
    public static void readFromFile(File targetFile, OutputStream output, int cacheBytesLength) throws IOException
    {
        if ((targetFile == null) || (output == null))
            throw new NullPointerException();
        if (cacheBytesLength <= 0)
            throw new IllegalArgumentException("The parameter of cacheBytesLength should be great than zero.");
        InputStream input = null;
        try
        {
            input = new FileInputStream(targetFile);
            readAndWrite(input, output, cacheBytesLength);
        } finally
        {
            if (input != null)
                input.close();
        }
    }

    public static void writeToFile(InputStream input, File targetFile, int cacheBytesLength) throws IOException
    {
        if ((input == null) || (targetFile == null))
            throw new NullPointerException();
        if (cacheBytesLength <= 0)
            throw new IllegalArgumentException("The parameter of cacheBytesLength should be great than zero.");
        OutputStream output = null;
        try
        {
            File parentFile = targetFile.getParentFile();
            if ((!parentFile.exists()) && (!parentFile.mkdirs()))
                throw new IOException("could not create the path:" + parentFile.getPath());
            output = new FileOutputStream(targetFile);
            readAndWrite(input, output, cacheBytesLength);
        } finally
        {
            if (output != null)
                output.close();
        }
    }

    public static void readAndWrite(InputStream input, OutputStream output, int cacheBytesLength) throws IOException
    {
        if ((input == null) || (output == null))
            throw new NullPointerException();
        if (cacheBytesLength <= 0)
            throw new IllegalArgumentException("The parameter of cacheBytesLength should be great than zero.");
        BufferedInputStream buffInput = new BufferedInputStream(input);
        BufferedOutputStream buffOutput = new BufferedOutputStream(output);
        byte[] b = new byte[cacheBytesLength];
        int len;
        while ((len = buffInput.read(b)) > 0)
        {
            buffOutput.write(b, 0, len);
        }
        buffOutput.flush();
    }

    public static void delDirectory(File f) throws IOException
    {
        if (f.isDirectory())
        {
            if (f.listFiles().length == 0)
            {
                if (!f.delete())
                    throw new IOException("delete failure!");
            } else
            {
                File[] delFile = f.listFiles();
                int i = delFile.length;
                for (int j = 0; j < i; j++)
                {
                    delDirectory(delFile[j]);
                }
                if (!f.delete())
                {
                    throw new IOException("delete failure!");
                }
            }
        } else if (!f.delete())
            throw new IOException("delete failure!");
    }

    public static List<File> recursionFile(File base, FileFilter filter, boolean listAll)
    {
        List<File> list = new LinkedList<File>();
        if ((filter == null) || (filter.accept(base)))
        {
            list.add(base);
            if (!listAll)
                return list;
        }
        if ((base != null) && (base.isDirectory()))
        {
            File[] f = base.listFiles();
            for (int i = 0; i < f.length; i++)
            {
                List<File> subList = recursionFile(f[i], filter, listAll);
                list.addAll(subList);
                if ((!listAll) && (list.size() > 0))
                    return list;
            }
        }
        return list;
    }
}