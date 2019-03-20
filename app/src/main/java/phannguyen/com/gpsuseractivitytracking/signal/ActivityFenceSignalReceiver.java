package phannguyen.com.gpsuseractivitytracking.signal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

import phannguyen.com.gpsuseractivitytracking.Utils;

import static phannguyen.com.gpsuseractivitytracking.Constants.ACTIVITY_FENCE_KEY;
import static phannguyen.com.gpsuseractivitytracking.PendingIntentUtils.ACTIVITY_SIGNAL_RECEIVER_ACTION;

public class ActivityFenceSignalReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivitySignalRc";
    private static final String TAG1 = "GeoFencingSignalRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Activity Fence onReceive");
        //Utils.appendLog(TAG,"I","Activity Fence onReceive");
        if (!TextUtils.equals(ACTIVITY_SIGNAL_RECEIVER_ACTION, intent.getAction())) {
            Log.i(TAG,"Received an unsupported action in ActivityFenceSignalReceiver: action="
                    + intent.getAction());
            return;
        }

        // The state information for the given fence is em
        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), ACTIVITY_FENCE_KEY)) {
            //String fenceStateStr;
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    stopLocationTrackingService(context);
                    break;
                case FenceState.FALSE:
                    startLocationTrackingService(context);
                    break;
                case FenceState.UNKNOWN:
                    stopLocationTrackingService(context);
                    break;
                default:
                    stopLocationTrackingService(context);
            }
            //Log.i(TAG,"Fence state: " + fenceStateStr);
            //Utils.appendLog(TAG,"I","Fence state: " + fenceStateStr);
        }/*else if(TextUtils.equals(fenceState.getFenceKey(), EXITING_LOCATION_FENCE_KEY)){
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG,"**** User exit location geo fencing");
                    Utils.appendLog(TAG,"I","*** User exit location geo fencing");
                    break;
                case FenceState.FALSE:
                    Log.i(TAG,"User not exit location geo fencing");
                    Utils.appendLog(TAG,"I","User not exit location geo fencing");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG,"User unknown exit location geo fencing");
                    Utils.appendLog(TAG,"I","User unknown exit location geo fencing");
                    break;
                default:
                    Log.i(TAG,"User unknown exit location geo fencing");
                    Utils.appendLog(TAG,"I","User unknown exit location geo fencing");
            }
        }else if(TextUtils.equals(fenceState.getFenceKey(), ENTERING_LOCATION_FENCE_KEY)){
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG,"*** User enter location geo fencing");
                    Utils.appendLog(TAG,"I","*** User enter location geo fencing");
                    break;
                case FenceState.FALSE:
                    Log.i(TAG,"User not enter location geo fencing");
                    Utils.appendLog(TAG,"I","User not enter location geo fencing");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG,"User unknown enter location geo fencing");
                    Utils.appendLog(TAG,"I","User unknown enter location geo fencing");
                    break;
                default:
                    Log.i(TAG,"User unknown enter location geo fencing");
                    Utils.appendLog(TAG,"I","User unknown enter location geo fencing");
            }
        }*/else {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG1,"******* User "+fenceState.getFenceKey());
                    Utils.appendLog(TAG1,"I","******* User "+fenceState.getFenceKey());
                    break;
                case FenceState.FALSE:
                    Log.i(TAG1,"User NOT "+fenceState.getFenceKey());
                    //Utils.appendLog(TAG1,"I","User NOT "+fenceState.getFenceKey());
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG1,"User UNKNOWN "+fenceState.getFenceKey());
                    //Utils.appendLog(TAG1,"I","User UNKNOWN "+fenceState.getFenceKey());
                    break;
                default:
                    Log.i(TAG1,"User _UNKNOWN "+fenceState.getFenceKey());
                    //Utils.appendLog(TAG1,"I","User _UNKNOWN "+fenceState.getFenceKey());
            }
        }
    }

    private void startLocationTrackingService(Context context){
        Log.i(TAG,"User MOVE SIGNAL - Start Location Tracking Job IS");
        Utils.appendLog(TAG,"I","User MOVE SIGNAL - Start Location Tracking Job IS");
        /*Intent serviceIntent = new Intent(context,CoreTrackingJobService.class);
        serviceIntent.putExtra(Constants.SIGNAL_KEY,Constants.SIGNAL.MOVE.toString());
        CoreTrackingJobService.enqueueWork(context,serviceIntent);*/
    }

    private void stopLocationTrackingService(Context context){
        Log.i(TAG,"USER NOT MOVE SIGNAL");
        Utils.appendLog(TAG,"I","USER NOT MOVE SIGNAL");
        //let location tracking decide to stop tracking or not
        //LocationTrackingJobIntentService.cancelLocationTriggerAlarm(context);

    }
}
