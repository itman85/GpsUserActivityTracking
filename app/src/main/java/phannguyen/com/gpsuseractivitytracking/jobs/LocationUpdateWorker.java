package phannguyen.com.gpsuseractivitytracking.jobs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LocationUpdateWorker extends Worker {

    private static final String TAG = "LocationUpdateWorker";
    public static final String KEY_RESULT = "locationResult";
    private int count;
    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        count = 1;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG,"Work time "+ count + " at " +System.currentTimeMillis());
        Data output = new Data.Builder()
                .putInt(KEY_RESULT, count)
                .build();
        return Result.success(output);
        //return null;
    }
}
