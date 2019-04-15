package phannguyen.com.gpsuseractivitytracking.android7.locationtracking;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.core.CoreTrackingJobService;
import phannguyen.com.gpsuseractivitytracking.core.storage.SharePref;

import static phannguyen.com.gpsuseractivitytracking.Constants.FASTEST_INTERVAL;
import static phannguyen.com.gpsuseractivitytracking.Constants.UPDATE_INTERVAL;

/**
 * this work stable in im app
 */
public class LocationRequestUpdateService1 extends Service {
    private static final String TAG = "LocationReqUpdateSv1";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Context mContext;
    //private MyLocationUpdateReceiver myLocationUpdateReceiver;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    private boolean isStartTracking;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Location Request Update Service Created");
        Utils.appendLog(TAG,"I","Location Request Update Service Created");
       // myLocationUpdateReceiver = new MyLocationUpdateReceiver();
        mContext = getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        //LocalBroadcastManager.getInstance(this).registerReceiver(myLocationUpdateReceiver, "abc");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        isStartTracking = false;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        Utils.appendLog(TAG,"I","Service onStartCommand");
        boolean isOnTracking = false;
        //for case this service is killed by OS, it will restart with null intent, then request location update again
        if(intent==null){
            isOnTracking = SharePref.getLocationRequestUpdateStatus(this);
            if(isOnTracking){
                isStartTracking  = false;//restart request update location
                Utils.appendLog(TAG,"I","Service restart Null Intent, restart update location");
            }else{
                //no location request update live now, so kill this service
                Utils.appendLog(TAG,"I","Service restart Null Intent, Stop now");
                stopSelf();//https://stackoverflow.com/questions/8279199/can-i-call-stopself-in-service-onstartcommand
            }
        }
        if(isOnTracking || (intent!=null && intent.hasExtra("action") && "START".equals(intent.getStringExtra("action")))) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No location permission granted");
                Utils.appendLog(TAG,"E","No location permission granted");
            } else {
                Log.i(TAG, "Request location update");
                Utils.appendLog(TAG,"I","Request location update now");
                if(!isStartTracking) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    isStartTracking = true;
                    SharePref.setLocationRequestUpdateStatus(this, true);
                }
            }
        }else if(intent!=null && intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))){
            Log.e(TAG, "*** Remove Request location update");
            Utils.appendLog(TAG,"I","*** Remove Request location update");
            removeLocationRequestUpdate();
            isStartTracking = false;
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Location Request Update Service Destroy");
        Utils.appendLog(TAG,"I","**** Location Request Update Service Destroy");
        SharePref.setLocationRequestUpdateStatus(this, false);
        mServiceHandler.removeCallbacksAndMessages(null);
       // LocalBroadcastManager.getInstance(this).unregisterReceiver(myLocationUpdateReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void removeLocationRequestUpdate(){
        //PendingIntent pendingIntent = PendingIntentUtils.createLocationTrackingPendingIntent(this);
        //mFusedLocationClient.removeLocationUpdates(pendingIntent);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void onNewLocation(Location location) {
        if (location != null) {
            Intent intent = new Intent("ACTION_BROADCAST");
            intent.putExtra("com.google.android.gms.location.EXTRA_LOCATION_RESULT", location);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            CoreTrackingJobService.enqueueWork(this,intent);
            // use the Location
            Log.i(TAG,"***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            Utils.appendLog(TAG,"I","***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
        }

    }

    private class MyLocationUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationResult.hasResult(intent)) {
                CoreTrackingJobService.enqueueWork(context,intent);
                LocationResult locationResult = LocationResult.extractResult(intent);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // use the Location
                    Log.i(TAG,"***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                    Utils.appendLog("MyLocationUpdateReceiver","I","***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                }
            }
        }
    }
}
