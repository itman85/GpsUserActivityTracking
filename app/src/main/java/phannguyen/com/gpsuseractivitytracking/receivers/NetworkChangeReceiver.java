package phannguyen.com.gpsuseractivitytracking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import phannguyen.com.gpsuseractivitytracking.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive network changed");
        //Utils.appendLog(TAG,"I","onReceive network changed");
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Utils.appendLog(TAG,"I","onReceive network changed "+ (isConnected?"connected":"disconnected"));
    }
}
