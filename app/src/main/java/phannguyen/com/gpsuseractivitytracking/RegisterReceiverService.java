package phannguyen.com.gpsuseractivitytracking;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;

import phannguyen.com.gpsuseractivitytracking.receivers.NetworkChangeReceiver;

public class RegisterReceiverService extends Service {
    private NetworkChangeReceiver mNetworkReceiver;
    boolean isRegistered;
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNetworkReceiver = new NetworkChangeReceiver();
        isRegistered = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }
            isRegistered = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
    }
}
