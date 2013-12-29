package com.flyn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class JLog
{
    private final static String           JLOG_PATH                 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FlynAndroidCommon" + File.separator + "Log"; // 日志文件在sdcard中的路径
    private final static int              SDCARD_LOG_FILE_SAVE_DAYS = 30;                                                                                                                         // sd卡中日志文件的最多保存天数
    private final static String           JLOG_FILE_Name            = "JLog.txt";                                                                                                                 // 本类输出的日志文件名称
    private final static SimpleDateFormat JLOG_MSG_FORMAT           = new SimpleDateFormat("yyyy-MM-dd");                                                                                         // 日志的输出格式
    private final static SimpleDateFormat JLOG_FORMAT               = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");                                                                                // 日志的输出格式

    /**
     * 打开日志文件并写入日志
     * 
     * @return
     **/
    public static void writeLogtoFile(String mylogtype, String tag, String text)
    {
        Date nowtime = new Date();
        String needWriteFile = JLOG_MSG_FORMAT.format(nowtime);
        String needWriteMessage = JLOG_FORMAT.format(nowtime) + "    " + mylogtype + "    " + tag + "    " + text;
        File file = new File(JLOG_PATH, needWriteFile + JLOG_FILE_Name);
        File dirFile = new File(JLOG_PATH);
        if (!dirFile.exists())// 判断目录是否已经创建
        {
            dirFile.mkdir();
        }
        FileWriter filerWriter = null;
        BufferedWriter bufWriter = null;
        try
        {
            filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {

            try
            {
                if (null != filerWriter)
                    filerWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                if (null != bufWriter)
                    bufWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
