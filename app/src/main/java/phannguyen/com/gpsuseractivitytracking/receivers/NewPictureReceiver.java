package phannguyen.com.gpsuseractivitytracking.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import phannguyen.com.gpsuseractivitytracking.Utils;

public class NewPictureReceiver extends BroadcastReceiver {
    private static final String TAG = "NewPictureReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"onReceive new picture");
        Utils.appendLog(TAG,"I","onReceive new picture");
    }
}
