package phannguyen.com.gpsuseractivitytracking.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePref {
    private static String PREF_NAME = "GPSTestSharePref";

    public static void setGpsTrackingStatus(Context context, boolean status){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("gpstrackingstatus", status);
        editor.apply();
    }

    public static boolean getGpsTrackingStatus(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("gpstrackingstatus",false);
    }

    public static void setLastLngLocation(Context context, float lng){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("last_lng", lng);
        editor.apply();
    }

    public static void setLastLatLocation(Context context, float lat){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("last_lat", lat);
        editor.apply();
    }

    public static float getLastLngLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat("last_lng",0);
    }

    public static float getLastLatLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat("last_lat",0);
    }

    public static void setLastMomentGPSNotChange(Context context, long time){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("gpsmomentnotchange",time);
        editor.apply();
    }

    public static long getLastMomentGPSNotChange(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong("gpsmomentnotchange",0);
    }

    public static void setLastSpeedInterval(Context context, int interval){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastSpeedInterval",interval);
        editor.apply();
    }

    public static int getLastSpeedInterval(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("lastSpeedInterval",0);
    }


}
