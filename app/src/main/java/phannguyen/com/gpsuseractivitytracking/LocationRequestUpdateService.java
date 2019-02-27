package phannguyen.com.gpsuseractivitytracking;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static phannguyen.com.gpsuseractivitytracking.Constants.FASTEST_INTERVAL;
import static phannguyen.com.gpsuseractivitytracking.Constants.UPDATE_INTERVAL;

public class LocationRequestUpdateService extends IntentService {
    private static final String TAG = "LocationReqUpdateSv";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Context mContext;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public LocationRequestUpdateService() {
        super(TAG);
        Log.i(TAG, "Location Request Update Service Created");

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        mContext = getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }

    //this service will send request update location then stop itself
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Location Request Update Service Destroy");
    }

    private void removeLocationRequestUpdate(){
        PendingIntent pendingIntent = PendingIntentUtils.createLocationTrackingPendingIntent(this);
        mFusedLocationClient.removeLocationUpdates(pendingIntent);
    }
}
