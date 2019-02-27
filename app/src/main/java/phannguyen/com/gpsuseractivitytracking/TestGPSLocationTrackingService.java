package phannguyen.com.gpsuseractivitytracking;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static phannguyen.com.gpsuseractivitytracking.Constants.FASTEST_INTERVAL;
import static phannguyen.com.gpsuseractivitytracking.Constants.UPDATE_INTERVAL;

/**
 * https://medium.com/@droidbyme/get-current-location-using-fusedlocationproviderclient-in-android-cb7ebf5ab88e
 */
public class TestGPSLocationTrackingService extends Service {

    private static final String TAG = "GPSLocationTrackingSv";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TrackingGPSService", "Service Created");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onHandleIntent");
        if(intent.hasExtra("action") && "START".equals(intent.getStringExtra("action"))) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No location permission granted");
            } else {
                Log.i(TAG, "Request location update");
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, PendingIntentUtils.createLocationTrackingPendingIntent(this));
            }
        }else if(intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))){
            Log.e(TAG, "*** Remove Request location update");
            removeLocationRequestUpdate();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service Destroyed");
    }

    private void removeLocationRequestUpdate(){
        PendingIntent pendingIntent = PendingIntentUtils.createLocationTrackingPendingIntent(this);
        mFusedLocationClient.removeLocationUpdates(pendingIntent);
    }


}
