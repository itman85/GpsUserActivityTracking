package phannguyen.com.gpsuseractivitytracking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
public class BatteryChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryChangeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive battery changed");

    }
}
