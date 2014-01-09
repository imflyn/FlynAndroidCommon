
package com.flyn.net.volcano;

import java.util.Map;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

/**
 * Utility methods for parsing HTTP headers.
 */
public class HttpHeaderParser
{


    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr)
    {
        try
        {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e)
        {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header, or the
     * HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Map<String, String> headers)
    {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null)
        {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++)
            {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2)
                {
                    if (pair[0].equals("charset"))
                    {
                        return pair[1];
                    }
                }
            }
        }

        return HTTP.DEFAULT_CONTENT_CHARSET;
    }
}
