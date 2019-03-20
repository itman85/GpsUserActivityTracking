package phannguyen.com.gpsuseractivitytracking.android7.locationtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.core.CoreTrackingJobService;

public class LocationTrackingReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationTrackingRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationResult.hasResult(intent)) {
            CoreTrackingJobService.enqueueWork(context,intent);
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                // use the Location
                Log.i(TAG,"***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                Utils.appendLog(TAG,"I","***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            }
        }
    }
}
