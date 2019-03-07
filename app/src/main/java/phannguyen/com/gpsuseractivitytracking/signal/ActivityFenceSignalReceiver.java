package phannguyen.com.gpsuseractivitytracking.signal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

import phannguyen.com.gpsuseractivitytracking.Constants;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.core.CoreTrackingJobService;

import static phannguyen.com.gpsuseractivitytracking.Constants.ACTIVITY_FENCE_KEY;
import static phannguyen.com.gpsuseractivitytracking.PendingIntentUtils.ACTIVITY_SIGNAL_RECEIVER_ACTION;

public class ActivityFenceSignalReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivitySignalRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Activity Fence onReceive");
        Utils.appendLog(TAG,"I","Activity Fence onReceive");
        if (!TextUtils.equals(ACTIVITY_SIGNAL_RECEIVER_ACTION, intent.getAction())) {
            Log.i(TAG,"Received an unsupported action in ActivityFenceSignalReceiver: action="
                    + intent.getAction());
            return;
        }

        // The state information for the given fence is em
        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), ACTIVITY_FENCE_KEY)) {
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
            //Log.i(TAG,"Fence state: " + fenceStateStr);
            //Utils.appendLog(TAG,"I","Fence state: " + fenceStateStr);
        }
    }

    private void startLocationTrackingService(Context context){
        Log.i(TAG,"User MOVE SIGNAL - Start Location Tracking Job IS");
        Utils.appendLog(TAG,"I","User MOVE SIGNAL - Start Location Tracking Job IS");
        Intent serviceIntent = new Intent(context,CoreTrackingJobService.class);
        serviceIntent.putExtra(Constants.SIGNAL_KEY,Constants.SIGNAL.MOVE.toString());
        CoreTrackingJobService.enqueueWork(context,serviceIntent);
    }

    private void stopLocationTrackingService(Context context){
        Log.i(TAG,"USER NOT MOVE SIGNAL");
        Utils.appendLog(TAG,"I","USER NOT MOVE SIGNAL");
        //let location tracking decide to stop tracking or not
        //LocationTrackingJobIntentService.cancelLocationTriggerAlarm(context);

    }
}
