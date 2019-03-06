package phannguyen.com.gpsuseractivitytracking.core;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import phannguyen.com.gpsuseractivitytracking.Constants;
import phannguyen.com.gpsuseractivitytracking.PendingIntentUtils;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.core.storage.SharePref;
import phannguyen.com.gpsuseractivitytracking.jobs.LocationTrackingJobIntentService;

public class CoreTrackingJobService extends JobIntentService {
    private static final int JOB_ID = 1009;
    private static final String TAG = "CoreTrackingJobSv";

    private static final int DELAY_IN_MS = 30*1000;//20S
    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, CoreTrackingJobService.class, JOB_ID, intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG,"Location tracking job service onHandleWork");
        Utils.appendLog(TAG,"I","Location tracking job service onHandleWork");
        boolean isStartTrackingSignal = handleIntentSignal(intent);
        boolean statusTracking = SharePref.getGpsTrackingStatus(this);
        if(isStartTrackingSignal && statusTracking)
            cancelLocationTriggerAlarm(this);//cancel for restart
        if(!statusTracking)
            SharePref.setGpsTrackingStatus(this,true);
        //
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if(location!=null){
                processLocationData(location);
            }else {
                startAlarmLocationTrigger(DELAY_IN_MS);
            }
        }).addOnFailureListener(e -> {
            if(e!=null){
                Log.e(TAG,e.getMessage());
                Utils.appendLog(TAG,"I","Tracking location Error "+ e.getMessage());
            }
            startAlarmLocationTrigger(DELAY_IN_MS);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        Utils.appendLog(TAG,"I","onDestroy");
    }

    private void startAlarmLocationTrigger(int delayInMs){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
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
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //this make alarm on time when device goes to doze mode, this will done_item_menu clock icon on device top bar
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(triggerMoment, pendingIntent), pendingIntent);
        }*/
    }

    public static void cancelLocationTriggerAlarm(Context context) {
        Log.i(TAG,"Cancel Location Trigger Alarm");
        Utils.appendLog(TAG,"I","Cancel Location Trigger Alarm");
        SharePref.setGpsTrackingStatus(context,false);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntentUtils.getLocationTriggerAlarmPendingIntent(context));
    }

    /**
     *
     * @param intent
     * @return true force get location tracking, false: ignore
     */
    private boolean handleIntentSignal(Intent intent){
        if(intent!=null && intent.hasExtra(Constants.SIGNAL_KEY)){
            Constants.SIGNAL fromSignal =  Constants.SIGNAL.valueOf(intent.getStringExtra(Constants.SIGNAL_KEY));
            if(fromSignal == Constants.SIGNAL.NOT_STILL) {
                Log.i(TAG,"Start Tracking From NOT STILL Signal");
                Utils.appendLog(TAG,"I","Start Tracking From NOT STILL Signal");
                return true;
            }
            return false;
        }
        return false;
    }

    private void processLocationData(Location location){
        boolean isMove = checkUserLocationData(location);
        if(isMove){
            Log.i(TAG,"***User moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            Utils.appendLog(TAG,"I","***User moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            startAlarmLocationTrigger(DELAY_IN_MS);
        }else{
            long lastStayMoment = SharePref.getLastMomentGPSNotChange(this);
            //check if user dont move for long time => user STILL
            if(System.currentTimeMillis() - lastStayMoment >= Constants.TIMEOUT_STAY_LOCATION){
                Log.i(TAG,"***User Stay for long time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                Utils.appendLog(TAG,"I","***User stay for long time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                getSnapshotCurrentActivity(this,location);
            }else{
                Log.i(TAG,"***User Stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                Utils.appendLog(TAG,"I","***User stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                //keep tracking location to see if user move or not
                startAlarmLocationTrigger(DELAY_IN_MS);
            }
        }
    }

    /**
     *
     * @param location
     * @return true if user move, false if not move
     */
    private boolean checkUserLocationData(Location location){
        float lastLng = SharePref.getLastLngLocation(this);
        float lastLat = SharePref.getLastLatLocation(this);
        if(lastLat==0||lastLng==0){
            updateLastLocation((float) location.getLatitude(),(float) location.getLongitude(),System.currentTimeMillis());
            return true;
        }else{
            float distance = getMetersFromLatLong(lastLat,lastLng, (float) location.getLatitude(), (float) location.getLongitude());
            //seem not move
            if(distance <= Constants.STAY_DISTANCE_IN_MET){
                return false;
            }else{
                //user move
                updateLastLocation((float) location.getLatitude(),(float) location.getLongitude(),System.currentTimeMillis());
                return true;
            }
        }
    }

    private float getMetersFromLatLong(float lat1, float lng1, float lat2, float lng2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }

    private void updateLastLocation(float lat,float lng,long time){
        SharePref.setLastLatLocation(this, lat);
        SharePref.setLastLngLocation(this, lng);
        SharePref.setLastMomentGPSNotChange(this, time);
    }

    private void getSnapshotCurrentActivity(Context context, Location location){
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
                    Log.i(TAG,"Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    Utils.appendLog(TAG,"I","Get Snapshot Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    //check if STILL now, so cancel tracking
                    if(probableActivity.getType() == DetectedActivity.STILL && confidence >= 70){
                        //user still, so cancel tracking location alarm
                        Utils.appendLog(TAG,"I","User STILL now, cancel tracking location");
                        cancelLocationTriggerAlarm(context);
                        updateLastLocation((float) location.getLatitude(),(float) location.getLongitude(),System.currentTimeMillis());
                    }
                })

                .addOnFailureListener(e -> {
                    Log.e(TAG, "Could not detect activity: " + e);
                    Utils.appendLog(TAG,"E","Get Snapshot could not detect activity: " + e);
                });
    }
}
