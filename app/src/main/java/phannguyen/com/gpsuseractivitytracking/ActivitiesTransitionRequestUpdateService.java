package phannguyen.com.gpsuseractivitytracking;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;


public class ActivitiesTransitionRequestUpdateService extends Service {
    private static final String TAG = "LTTrackingService" ;
    PendingIntent mPendingIntent;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");//call first time start service, if start again it will call onStartCommand directly
        Utils.appendLog(TAG,"I","onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");//call whenever start service call
        Utils.appendLog(TAG,"I","onStartCommand");
        if(intent.hasExtra("action") && "START".equals(intent.getStringExtra("action"))) {
            if (mPendingIntent == null)
                setupFences();
                //setupActivityTransitions();
        }else if(intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))){
            //removeActivityTransitionUpdateRequest();
            removeFence();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        Utils.appendLog(TAG,"I","onDestroy");
        //removeActivityTransitionUpdate();
    }


    /**
     * https://proandroiddev.com/new-activity-recognition-transition-api-f4cdb5cd5708
     *
     * https://codelabs.developers.google.com/codelabs/activity-recognition-transition/index.html#0
     */
    private void setupActivityTransitions() {
        mPendingIntent = PendingIntentUtils.createTransitionTrackingPendingIntent(this);
        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        // Register for Transitions Updates.
        Log.i(TAG,"Register for Transitions Updates");
        Utils.appendLog(TAG,"I","Register for Transitions Updates");
        Task<Void> task =
                ActivityRecognition.getClient(this)
                        .requestActivityTransitionUpdates(request, mPendingIntent);
        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.i(TAG, "Transitions Api was successfully registered.");
                        Utils.appendLog(TAG,"I","Transitions Api was successfully registered.");
                        //stop service
                        stopSelf();
                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Transitions Api could not be registered: " + e);
                        Utils.appendLog(TAG,"E","Transitions Api could not be registered: " + e.getMessage());
                        //start get gps location not depend on user activities
                        stopSelf();
                    }
                });
    }

    /**
     * when stop gps tracking, no need to track user's activities
     */
    public void removeActivityTransitionUpdateRequest(){
        PendingIntent pendingIntent = PendingIntentUtils.createTransitionTrackingPendingIntent(this);
        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Transitions successfully unregistered.");
                        Utils.appendLog(TAG,"I","Transitions successfully unregistered.");
                        stopSelf();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Transitions could not be unregistered: " + e);
                        Utils.appendLog(TAG,"E","Transitions could not be unregistered: " + e.getMessage());
                        stopSelf();
                    }
                });
    }

    public static  final String FENCE_KEY = "fence_key";
    private void setupFences() {
        // DetectedActivityFence will fire when it detects the user performing the specified
        // activity.  In this case it's walking.
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);

        AwarenessFence stayFence = DetectedActivityFence.during(DetectedActivityFence.STILL);

        // There are lots of cases where it's handy for the device to know if headphones have been
        // plugged in or unplugged.  For instance, if a music app detected your headphones fell out
        // when you were in a library, it'd be pretty considerate of the app to pause itself before
        // the user got in trouble.
        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        // Combines multiple fences into a compound fence.  While the first two fences trigger
        // individually, this fence will only trigger its callback when all of its member fences
        // hit a true state.
        AwarenessFence walkingWithHeadphones = AwarenessFence.and(walkingFence, headphoneFence);

        // We can even nest compound fences.  Using both "and" and "or" compound fences, this
        // compound fence will determine when the user has headphones in and is engaging in at least
        // one form of exercise.
        // The below breaks down to "(headphones plugged in) AND (walking OR running OR bicycling)"
        AwarenessFence exercisingWithHeadphonesFence = AwarenessFence.and(
                headphoneFence,
                AwarenessFence.or(
                        walkingFence,
                        DetectedActivityFence.during(DetectedActivityFence.RUNNING),
                        DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE)));


        // Now that we have an interesting, complex condition, register the fence to receive
        // callbacks.

        // Register the fence to receive callbacks.
        mPendingIntent =  PendingIntentUtils.getFenceAwareNessPendingIntent(this);
        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(FENCE_KEY, stayFence,mPendingIntent)
                .build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Fence was successfully registered.");
                    Utils.appendLog(TAG,"I","Fence was successfully registered.");
                    stopSelf();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fence could not be registered: " + e);
                    Utils.appendLog(TAG,"E","Fence could not be registered: " + e);
                    stopSelf();
                });
    }

    private void removeFence(){
        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .removeFence(FENCE_KEY)
                .build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Fence was successfully unregistered.");
                    Utils.appendLog(TAG,"I","Fence was successfully unregistered.");
                    stopSelf();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Fence could not be unregistered: " + e);
                    Utils.appendLog(TAG,"E","Fence could not be unregistered: " + e);
                    stopSelf();
                });

    }

}
