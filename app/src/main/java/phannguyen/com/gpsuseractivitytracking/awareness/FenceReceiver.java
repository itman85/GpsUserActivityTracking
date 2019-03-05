package phannguyen.com.gpsuseractivitytracking.awareness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

import phannguyen.com.gpsuseractivitytracking.BuildConfig;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.jobs.LocationTrackingJobIntentService;

import static phannguyen.com.gpsuseractivitytracking.awareness.AwarenessActivity.FENCE_KEY;

public class FenceReceiver extends BroadcastReceiver {
    private static final String TAG = "FenceReceiver";
    public static final String FENCE_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive");
        Utils.appendLog(TAG,"I","onReceive");
        if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.getAction())) {
            Log.i(TAG,"Received an unsupported action in FenceReceiver: action="
                    + intent.getAction());
            return;
        }

        // The state information for the given fence is em
        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
            String fenceStateStr;
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    fenceStateStr = "true";
                    stopLocationTrackingService(context);
                    break;
                case FenceState.FALSE:
                    startLocationTrackingService(context);
                    fenceStateStr = "false";
                    break;
                case FenceState.UNKNOWN:
                    fenceStateStr = "unknown";
                    stopLocationTrackingService(context);
                    break;
                default:
                    fenceStateStr = "unknown value";
                    stopLocationTrackingService(context);
            }
            Log.i(TAG,"Fence state: " + fenceStateStr);
            Utils.appendLog(TAG,"I","Fence state: " + fenceStateStr);
        }
    }

    private void startLocationTrackingService(Context context){
        Log.i(TAG,"Start Location Tracking Job IS");
        Utils.appendLog(TAG,"I","Start Location Tracking Job IS");
        Intent serviceIntent = new Intent(context,LocationTrackingJobIntentService.class);
        serviceIntent.putExtra("action","START");
        LocationTrackingJobIntentService.enqueueWork(context,serviceIntent);
    }

    private void stopLocationTrackingService(Context context){
        Log.i(TAG,"Cancel Location Tracking Alarm");
        Utils.appendLog(TAG,"I","Cancel Location Tracking Alarm");
        LocationTrackingJobIntentService.cancelLocationTriggerAlarm(context);
    }
}
