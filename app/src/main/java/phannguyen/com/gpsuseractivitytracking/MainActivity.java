package phannguyen.com.gpsuseractivitytracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import phannguyen.com.gpsuseractivitytracking.awareness.AwarenessActivity;
import phannguyen.com.gpsuseractivitytracking.jobs.LocationUpdateWorker;
import phannguyen.com.gpsuseractivitytracking.signal.ActivitiesTransitionRequestUpdateService;
import phannguyen.com.gpsuseractivitytracking.signal.LocationTrackingIntervalWorker;
import phannguyen.com.gpsuseractivitytracking.signal.RegisterActivityFenceSignalWorker;

import static phannguyen.com.gpsuseractivitytracking.Constants.LOCATION_TRACKING_INTERVAL_WORK_TAG;
import static phannguyen.com.gpsuseractivitytracking.Constants.REGISTER_ACTIVTY_WORK_TAG;
import static phannguyen.com.gpsuseractivitytracking.jobs.LocationUpdateWorker.KEY_RESULT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                Intent serviceIntent = new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class);
                serviceIntent.putExtra("action","START");
                startService(serviceIntent);
                //
                //register();
                //
                //applyUpdateLocationWork(tag);
                //
                //LocationTrackingJobIntentService.enqueueWork(MainActivity.this,new Intent(MainActivity.this,LocationTrackingJobIntentService.class));
                //
                applyRegisterActivityFenceSignalWork(REGISTER_ACTIVTY_WORK_TAG);
                //
                //startOnetimeRequest(5,LOCATION_TRACKING_INTERVAL_WORK_TAG);

            }
        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                Intent serviceIntent = new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class);
                serviceIntent.putExtra("action","STOP");
                startService(serviceIntent);
                //
                //stopGeofencingMonitoring();
                //
                //trackingWorkerByTag(tag);
                //
                //LocationTrackingJobIntentService.cancelLocationTriggerAlarm(MainActivity.this);
                cancelWorkerByTag(REGISTER_ACTIVTY_WORK_TAG);

            }
        });

        Button awareBtn = findViewById(R.id.awarenessBtn);
        awareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
               //startActivity(new Intent(MainActivity.this,AwarenessActivity.class));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    101);
        }
        //geofencingClient = LocationServices.getGeofencingClient(this);
        mWorkManager = WorkManager.getInstance();

    }

    ///////////////////////////////////////////////////////////////
    private WorkManager mWorkManager;
    private String tag = "location";
    //repeat worker after interval time
    private void applyUpdateLocationWork(String tag){
       /* OneTimeWorkRequest locationWork =
                new OneTimeWorkRequest.Builder(LocationUpdateWorker.class)
                        .build();*/

       //PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS is 15mins
        PeriodicWorkRequest.Builder locationUpdateBuilder =
                new PeriodicWorkRequest.Builder(LocationUpdateWorker.class, 30,
                        TimeUnit.MINUTES);
        PeriodicWorkRequest locationWork = locationUpdateBuilder.addTag(tag)
                .build();
        WorkManager.getInstance().enqueue(locationWork);
        Log.i(TAG,"Location worker id "+ locationWork.getId());

    }

    private void startOnetimeRequest(int delayInSecond,String tag){
        OneTimeWorkRequest locationIntervalWork=
                new OneTimeWorkRequest.Builder(LocationTrackingIntervalWorker.class)
                        .setInitialDelay(delayInSecond,TimeUnit.SECONDS)
                        .addTag(tag)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();
        WorkManager.getInstance().enqueue(locationIntervalWork);
    }

    private void applyRegisterActivityFenceSignalWork(String tag){
        PeriodicWorkRequest.Builder registerActivityWorkBuilder =
                new PeriodicWorkRequest.Builder(RegisterActivityFenceSignalWorker.class, 30,
                        TimeUnit.MINUTES);
        PeriodicWorkRequest registerWork = registerActivityWorkBuilder.addTag(tag)
                .build();
        WorkManager.getInstance().enqueue(registerWork);
        Log.i(TAG,"Register activity fence signal worker id "+ registerWork.getId());

    }

    private void trackingWorkerByTag(String tag){
        LiveData<List<WorkInfo>> workInfos = mWorkManager.getWorkInfosByTagLiveData(tag);
        workInfos.observe(this, listOfWorkInfo -> {

            // If there are no matching work info, do nothing
            if (listOfWorkInfo == null || listOfWorkInfo.isEmpty()) {
                Log.i(TAG,"No work info");
                return;
            }

            // We only care about the one output status.
            WorkInfo workInfo = listOfWorkInfo.get(0);
            boolean finished = workInfo.getState().isFinished();

            if (!finished) {
                Log.i(TAG,"work info not finish");
            } else {
                int myResult = workInfo.getOutputData().getInt(KEY_RESULT, -1);
                Log.i(TAG,"work info finish result "+ myResult);

            }
        });
    }

    private void cancelWorkerByTag(String tag){
        mWorkManager.cancelAllWorkByTag(tag);
    }
    ///////////////////////NOT WORK ANDROID 8/////////////////////////
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @SuppressLint("MissingPermission")
    private void register(){
        geofencingClient.addGeofences(getGeofencingRequest(), PendingIntentUtils.createGeofencingTransitionPendingIntent(this))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Geo fencing request register successfully");
                            //stop service
                            //stopSelf();
                        } else {
                            Log.e(TAG, "Geo fencing request register fail");
                        }
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(createGeofenceObjectsList());
        return builder.build();
    }

   /* private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }*/

    //Stopping geofence monitoring when it is no longer needed or desired can help save battery power and CPU cycles on the device
    private void stopGeofencingMonitoring(){
        geofencingClient.removeGeofences(PendingIntentUtils.createGeofencingTransitionPendingIntent(this))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG,"Remove Geofencing Request successful");
                        }else{
                            Log.i(TAG,"Remove Geofencing Request Fail");
                        }
                    }
                });
    }

    private List<Geofence> createGeofenceObjectsList(){
        List<Geofence> geofencesList = new ArrayList<>();
        geofencesList.add(createGeofence(10.775020, 106.686813,"1",200));//cmt8 vs nguyen dinh chieu
        geofencesList.add(createGeofence(10.771563, 106.693179,"2",300));//cmt8 phu dong
        //geofencesList.add(createGeofence(10.761123, 106.700378,"3",500));//cau ong lanh vs hoang dieu
        geofencesList.add(createGeofence(10.740393, 106.700903,"Lotte",200));//cau ong lanh vs hoang dieu
        return geofencesList;
    }

    private Geofence createGeofence(double lat, double lng, String key, float radius) {
        return new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(lat, lng, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                //.setLoiteringDelay(10000)
                .build();
    }

}
