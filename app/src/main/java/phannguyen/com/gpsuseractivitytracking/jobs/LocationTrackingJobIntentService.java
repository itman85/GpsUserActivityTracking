package phannguyen.com.gpsuseractivitytracking.jobs;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import phannguyen.com.gpsuseractivitytracking.PendingIntentUtils;
import phannguyen.com.gpsuseractivitytracking.Utils;

public class LocationTrackingJobIntentService extends JobIntentService {
    private static final int JOB_ID = 1008;

    private static final String TAG = "LocationTrackingIS";

    private static final int DELAY_IN_MS = 20*1000;//20S
    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, LocationTrackingJobIntentService.class, JOB_ID, intent);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG,"Location tracking job service onHandleWork");
        Utils.appendLog(TAG,"I","Location tracking job service onHandleWork");
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if(location!=null){
                Log.i(TAG,"***Tracking location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                Utils.appendLog(TAG,"I","***Tracking location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            }
            startAlarmLocationTrigger(DELAY_IN_MS);
        }).addOnFailureListener(e -> {
            if(e!=null){
                Log.e(TAG,e.getMessage());
                Utils.appendLog(TAG,"I","Tracking location Error "+ e.getMessage());
            }
            startAlarmLocationTrigger(DELAY_IN_MS);
        });
    }

    private void startAlarmLocationTrigger(int delayInMs){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntentUtils.getLocationTriggerPendingIntent(this);
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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntentUtils.getLocationTriggerPendingIntent(context));
    }

}
