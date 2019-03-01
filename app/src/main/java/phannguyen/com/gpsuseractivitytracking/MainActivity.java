package phannguyen.com.gpsuseractivitytracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

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
                //startService(new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class));
                //
                /*Intent serviceIntent = new Intent(MainActivity.this,GeofencingRequestService.class);
                serviceIntent.putExtra("action","START");
                startService(serviceIntent);*/
                register();

            }
        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopService(new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class));
                //
                /*Intent serviceIntent = new Intent(MainActivity.this,GeofencingRequestService.class);
                serviceIntent.putExtra("action","STOP");
                startService(serviceIntent);*/
                stopGeofencingMonitoring();

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
    }

    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @SuppressLint("MissingPermission")
    private void register(){
        geofencingClient = LocationServices.getGeofencingClient(this);
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

    private PendingIntent getGeofencePendingIntent() {
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
    }

    //Stopping geofence monitoring when it is no longer needed or desired can help save battery power and CPU cycles on the device
    private void stopGeofencingMonitoring(){
        geofencingClient.removeGeofences(getGeofencePendingIntent())
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
        geofencesList.add(createGeofence(10.761123, 106.700378,"3",500));//cau ong lanh vs hoang dieu
        return geofencesList;
    }

    private Geofence createGeofence(double lat, double lng, String key, int radius) {
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
