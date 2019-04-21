package phannguyen.com.gpsuseractivitytracking;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;

import phannguyen.com.gpsuseractivitytracking.receivers.BatteryChangeReceiver;
import phannguyen.com.gpsuseractivitytracking.receivers.NetworkChangeReceiver;
import phannguyen.com.gpsuseractivitytracking.receivers.PackageChangeReceiver;

public class RegisterReceiverService extends Service {
    boolean isRegistered;
    private NetworkChangeReceiver mNetworkReceiver;
    //private NewPictureReceiver mPictureReceiver;
    private BatteryChangeReceiver mBatteryReceiver;
    private PackageChangeReceiver mPackageReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNetworkReceiver = new NetworkChangeReceiver();
        //mPictureReceiver = new NewPictureReceiver();
        mBatteryReceiver = new BatteryChangeReceiver();
        mPackageReceiver = new PackageChangeReceiver();
        isRegistered = false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRegistered) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //from android 7+ need to register for network, battery, package changed, in this way no need to declare receiver in android manifest
                registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            /*IntentFilter pictureIntent = new IntentFilter();
            pictureIntent.addAction("com.android.camera.NEW_PICTURE");
            pictureIntent.addAction("android.hardware.action.NEW_PICTURE");
            registerReceiver(mPictureReceiver, pictureIntent);*/

                IntentFilter packageIntent = new IntentFilter();
                packageIntent.addAction(Intent.ACTION_PACKAGE_ADDED);
                packageIntent.addAction(Intent.ACTION_PACKAGE_REMOVED);
                packageIntent.addAction(Intent.ACTION_PACKAGE_REPLACED);
                packageIntent.addDataScheme("package");
                registerReceiver(mPackageReceiver, packageIntent);
            }else{
                //under android 7 (Nougat) need to register for battery changed only
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
        //unregisterReceiver(mPictureReceiver);
        unregisterReceiver(mBatteryReceiver);
        unregisterReceiver(mPackageReceiver);
    }
}
