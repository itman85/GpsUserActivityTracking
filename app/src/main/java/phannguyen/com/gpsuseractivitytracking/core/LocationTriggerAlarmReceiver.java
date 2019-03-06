package phannguyen.com.gpsuseractivitytracking.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.jobs.LocationTrackingJobIntentService;

public class LocationTriggerAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationAlarmRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Location Alarm onReceive");
        Utils.appendLog(TAG,"I","Location Alarm onReceive");
        CoreTrackingJobService.enqueueWork(context,intent);
    }
}
