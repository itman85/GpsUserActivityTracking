package phannguyen.com.gpsuseractivitytracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import phannguyen.com.gpsuseractivitytracking.jobs.LocationUpdateWorker;
import phannguyen.com.gpsuseractivitytracking.signal.LocationTrackingIntervalWorker;
import phannguyen.com.gpsuseractivitytracking.signal.RegisterActivityFenceSignalWorker;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static phannguyen.com.gpsuseractivitytracking.Constants.REGISTER_ACTIVTY_WORK_TAG;
import static phannguyen.com.gpsuseractivitytracking.jobs.LocationUpdateWorker.KEY_RESULT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(v -> {
            //
            /*Intent serviceIntent = new Intent(MainActivity.this, LocationRequestUpdateService.class);
            serviceIntent.putExtra("action","START");
            startService(serviceIntent);*/
            //
            /*Intent serviceIntent1 = new Intent(MainActivity.this, ActivitiesTransitionRequestUpdateService.class);
            serviceIntent1.putExtra("action","START");
            startService(serviceIntent1);*/
            //
            //register();
            //
            //applyUpdateLocationWork(tag);
            //
            //LocationTrackingJobIntentService.enqueueWork(MainActivity.this,new Intent(MainActivity.this,LocationTrackingJobIntentService.class));
            //
            applyRegisterActivityFenceSignalWork(REGISTER_ACTIVTY_WORK_TAG);
            //
            //GeofencingDataManagement.Instance().addGeopointsList(Utils.createListGeoFencingPlaces());
            //
            //startOnetimeRequest(5,LOCATION_TRACKING_INTERVAL_WORK_TAG);
            Toast.makeText(MainActivity.this,"Register tracking user activity successfully, please close app now!",Toast.LENGTH_LONG).show();

        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(v -> {
            //
            /*Intent serviceIntent = new Intent(MainActivity.this,LocationRequestUpdateService.class);
            serviceIntent.putExtra("action","STOP");
            startService(serviceIntent);*/
            //
            /*Intent serviceIntent1 = new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class);
            serviceIntent1.putExtra("action","STOP");
            startService(serviceIntent1);*/
            //
            //stopGeofencingMonitoring();
            //
            //trackingWorkerByTag(tag);
            //
            //LocationTrackingJobIntentService.cancelLocationTriggerAlarm(MainActivity.this);
            cancelWorkerByTag(REGISTER_ACTIVTY_WORK_TAG);
            Toast.makeText(MainActivity.this,"Unregister tracking user activity",Toast.LENGTH_LONG).show();

        });

        Button awareBtn = findViewById(R.id.awarenessBtn);
        awareBtn.setOnClickListener(v -> {
            //
           //startActivity(new Intent(MainActivity.this,AwarenessActivity.class));
           // Crashlytics.getInstance().crash(); // Force a crash
            //List<String> message = new ArrayList<>();
            //message.add("123");
            //Toast.makeText(MainActivity.this,"This button disable processing" ,Toast.LENGTH_LONG).show();
            askIgnoreBatteryPermission();
        });

        List<String> permissionList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           /* this.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    100);*/
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            /*this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    101);*/
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] itemsArray = new String[permissionList.size()];
            itemsArray = permissionList.toArray(itemsArray);
            this.requestPermissions(
                    itemsArray,
                    101);

        }
        geofencingClient = LocationServices.getGeofencingClient(this);
        mWorkManager = WorkManager.getInstance();

    }

    private void askIgnoreBatteryPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
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
                new PeriodicWorkRequest.Builder(RegisterActivityFenceSignalWorker.class, 1440,
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
        geofencesList.add(createGeofence(10.740393, 106.700903,"Lotte",200));//lotte Q7
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
