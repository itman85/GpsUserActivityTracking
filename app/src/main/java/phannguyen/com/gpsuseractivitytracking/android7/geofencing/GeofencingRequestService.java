package phannguyen.com.gpsuseractivitytracking.android7.geofencing;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import phannguyen.com.gpsuseractivitytracking.PendingIntentUtils;
import phannguyen.com.gpsuseractivitytracking.Utils;

import static phannguyen.com.gpsuseractivitytracking.Constants.GEO_ID_PLIT_CHAR;
import static phannguyen.com.gpsuseractivitytracking.Constants.KEY_ADD_NEW_LIST;
import static phannguyen.com.gpsuseractivitytracking.Constants.KEY_REMOVE_LIST;

public class GeofencingRequestService extends Service {
    private static final String TAG = "GeoFencingReqSv";
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Geo fencing request service created");
        Utils.appendLog(TAG,"I","Geo fencing request service created");
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra("action") && "START".equals(intent.getStringExtra("action"))) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No permission for geo fencing");
                Utils.appendLog(TAG,"I","No permission for geo fencing");
                stopSelf();
            } else {
                //get add/remove from intent
                List<String> addNewGeoSetting = new ArrayList<>();
                List<String> removeGeoSetting = new ArrayList<>();
                extractGeoDataFromIntent(intent, addNewGeoSetting, removeGeoSetting);
                //1. call remove if there are list of remove points
                if (!removeGeoSetting.isEmpty()) {
                    Log.i(TAG, "Geo fencing request remove points now");
                    Utils.appendLog(TAG,"I","Geo fencing request remove points now");
                    removeGeofencingPoints(removeGeoSetting);
                }
                if (!addNewGeoSetting.isEmpty()) {
                    Log.i(TAG, "Geo fencing request add points now");
                    Utils.appendLog(TAG,"I","Geo fencing request add points now");
                    //2. call add geo fencing, will update if this request existing
                    geofencingClient.addGeofences(getGeofencingRequest(addNewGeoSetting), PendingIntentUtils.createGeofencingTransitionPendingIntent(this))
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Geo fencing request register successfully");
                                    Utils.appendLog(TAG,"I","Geo fencing request register successfully");
                                    //stop service
                                    stopSelf();
                                } else {
                                    Log.e(TAG, "Geo fencing request register fail");
                                    Utils.appendLog(TAG,"I","Geo fencing request register fail");
                                    stopSelf();
                                }
                            });
                }
            }
        }else if(intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))) {
            Log.e(TAG, "***Geo fencing request remove...");
            Utils.appendLog(TAG,"I","***Geo fencing request remove...");
            stopGeofencingMonitoring();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Geo fencing request service destroy");
    }

    public void extractGeoDataFromIntent(Intent intent, List<String> addNewGeoSetting, List<String> removeGeoSetting) {
        addNewGeoSetting.add("ok");//test data
        if (intent.hasExtra(KEY_REMOVE_LIST)) {
            String tempRemove = intent.getStringExtra(KEY_REMOVE_LIST);
            String[] tempArray = tempRemove.split(GEO_ID_PLIT_CHAR);
            for (String value:tempArray){
                removeGeoSetting.add(value);
            }
        }
        if (intent.hasExtra(KEY_ADD_NEW_LIST)) {
            String tempAdd = intent.getStringExtra(KEY_ADD_NEW_LIST);
            String[] tempArray = tempAdd.split(GEO_ID_PLIT_CHAR);
            for (String value:tempArray){
                addNewGeoSetting.add(value);
            }
        }
    }
    private GeofencingRequest getGeofencingRequest(List<String> addedIdGeoPoints) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(createGeofenceObjectsList(addedIdGeoPoints));
        return builder.build();
    }

    /*private PendingIntent getGeofencePendingIntent() {
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
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG,"Remove Geofencing Request successful");
                        Utils.appendLog(TAG,"I","Remove Geofencing Request successful");
                    }else{
                        Log.i(TAG,"Remove Geofencing Request Fail");
                        Utils.appendLog(TAG,"I","Remove Geofencing Request Fail");
                    }
                    stopSelf();
                });
    }

    //remove geo points which no longer need to monitor
    private void removeGeofencingPoints(List<String> pointsId){
        geofencingClient.removeGeofences(pointsId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG,"Location alters have been removed");
                Utils.appendLog(TAG,"I","Location alters have been removed");
            }else{
                Log.i(TAG,"Location alters could not be removed");
                Utils.appendLog(TAG,"I","Location alters could not be removed");
            }
        });
    }


    private List<Geofence> createGeofenceObjectsList(List<String> addedIdGeoPoints){
        List<Geofence> geofencesList = new ArrayList<>();
        geofencesList.add(createGeofence(10.775020, 106.686813,"1",200));//cmt8 vs nguyen dinh chieu
        geofencesList.add(createGeofence(10.771563, 106.693179,"2",300));//cmt8 phu dong
        geofencesList.add(createGeofence(10.740370, 106.700955,"3",100));//lotteq7
        geofencesList.add(createGeofence(10.740375, 106.700972,"LotteQ7",200));//Lotte Q7
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
