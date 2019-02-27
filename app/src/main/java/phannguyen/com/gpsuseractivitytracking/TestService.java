package phannguyen.com.gpsuseractivitytracking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class TestService extends Service {
    private static final String TAG = "TestService" ;

    Thread myThread;
    boolean isStop;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate Tracking service");//call first time start service, if start again it will call onStartCommand directly
        myThread = new Thread(task);
        isStop = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand Tracking service");//call whenever start service call
        if(!myThread.isAlive())
            myThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy Tracking service");
        isStop = true;
        myThread.interrupt();
    }

    Runnable task = new Runnable() {
        @Override
        public void run() {
            int  count = 1;
            Log.i(TAG,"Start Tracking service");
            while (!isStop){
                Log.i(TAG,"Tracking..."+count);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
            }
            Log.i(TAG,"Stop Tracking service");
        }
    };
}
