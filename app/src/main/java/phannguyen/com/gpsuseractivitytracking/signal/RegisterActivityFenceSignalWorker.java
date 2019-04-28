package phannguyen.com.gpsuseractivitytracking.signal;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;

import java.util.List;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import phannguyen.com.gpsuseractivitytracking.Constants;
import phannguyen.com.gpsuseractivitytracking.PendingIntentUtils;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.core.storage.SharePref;
import phannguyen.com.gpsuseractivitytracking.geofencing.GeoFencingPlaceModel;

import static phannguyen.com.gpsuseractivitytracking.Constants.ACTIVITY_MOVE_FENCE_KEY;
import static phannguyen.com.gpsuseractivitytracking.Constants.ACTIVITY_STILL_FENCE_KEY;
import static phannguyen.com.gpsuseractivitytracking.Constants.INTERVAL_REGISTER_ACTIVITY_IN_MIN;

public class RegisterActivityFenceSignalWorker extends Worker {
    private static final String TAG = "RegisterActivityWorker";
    public static final String KEY_RESULT = "locationResult";
    private int count;
    public RegisterActivityFenceSignalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        count = 1;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG,"Work time "+ count + " at " +System.currentTimeMillis());
        setupFences();
        //setupGeoFencing();//this will affect detect move
        Data output = new Data.Builder()
                .putInt(KEY_RESULT, count)
                .build();
        final Handler handler = new Handler();
        handler.postDelayed(() -> Utils.startRegisterActivityOneTimeRequest(INTERVAL_REGISTER_ACTIVITY_IN_MIN), 500);
        return Result.success(output);
        //return null;
    }

    private void setupFences() {
        AwarenessFence stayFence = DetectedActivityFence.during(DetectedActivityFence.STILL);

        AwarenessFence moveFence = AwarenessFence.or(
                DetectedActivityFence.during(DetectedActivityFence.RUNNING),
                DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE),
                DetectedActivityFence.during(DetectedActivityFence.WALKING),
                DetectedActivityFence.during(DetectedActivityFence.ON_FOOT),
                DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE));

        PendingIntent pendingIntent = PendingIntentUtils.getFenceAwareNessPendingIntent(getApplicationContext());
        FenceUpdateRequest.Builder fenceReqBuilder = new FenceUpdateRequest.Builder();

        String lastKeyActivityFence = SharePref.getLastRegisterActivityFence(this.getApplicationContext());
        if(lastKeyActivityFence.equals(ACTIVITY_MOVE_FENCE_KEY)){
            fenceReqBuilder.addFence(ACTIVITY_STILL_FENCE_KEY,stayFence,pendingIntent)
                    .removeFence(ACTIVITY_MOVE_FENCE_KEY);
            //update last register activity fence
            SharePref.setLastRegisterActivityFence(this.getApplicationContext(),ACTIVITY_STILL_FENCE_KEY);
            lastKeyActivityFence = ACTIVITY_STILL_FENCE_KEY;
        }else if(lastKeyActivityFence.equals(ACTIVITY_STILL_FENCE_KEY)){
            fenceReqBuilder.addFence(ACTIVITY_MOVE_FENCE_KEY,moveFence,pendingIntent)
                    .removeFence(ACTIVITY_STILL_FENCE_KEY);
            //update last register activity fence
            SharePref.setLastRegisterActivityFence(this.getApplicationContext(),ACTIVITY_MOVE_FENCE_KEY);
            lastKeyActivityFence = ACTIVITY_MOVE_FENCE_KEY;
        }
        final String temp = lastKeyActivityFence;
        // Now that we have an interesting, complex condition, register the fence to receive
        // callbacks.
        Log.i(TAG, "Activity Fence now registered again.");
        Utils.appendLog(TAG,"I","Activity Fence now registered again for "+ temp);
        // Register the fence to receive callbacks.
        Awareness.getFenceClient(this.getApplicationContext()).updateFences(fenceReqBuilder.build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Activity Fence was successfully registered again.");
                    Utils.appendLog(TAG,"I","Activity Fence was successfully registered again for "+temp);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activity Fence could not be registered again: " + e);
                    Utils.appendLog(TAG,"E","Activity Fence could not be registered again: " + e + " for "+temp);
                });
    }

    private void setupGeoFencing() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        PendingIntent pendingIntent = PendingIntentUtils.getFenceAwareNessPendingIntent(getApplicationContext());
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
        Log.i(TAG, "Geo Fence now registered again.");
        Utils.appendLog(TAG,"I","Geo Fence now registered again.");

        Awareness.getFenceClient(getApplicationContext()).updateFences(fenceReqBuilder.build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Activity Geo Fence List were successfully registered.");
                    Utils.appendLog(TAG, "I", "Activity Geo Fence List were successfully registered again.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activity Geo Fence List could not be registered: " + e);
                    Utils.appendLog(TAG, "E", "Activity Geo Fence List could not be registered again: " + e);
                });
    }
}
