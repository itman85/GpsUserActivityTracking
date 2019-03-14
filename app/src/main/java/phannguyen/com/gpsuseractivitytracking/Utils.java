package phannguyen.com.gpsuseractivitytracking;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import phannguyen.com.gpsuseractivitytracking.geofencing.GeoFencingPlaceModel;

public class Utils {
    public static void appendLog(String tag,String type,String text)
    {
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String dateFormat = s.format(new Date());

                File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/testlocation/mylog_"+tag+".txt");
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

    public static List<GeoFencingPlaceModel> createListGeoFencingPlaces(){
        List<GeoFencingPlaceModel> geoList = new ArrayList<>();
        geoList.add(new GeoFencingPlaceModel(10.776677, 106.683699,100,"CMT8_DBP"));
        geoList.add(new GeoFencingPlaceModel(10.775034, 106.686850,100,"CMT8_NDC"));
        geoList.add(new GeoFencingPlaceModel(10.772703, 106.691175,200,"CMT8_BTX"));

        geoList.add(new GeoFencingPlaceModel(10.775911, 106.682737,100,"NTHien_DBPhu"));
        geoList.add(new GeoFencingPlaceModel(10.771550, 106.685651,50,"NTHhien_VVTan"));
        geoList.add(new GeoFencingPlaceModel(10.770002, 106.688607,100,"BTXuan_TTTung"));
        geoList.add(new GeoFencingPlaceModel(10.767928, 106.695467,200,"NTHoc_THDao"));
        geoList.add(new GeoFencingPlaceModel(10.759683, 106.698477,100,"HDieu_KHoi"));
        geoList.add(new GeoFencingPlaceModel(10.753596, 106.702088,200,"CauKTe"));
        geoList.add(new GeoFencingPlaceModel(10.745254, 106.701887,100,"NHTho_D15"));
        geoList.add(new GeoFencingPlaceModel(10.745970, 106.708287,100,"Home"));

        geoList.add(new GeoFencingPlaceModel(10.778449, 106.679971,200,"Workplace"));


        return geoList;
    }
}
