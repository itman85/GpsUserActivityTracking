package phannguyen.com.gpsuseractivitytracking;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static void appendLog(String tag,String type,String text)
    {
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String dateFormat = s.format(new Date());

                File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/mylog_"+tag+".txt");
                if (!logFile.exists())
                {
                    try
                    {
                        logFile.createNewFile();
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                try
                {
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(dateFormat + "  " + type+ "/" + tag + ": "+text);
                    buf.newLine();
                    buf.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        task.start();

    }
}
