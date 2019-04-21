package phannguyen.com.gpsuseractivitytracking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import phannguyen.com.gpsuseractivitytracking.Utils;

//https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
public class BatteryChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryChangeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive battery changed");
        int level = intent.getIntExtra( BatteryManager.EXTRA_LEVEL , - 1 );
        int scale = intent.getIntExtra( BatteryManager.EXTRA_SCALE , - 1 );
        float batteryPct = level / ( float ) scale ;
        Utils.appendLog(TAG,"I","onReceive battery changed "+batteryPct);
    }
}
