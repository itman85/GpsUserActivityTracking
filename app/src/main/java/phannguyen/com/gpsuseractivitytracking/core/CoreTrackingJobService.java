package phannguyen.com.gpsuseractivitytracking.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import phannguyen.com.gpsuseractivitytracking.Constants;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.android7.locationtracking.LocationRequestUpdateService1;
import phannguyen.com.gpsuseractivitytracking.core.storage.SharePref;
import phannguyen.com.gpsuseractivitytracking.geofencing.GeoFencingPlaceModel;
import phannguyen.com.gpsuseractivitytracking.geofencing.GeoFencingPlaceStatusModel;
import phannguyen.com.gpsuseractivitytracking.geofencing.GeofencingDataManagement;
import phannguyen.com.gpsuseractivitytracking.geofencing.service.GeofencingRequestUpdateService;
import phannguyen.com.gpsuseractivitytracking.signal.LocationTrackingIntervalWorker;

public class CoreTrackingJobService extends JobIntentService {
    private static final int JOB_ID = 1009;
    private static final String TAG = "CoreTrackingJobSv";
    private static final String TAG1 = "GeoFencingTracking";


    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, CoreTrackingJobService.class, JOB_ID, intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "CoreTrackingLocation job service on handle");
        Utils.appendLog(TAG, "I", "CoreTrackingLocation job service on handle");
        boolean res = handleLocationIntent(intent);
        if (!res) {
            /*boolean isStartTrackingSignal = handleIntentSignal(intent);
            boolean statusTracking = SharePref.getGpsTrackingStatus(this);
            if (isStartTrackingSignal && statusTracking)
                cancelLocationTriggerAlarm(this);//cancel for restart
            if (!statusTracking)
                SharePref.setGpsTrackingStatus(this, true);
            */
            //
            Utils.appendLog(TAG,"I","Get fused location now");
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    processLocationData(location);
                } else {
                    startAlarmLocationTrigger(Constants.INTERVAL_SLOW_MOVE_IN_MS);
                }
            }).addOnFailureListener(e -> {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                    Utils.appendLog(TAG, "I", "CoreTrackingLocation job service Error " + e.getMessage());
                }
                startAlarmLocationTrigger(Constants.INTERVAL_SLOW_MOVE_IN_MS);
            });

        }
    }

   /* private boolean handleLocationIntent(Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Utils.appendLog(TAG,"I","Receive location intent and process now");
                processLocationData(location);
                return true;
            }
        }
        return false;
    }*/

    private boolean handleLocationIntent(Intent intent) {
        if (LocationResult.hasResult(intent)) {
            Location location = intent.getParcelableExtra("com.google.android.gms.location.EXTRA_LOCATION_RESULT");
           // LocationResult locationResult = LocationResult.extractResult(intent);
           // Location location = locationResult.getLastLocation();
            if (location != null) {
                Utils.appendLog(TAG,"I","CoreTrackingLocation receive location intent and process now");
                processLocationData(location);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        //Utils.appendLog(TAG,"I","onDestroy");
    }

    private void startOnetimeRequest(int delayInSecond) {
        int lastInterval = SharePref.getLastSpeedInterval(this);
        ExistingWorkPolicy workPolicy = ExistingWorkPolicy.REPLACE;//replace by new request
        if (lastInterval == 0)
            SharePref.setLastSpeedInterval(this, delayInSecond);
        else {
            if (lastInterval <= delayInSecond)
                workPolicy = ExistingWorkPolicy.KEEP;//keep last request
            else {//new interval < last interval means that user speed up, move faster
                workPolicy = ExistingWorkPolicy.REPLACE;//replace by new request
                SharePref.setLastSpeedInterval(this, delayInSecond);
            }
        }
        OneTimeWorkRequest locationIntervalWork =
                new OneTimeWorkRequest.Builder(LocationTrackingIntervalWorker.class)
                        .setInitialDelay(delayInSecond, TimeUnit.SECONDS)
                        .addTag(Constants.LOCATION_TRACKING_INTERVAL_WORK_TAG)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();
        //WorkManager.getInstance().enqueue(locationIntervalWork);
        WorkManager.getInstance().enqueueUniqueWork(Constants.LOCATION_TRACKING_INTERVAL_WORK_UNIQUE_NAME, workPolicy, locationIntervalWork);
    }

    private void startAlarmLocationTrigger(int delayInMs) {
        //startOnetimeRequest(delayInMs/1000);
        OneTimeWorkRequest locationIntervalWork =
                new OneTimeWorkRequest.Builder(LocationTrackingIntervalWorker.class)
                        .setInitialDelay(delayInMs, TimeUnit.MILLISECONDS)
                        .addTag(Constants.LOCATION_TRACKING_INTERVAL_WORK_TAG)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();
        //WorkManager.getInstance().enqueue(locationIntervalWork);
        WorkManager.getInstance().enqueueUniqueWork(Constants.LOCATION_TRACKING_INTERVAL_WORK_UNIQUE_NAME, ExistingWorkPolicy.KEEP, locationIntervalWork);
        Utils.appendLog("WorkerQueue", "I", "Enqueue delay "+ (delayInMs/1000));
       /* AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntentUtils.getLocationTriggerAlarmPendingIntent(this);
        long triggerMoment = System.currentTimeMillis()+delayInMs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMoment,
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMoment,
                    pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMoment,
                    pendingIntent);
        }*/

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //this make alarm on time when device goes to doze mode, this will done_item_menu clock icon on device top bar
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerMoment, pendingIntent), pendingIntent);
        }*/
    }

    private static void cancelLocationTriggerAlarm(Context context) {
        Log.i(TAG, "Cancel Location Trigger Interval Worker");
        Utils.appendLog(TAG,"I","Cancel Location Trigger worker");
        SharePref.setGpsTrackingStatus(context, false);
        WorkManager.getInstance().cancelUniqueWork(Constants.LOCATION_TRACKING_INTERVAL_WORK_UNIQUE_NAME);
    }

    /**
     * @param intent
     * @return true force get location tracking, false: ignore
     */
    private boolean handleIntentSignal(Intent intent) {
        if (intent != null && intent.hasExtra(Constants.SIGNAL_KEY)) {
            Constants.SIGNAL fromSignal = Constants.SIGNAL.valueOf(intent.getStringExtra(Constants.SIGNAL_KEY));
            if (fromSignal == Constants.SIGNAL.MOVE) {
                Log.i(TAG, "Start Tracking From MOVE Signal");
                Utils.appendLog(TAG, "I", "Start Tracking From MOVE Signal");
                return true;
            }
            return false;
        }
        return false;
    }

    private void processLocationData(Location location) {
        boolean isMove = checkUserLocationData(location);
        Utils.appendLog("LocationData", "I", "Location Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
        //processGeoFencing(location);
        if (isMove) {
            Log.i(TAG, "***User moving Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
            Utils.appendLog(TAG, "I", "***User moving Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
            //check if location request update still alive, then start alarm
            startAlarmLocationTrigger(Constants.INTERVAL_SLOW_MOVE_IN_MS);
        } else {
            long lastStayMoment = SharePref.getLastMomentGPSNotChange(this);
            //check if user dont move for long time => user STILL
            if (System.currentTimeMillis() - lastStayMoment >= Constants.TIMEOUT_STAY_LOCATION) {
                Log.i(TAG, "***User Stay for certant time Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                Utils.appendLog(TAG, "I", "***User stay for certain time Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                getSnapshotCurrentActivity(this, location);
            } else {
                Log.i(TAG, "***User move slowly or stay a bit Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                Utils.appendLog(TAG, "I", "***User  move slowly or stay a bit Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                //check if location request update still alive, then start alarm
                startAlarmLocationTrigger(Constants.INTERVAL_VERY_SLOW_MOVE_IN_MS);
            }
        }
    }


    /**
     * @param location
     * @return true if user move, false if not move or move too slow
     */
    private boolean checkUserLocationData(Location location) {
        float lastLng = SharePref.getLastLngLocation(this);
        float lastLat = SharePref.getLastLatLocation(this);
        if (lastLat == 0 || lastLng == 0) {
            updateLastLocation((float) location.getLatitude(), (float) location.getLongitude(), System.currentTimeMillis());
            return true;
        } else {
            float distance = getMetersFromLatLong(lastLat, lastLng, (float) location.getLatitude(), (float) location.getLongitude());
            //seem not move
            if (distance <= Constants.STAY_DISTANCE_IN_MET) {
                return false;
            } else {
                //user move
                updateLastLocation((float) location.getLatitude(), (float) location.getLongitude(), System.currentTimeMillis());
                return true;
            }
        }
    }

    private float getMetersFromLatLong(float lat1, float lng1, float lat2, float lng2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }

    private float getMetersFromLatLong(double lat1, double lng1, double lat2, double lng2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }


    private void updateLastLocation(float lat, float lng, long time) {
        SharePref.setLastLatLocation(this, lat);
        SharePref.setLastLngLocation(this, lng);
        SharePref.setLastMomentGPSNotChange(this, time);
    }

    private void getSnapshotCurrentActivity(Context context, Location location) {
        Awareness.getSnapshotClient(context).getDetectedActivity()
                .addOnSuccessListener(dar -> {
                    ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                    // getMostProbableActivity() is good enough for basic Activity detection.
                    // To work within a threshold of confidence,
                    // use ActivityRecognitionResult.getProbableActivities() to get a list of
                    // potential current activities, and check the confidence of each one.
                    DetectedActivity probableActivity = arr.getMostProbableActivity();
                    int confidence = probableActivity.getConfidence();
                    String activityStr = probableActivity.toString();
                    Log.i(TAG, "Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    Utils.appendLog(TAG, "I", "Get Snapshot Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    //check if STILL now, so cancel tracking
                    if (probableActivity.getType() == DetectedActivity.STILL && confidence >= 90) {
                        //user still, so cancel tracking location alarm
                        Utils.appendLog(TAG, "I", "User STILL now, Cancel LocationRequestUpdateService");
                        //stop interval check location work
                        cancelLocationTriggerAlarm(context);
                        //stop location tracking
                        Intent serviceIntent = new Intent(context, LocationRequestUpdateService1.class);
                        serviceIntent.putExtra("action", "STOP");
                        startService(serviceIntent);
                        //stop geo fencing tracking
                        Intent serviceIntent1 = new Intent(context, GeofencingRequestUpdateService.class);
                        serviceIntent1.putExtra("action","STOP");
                        startService(serviceIntent1);
                        //TODO: when user STILL will check if in/out geo fencing later ...

                        //remove location request update by pending intent in signal
                        /*if(SharePref.getLocationRequestUpdateStatus(context)) {
                            SharePref.setLocationRequestUpdateStatus(context, false);
                            PendingIntent pendingIntent = PendingIntentUtils.createLocationTrackingPendingIntent(this);
                            LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(pendingIntent);
                        }*/
                        //
                        updateLastLocation((float) location.getLatitude(), (float) location.getLongitude(), System.currentTimeMillis());
                        Utils.appendLog("LocationData", "I", "STOP at location Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                    } else {
                        Utils.appendLog(TAG, "I", "User NOT STILL now, keep check status after interval");
                        //seem user not move, so track again after quite long time
                        startAlarmLocationTrigger(Constants.INTERVAL_CHECK_STILL_IN_MS);
                    }
                })

                .addOnFailureListener(e -> {
                    Log.e(TAG, "Could not detect activity: " + e);
                    Utils.appendLog(TAG, "E", "Get Snapshot could not detect activity: " + e);
                });
    }

    private void processGeoFencing(Location location) {
        Thread task = new Thread(() -> {
            List<GeoFencingPlaceModel> geoPointsList = GeofencingDataManagement.Instance().getAllGeoPoints();
            if (geoPointsList != null && !geoPointsList.isEmpty()) {
                Utils.appendLog(TAG1, "I", "*** Check status all geo point number = " + geoPointsList.size());
                for (GeoFencingPlaceModel geoPoint : geoPointsList) {
                    checkGeoPointStatus(geoPoint, location);
                }
            }
        });
        task.start();

    }

    private void checkGeoPointStatus(GeoFencingPlaceModel geoPoint, Location location) {
        Utils.appendLog(TAG1, "I", "** Check status geo point " + geoPoint.getName());
        GeoFencingPlaceStatusModel geoPointStatusModel = GeofencingDataManagement.Instance().getGeoFencingPointOnProgress(geoPoint.getName());
        float distance = getMetersFromLatLong(location.getLatitude(), location.getLongitude(), geoPoint.getLat(), geoPoint.getLng());
        if (geoPointStatusModel != null) {
            Utils.appendLog(TAG1, "I", "* Existed this geo point " + geoPoint.getName() + " - d = " + distance + " - r = " + geoPoint.getRadius() + " - transtion " + (geoPointStatusModel.getTransition() == Constants.TRANSITION.ENTER ? "ENTER" : "EXIT"));
            //check if user exit
            if (distance > geoPoint.getRadius() && geoPointStatusModel.getTransition() == Constants.TRANSITION.ENTER) {
                geoPointStatusModel.setTransition(Constants.TRANSITION.EXIT);
                geoPointStatusModel.setLastActiveTime(System.currentTimeMillis());
                GeofencingDataManagement.Instance().addOrUpdateGeoOnProgress(geoPointStatusModel);
                Log.i(TAG1, "Geo Fencing Exit " + geoPoint.getName());
                Utils.appendLog(TAG1, "I", " - Geo Fencing Exit " + geoPoint.getName());
            } else if (distance <= geoPoint.getRadius() && geoPointStatusModel.getTransition() == Constants.TRANSITION.EXIT) {
                //check if user enter
                geoPointStatusModel.setTransition(Constants.TRANSITION.ENTER);
                geoPointStatusModel.setLastActiveTime(System.currentTimeMillis());
                GeofencingDataManagement.Instance().addOrUpdateGeoOnProgress(geoPointStatusModel);
                Log.i(TAG1, "Geo Fencing Enter " + geoPoint.getName());
                Utils.appendLog(TAG1, "I", "+ Geo Fencing Enter " + geoPoint.getName());
            }
        } else {
            Utils.appendLog(TAG1, "I", "Not existed this geo point " + geoPoint.getName() + " - d = " + distance + " - r = " + geoPoint.getRadius());
            //this is first moment user enter
            if (distance <= geoPoint.getRadius()) {
                geoPointStatusModel = new GeoFencingPlaceStatusModel(geoPoint);
                geoPointStatusModel.setTransition(Constants.TRANSITION.ENTER);
                geoPointStatusModel.setLastActiveTime(System.currentTimeMillis());
                GeofencingDataManagement.Instance().addOrUpdateGeoOnProgress(geoPointStatusModel);
                Log.i(TAG1, "+ Geo Fencing Enter " + geoPoint.getName());
                Utils.appendLog(TAG1, "I", "Geo Fencing Enter " + geoPoint.getName());
            }
        }

    }
}
