package phannguyen.com.gpsuseractivitytracking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import phannguyen.com.gpsuseractivitytracking.awareness.FenceReceiver;
import phannguyen.com.gpsuseractivitytracking.jobs.LocationTriggerReceiver;


public class PendingIntentUtils {
    public static final String TRANSITIONS_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".TRANSITIONS_RECEIVER_ACTION";
    public static final String LOCATION_UPDATE_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".LOCATION_UPDATE_RECEIVER_ACTION";
    public static final String GEOFENCING_TRANSITION_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".GEOFENCING_TRANSITION_RECEIVER_ACTION";

    public static PendingIntent createTransitionTrackingPendingIntent(Context context){
        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);//TransitionReceiver will handle this action, register in manifest
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static PendingIntent createLocationTrackingPendingIntent(Context context){
        Intent intent = new Intent(LOCATION_UPDATE_RECEIVER_ACTION);//LocationTrackingReceiver will handle this action, register in manifest
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static PendingIntent createGeofencingTransitionPendingIntent(Context context){
        Intent intent = new Intent(GEOFENCING_TRANSITION_RECEIVER_ACTION);//LocationTrackingReceiver will handle this action, register in manifest
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static PendingIntent getLocationTriggerPendingIntent(Context context){
        Intent intent = new Intent(context, LocationTriggerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static PendingIntent getFenceAwareNessPendingIntent(Context context){
        Intent intent = new Intent(context, FenceReceiver.class);
        intent.setAction(FenceReceiver.FENCE_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

}
