package phannguyen.com.gpsuseractivitytracking;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;

import phannguyen.com.gpsuseractivitytracking.receivers.BatteryChangeReceiver;
import phannguyen.com.gpsuseractivitytracking.receivers.NetworkChangeReceiver;
import phannguyen.com.gpsuseractivitytracking.receivers.NewPictureReceiver;

public class RegisterReceiverService extends Service {
    private NetworkChangeReceiver mNetworkReceiver;
    private NewPictureReceiver mPictureReceiver;
    private BatteryChangeReceiver mBatteryReceiver;
    boolean isRegistered;
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNetworkReceiver = new NetworkChangeReceiver();
        mPictureReceiver = new NewPictureReceiver();
        mBatteryReceiver = new BatteryChangeReceiver();
        isRegistered = false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                IntentFilter pictureIntent = new IntentFilter();
                pictureIntent.addAction("com.android.camera.NEW_PICTURE");
                pictureIntent.addAction("android.hardware.action.NEW_PICTURE");
                registerReceiver(mPictureReceiver, pictureIntent);
                registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
            isRegistered = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
        unregisterReceiver(mPictureReceiver);
    }
}
