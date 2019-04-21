package phannguyen.com.gpsuseractivitytracking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import phannguyen.com.gpsuseractivitytracking.Utils;

public class PackageChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageChangeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive package changed");
        Utils.appendLog(TAG,"I","onReceive package changed " + intent.getAction());

    }
}
