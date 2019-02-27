package phannguyen.com.gpsuseractivitytracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static phannguyen.com.gpsuseractivitytracking.PendingIntentUtils.TRANSITIONS_RECEIVER_ACTION;

/**
 * A basic BroadcastReceiver to handle intents from from the Transitions API.
 */
public class TransitionTrackingReceiver extends BroadcastReceiver {

    private static final String TAG = "TransitionTrackingRc";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle the Activity Transition Response
        if (!TRANSITIONS_RECEIVER_ACTION.equals(intent.getAction())) {
            Log.i(TAG,"Received an unsupported action in TransitionTrackingReceiver: action="
                    + intent.getAction());
            return;
        }
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                String activity = toActivityString(event.getActivityType());
                String transitionType = toTransitionType(event.getTransitionType());
                Log.i(TAG,"Transition: "
                        + activity + " (" + transitionType + ")" + "   "
                        + new SimpleDateFormat("HH:mm:ss", Locale.US)
                        .format(new Date()));

                //start request update location when device exit still
                if(event.getTransitionType()==ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                    Intent serviceIntent = new Intent(context,LocationRequestUpdateService.class);
                    serviceIntent.putExtra("action","START");
                    context.startService(serviceIntent);

                }else if(event.getTransitionType()==ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                    //device enter still, so stop request update location, no need to drain battery
                    Intent serviceIntent = new Intent(context,LocationRequestUpdateService.class);
                    serviceIntent.putExtra("action","STOP");
                    context.startService(serviceIntent);
                }
            }
        }
    }

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }
}
