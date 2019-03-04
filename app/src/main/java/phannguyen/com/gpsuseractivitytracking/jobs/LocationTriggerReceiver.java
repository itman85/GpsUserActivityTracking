package phannguyen.com.gpsuseractivitytracking.jobs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import phannguyen.com.gpsuseractivitytracking.Utils;

public class LocationTriggerReceiver extends BroadcastReceiver {
    private static final String TAG = "LocationTriggerRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"LocationTriggerRc OnReceive");
        Utils.appendLog(TAG,"I","LocationTriggerRc OnReceive");
        LocationTrackingJobIntentService.enqueueWork(context,intent);
    }
}
