package phannguyen.com.gpsuseractivitytracking.signal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import phannguyen.com.gpsuseractivitytracking.Utils;
import phannguyen.com.gpsuseractivitytracking.core.CoreTrackingJobService;

public class LocationTrackingIntervalWorker extends Worker {

    private static final String TAG = "LocationIntervalWorker";
    public static final String KEY_RESULT = "locationResult";

    public LocationTrackingIntervalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG,"Location Tracking Interval Worker Trigger");
        Utils.appendLog(TAG,"I","LocationIntervalWorker For Recheck Location & User Activity Status");
        CoreTrackingJobService.enqueueWork(getApplicationContext(),new Intent(getApplicationContext(),CoreTrackingJobService.class));
        Data output = new Data.Builder()
                .putBoolean(KEY_RESULT, true)
                .build();
        return Result.success(output);
    }
}
