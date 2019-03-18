package phannguyen.com.gpsuseractivitytracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import phannguyen.com.gpsuseractivitytracking.jobs.LocationTrackingJobIntentService;

import static phannguyen.com.gpsuseractivitytracking.PendingIntentUtils.TRANSITIONS_RECEIVER_ACTION;

/**
 * A basic BroadcastReceiver to handle intents from from the Transitions API.
 */
public class TransitionTrackingReceiver extends BroadcastReceiver {

    private static final String TAG = "TransitionTrackingRc";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle the Activity Transition Response
        Log.i(TAG,"TransitionTrackingRc OnReceive");
        Utils.appendLog(TAG,"I","TransitionTrackingRc OnReceive");
        if (!TRANSITIONS_RECEIVER_ACTION.equals(intent.getAction())) {
            Log.i(TAG,"Received an unsupported action in TransitionTrackingReceiver: action="
                    + intent.getAction());
            Utils.appendLog(TAG,"I","Received an unsupported action in TransitionTrackingReceiver: action="
                    + intent.getAction());
            return;
        }

        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostProbableActivity
                    = result.getMostProbableActivity();

            // Get the confidence % (probability)
            int confidence = mostProbableActivity.getConfidence();

            // Get the type
            int activityType = mostProbableActivity.getType();
            String activity = toActivityString(mostProbableActivity.getType());
            Utils.appendLog(TAG,"I","Activity: "
                    + activity + " (confidence: " + confidence + ")" + "   "
                    + new SimpleDateFormat("HH:mm:ss", Locale.US)
                    .format(new Date()));
            /* types:
             * DetectedActivity.IN_VEHICLE
             * DetectedActivity.ON_BICYCLE
             * DetectedActivity.ON_FOOT
             * DetectedActivity.STILL
             * DetectedActivity.UNKNOWN
             * DetectedActivity.TILTING
             */
            // process
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
                Utils.appendLog(TAG,"I","Transition: "
                        + activity + " (" + transitionType + ")" + "   "
                        + new SimpleDateFormat("HH:mm:ss", Locale.US)
                        .format(new Date()));

                //start request update location when device exit still
                if(event.getActivityType() == DetectedActivity.STILL && event.getTransitionType()==ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                    /*Intent serviceIntent = new Intent(context,LocationRequestUpdateService.class);
                    serviceIntent.putExtra("action","START");
                    context.startService(serviceIntent);*/
                    //start tracking location trigger interval
                    Log.i(TAG,"Start Location Tracking Job IS");
                    Utils.appendLog(TAG,"I","Start Location Tracking Job IS");
                    Intent serviceIntent = new Intent(context,LocationTrackingJobIntentService.class);
                    serviceIntent.putExtra("action","START");
                    //LocationTrackingJobIntentService.enqueueWork(context,serviceIntent);


                }else if(event.getActivityType() == DetectedActivity.STILL && event.getTransitionType()==ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                    //device enter still, so stop request update location, no need to drain battery
                    /*Intent serviceIntent = new Intent(context,LocationRequestUpdateService.class);
                    serviceIntent.putExtra("action","STOP");
                    context.startService(serviceIntent);*/
                    //stop tracking location trigger interval
                    Log.i(TAG,"Cancel Location Tracking Alarm");
                    Utils.appendLog(TAG,"I","Cancel Location Tracking Alarm");
                    //LocationTrackingJobIntentService.cancelLocationTriggerAlarm(context);

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
            case DetectedActivity.TILTING:
                return "TILTING";
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
