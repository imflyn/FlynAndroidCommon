package com.flyn.net.volcano;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;

public class MultiByteParser
{
    private static final String          TAG                      = MultiByteParser.class.getName();

    private static final int             DEAULT_BUFFER_SIZE       = 4096;
    private static final String          APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final byte[]          CR_LF                    = ("\r\n").getBytes();
    private static final byte[]          TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary\r\n".getBytes();
    private final static char[]          MULTIPART_CHARS          = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private String                       boundary;
    private byte[]                       boundaryLine;
    private byte[]                       boundaryEnd;

    private IResponseHandler             progressHandler;

    private int                          bytesWritten;

    private int                          totalSize;
    private ByteArrayPool                mPool;
    private PoolingByteArrayOutputStream out;
    private List<FilePart>               fileParts                = new ArrayList<FilePart>(3);

    public MultiByteParser(IResponseHandler progressHandler)
    {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++)
        {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        this.boundary = buf.toString();
        this.boundaryLine = ("--" + this.boundary + "\r\n").getBytes();
        this.boundaryEnd = ("--" + this.boundary + "--\r\n").getBytes();

        this.mPool = new ByteArrayPool(DEAULT_BUFFER_SIZE);
        this.out = new PoolingByteArrayOutputStream(this.mPool);
        
        this.progressHandler=progressHandler;
    }

    public void addPart(String key, String value, String contentType)
    {
        try
        {
            this.out.write(boundaryLine);
            this.out.write(createContentDisposition(key));
            this.out.write(createContentType(contentType));
            this.out.write(CR_LF);
            this.out.write(value.getBytes());
            this.out.write(CR_LF);
        } catch (final IOException e)
        {
            Log.e(TAG, "addPart ByteArrayOutputStream exception", e);
        }
    }

    public void addPart(final String key, final String value)
    {
        addPart(key, value, "text/plain; charset=UTF-8");
    }

    public void addPart(String key, File file)
    {
        addPart(key, file, null);
    }

    public void addPart(final String key, File file, String type)
    {
        if (type == null)
        {
            type = APPLICATION_OCTET_STREAM;
        }
        this.fileParts.add(new FilePart(key, file, type));
    }

    public void addPart(String key, String streamName, InputStream inputStream, String type) throws IOException
    {
        if (type == null)
        {
            type = APPLICATION_OCTET_STREAM;
        }
        this.out.write(this.boundaryLine);

        // Headers
        this.out.write(createContentDisposition(key, streamName));
        this.out.write(createContentType(type));
        this.out.write(TRANSFER_ENCODING_BINARY);
        this.out.write(CR_LF);

        // Stream (file)
        final byte[] tmp = new byte[4096];
        int l;
        while ((l = inputStream.read(tmp)) != -1)
        {
            this.out.write(tmp, 0, l);
        }

        this.out.write(CR_LF);
        this.out.flush();
        try
        {
            inputStream.close();
        } catch (final IOException e)
        {
            // Not important, just log it
            Log.w(TAG, "Cannot close input stream", e);
        }
    }

    public byte[] getData()
    {
        this.bytesWritten = 0;
        this.totalSize = (int) getContentLength();
        updateProgress(this.out.size());
        byte [] result=null;
        try
        {
            for (FilePart filePart : this.fileParts)
            {
                filePart.writeTo(this.out);
            }
            this.out.write(this.boundaryEnd);
            result=this.out.toByteArray();
        } catch (IOException e)
        {
            Log.e(TAG, "Uploading IOException:" + e.getMessage());
        } finally
        {
            try
            {
                this.out.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        updateProgress(this.boundaryEnd.length);

        return result;
    }

    private byte[] createContentDisposition(final String key)
    {
        return ("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes();
    }

    private byte[] createContentDisposition(final String key, final String fileName)
    {
        return ("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes();
    }

    private byte[] createContentType(String type)
    {
        String result = "Content-Type: " + type + "\r\n";
        return result.getBytes();
    }

    private void updateProgress(int count)
    {
        this.bytesWritten += count;
        this.progressHandler.sendProgress(this.bytesWritten, this.totalSize);
    }

    public long getContentLength()
    {
        long contentLen = this.out.size();
        for (FilePart filePart : this.fileParts)
        {
            long len = filePart.getTotalLength();
            if (len < 0)
            {
                return -1; // Should normally not happen
            }
            contentLen += len;
        }
        contentLen += this.boundaryEnd.length;
        return contentLen;
    }

    private final class FilePart
    {
        public File   file;
        public byte[] header;

        public FilePart(String key, File file, String type)
        {

            this.file = file;
            this.header = createHeader(key, file.getName(), type);
        }

        private byte[] createHeader(String key, String filename, String type)
        {
            byte [] result=null;
            PoolingByteArrayOutputStream outStream = new PoolingByteArrayOutputStream(mPool);
            try
            {

                outStream.write(boundaryLine);

                outStream.write(createContentDisposition(key, filename));
                outStream.write(createContentType(type));
                outStream.write(TRANSFER_ENCODING_BINARY);
                outStream.write(CR_LF);
                result=outStream.toByteArray();
            } catch (IOException e)
            {

                Log.e(TAG, "createHeader PoolingByteArrayOutputStream exception.", e);
            } finally
            {

                try
                {
                    if (null != outStream)
                        outStream.close();
                } catch (IOException e)
                {
                    Log.e(TAG, "Close PoolingByteArrayOutputStream exception.", e);
                }
            }

            return result;
        }

        private long getTotalLength()
        {
            long length = this.file.length() + CR_LF.length;
            return this.header.length + length;
        }

        public void writeTo(OutputStream out) throws IOException
        {
            out.write(this.header);
            updateProgress(this.header.length);

            FileInputStream inStream = new FileInputStream(this.file);
            try
            {
                final byte[] temp = new byte[4096];
                int l;
                while ((l = inStream.read(temp)) != -1)
                {
                    out.write(temp, 0, l);
                    updateProgress(l);
                }
                out.write(CR_LF);
                updateProgress(CR_LF.length);
                out.flush();
            } catch (IOException e)
            {
                throw new IOException("writeTo exception ");
            } finally
            {
                inStream.close();
            }

        }

    }

}
