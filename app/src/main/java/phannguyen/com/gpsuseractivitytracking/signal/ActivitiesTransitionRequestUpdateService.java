package phannguyen.com.gpsuseractivitytracking.signal;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import phannguyen.com.gpsuseractivitytracking.Constants;
import phannguyen.com.gpsuseractivitytracking.PendingIntentUtils;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.geofencing.GeoFencingPlaceModel;

import static phannguyen.com.gpsuseractivitytracking.Constants.ACTIVITY_STILL_FENCE_KEY;


public class ActivitiesTransitionRequestUpdateService extends Service {
    private static final String TAG = "ActivityTrackingSv";
    PendingIntent mPendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");//call first time start service, if start again it will call onStartCommand directly
        Utils.appendLog(TAG, "I", "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");//call whenever start service call
        Utils.appendLog(TAG, "I", "onStartCommand");
        if (intent.hasExtra("action") && "START".equals(intent.getStringExtra("action"))) {
            if (mPendingIntent == null) {
                setupFences();
                setupGeoFencing();
                //setupActivityTransitions();
            }

        } else if (intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))) {
            //removeActivityTransitionUpdateRequest();
            removeFence();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        Utils.appendLog(TAG, "I", "onDestroy");
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
        Log.i(TAG, "Register for Transitions Updates");
        Utils.appendLog(TAG, "I", "Register for activity update interval");
        Task<Void> task =
                ActivityRecognition.getClient(this)
                        .requestActivityUpdates(0,mPendingIntent);
                        //.requestActivityTransitionUpdates(request, mPendingIntent);
        task.addOnSuccessListener(
                result -> {
                    Log.i(TAG, "Transitions Api was successfully registered.");
                    Utils.appendLog(TAG, "I", "Transitions Api was successfully registered.");
                    //stop service
                    stopSelf();
                });
        task.addOnFailureListener(
                e -> {
                    Log.e(TAG, "Transitions Api could not be registered: " + e);
                    Utils.appendLog(TAG, "E", "Transitions Api could not be registered: " + e.getMessage());
                    //start get gps location not depend on user activities
                    stopSelf();
                });
    }

    /**
     * when stop gps tracking, no need to track user's activities
     */
    public void removeActivityTransitionUpdateRequest() {
        PendingIntent pendingIntent = PendingIntentUtils.createTransitionTrackingPendingIntent(this);
        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Transitions successfully unregistered.");
                    Utils.appendLog(TAG, "I", "Transitions successfully unregistered.");
                    stopSelf();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Transitions could not be unregistered: " + e);
                    Utils.appendLog(TAG, "E", "Transitions could not be unregistered: " + e.getMessage());
                    stopSelf();
                });
    }

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
        mPendingIntent = PendingIntentUtils.getFenceAwareNessPendingIntent(this);
        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(ACTIVITY_STILL_FENCE_KEY, stayFence, mPendingIntent)
                .build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Activity Fence was successfully registered.");
                    Utils.appendLog(TAG, "I", "Activity Fence was successfully registered.");
                    stopSelf();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activity Fence could not be registered: " + e);
                    Utils.appendLog(TAG, "E", "Activity Fence could not be registered: " + e);
                    stopSelf();
                });
    }

    private void removeFence() {
        FenceUpdateRequest.Builder fenceReqBuilder = new FenceUpdateRequest.Builder();
        List<GeoFencingPlaceModel> geoFencingList = Utils.createListGeoFencingPlaces();
        for(GeoFencingPlaceModel model:geoFencingList){
            fenceReqBuilder.removeFence("exit_"+model.getName());
            fenceReqBuilder.removeFence("enter_"+model.getName());
            fenceReqBuilder.removeFence("dwell_"+model.getName());
        }

        Awareness.getFenceClient(this).updateFences(fenceReqBuilder
                .removeFence(ACTIVITY_STILL_FENCE_KEY)
                //.removeFence(EXITING_LOCATION_FENCE_KEY)
                //.removeFence(ENTERING_LOCATION_FENCE_KEY)
                .build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Activity&Geo Fence were successfully unregistered.");
                    Utils.appendLog(TAG, "I", "Activity&Geo Fence were successfully unregistered.");
                    stopSelf();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activity&Geo Fence could not be unregistered: " + e);
                    Utils.appendLog(TAG, "E", "Activity&Geo Fence could not be unregistered: " + e);
                    stopSelf();
                });

    }

    private void setupGeoFencing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        /*AwarenessFence exitingLocationFence = LocationFence.exiting(
                10.740393, 106.700903, 200);
        AwarenessFence enteringLocationFence = LocationFence.entering(
                10.740393, 106.700903, 200);*/
        PendingIntent pendingIntent = PendingIntentUtils.getFenceAwareNessPendingIntent(this);
        FenceUpdateRequest.Builder fenceReqBuilder = new FenceUpdateRequest.Builder();
        List<GeoFencingPlaceModel> geoFencingList = Utils.createListGeoFencingPlaces();
        for(GeoFencingPlaceModel model:geoFencingList){
            AwarenessFence exitingLocationFence = LocationFence.exiting(
                    model.getLat(), model.getLng(), model.getRadius());
            AwarenessFence enteringLocationFence = LocationFence.entering(
                    model.getLat(), model.getLng(), model.getRadius());
            AwarenessFence dwellLocationFence = LocationFence.in(
                    model.getLat(), model.getLng(), model.getRadius(),Constants.DWELL_TIME_IN_MS);

            fenceReqBuilder.addFence("exit_"+model.getName(),exitingLocationFence,pendingIntent);
            fenceReqBuilder.addFence("enter_"+model.getName(),enteringLocationFence,pendingIntent);
            fenceReqBuilder.addFence("dwell_"+model.getName(),dwellLocationFence,pendingIntent);
        }


        Awareness.getFenceClient(this).updateFences(fenceReqBuilder.build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Activity Geo Fence List were successfully registered.");
                    Utils.appendLog(TAG, "I", "Activity Geo Fence List were successfully registered.");
                    stopSelf();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activity Geo Fence List could not be registered: " + e);
                    Utils.appendLog(TAG, "E", "Activity Geo Fence List could not be registered: " + e);
                    stopSelf();
                });
    }



}
