package phannguyen.com.gpsuseractivitytracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * For background receive geofencing record data, using broadcast receiver instead of intentservice (for foreground)
 * http://www.hiren.dev/2015/01/android-geofence-stop-getting.html
 */
public class GeofenceTransitionReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofencingTransitionRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "geo fencing event error code "+ geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for(Geofence geofence:triggeringGeofences){
                Log.i(TAG,"GeoFencing trigger at point id "+geofence.getRequestId()+ " type transition "+ geofenceTransition);
            }
        } else {
            // Log the error.
            Log.e(TAG, "Geo fencing trigger type transition invalid");
        }
    }
}
