package phannguyen.com.gpsuseractivitytracking.signal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import phannguyen.com.gpsuseractivitytracking.PendingIntentUtils;
import phannguyen.com.gpsuseractivitytracking.Utils;

import static phannguyen.com.gpsuseractivitytracking.Constants.ACTIVITY_FENCE_KEY;

public class RegisterActivityFenceSignalWorker extends Worker {
    private static final String TAG = "RegisterActivityWorker";
    public static final String KEY_RESULT = "locationResult";
    private int count;
    public RegisterActivityFenceSignalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        count = 1;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG,"Work time "+ count + " at " +System.currentTimeMillis());
        setupFences();
        Data output = new Data.Builder()
                .putInt(KEY_RESULT, count)
                .build();
        return Result.success(output);
        //return null;
    }

    private void setupFences() {
        // DetectedActivityFence will fire when it detects the user performing the specified
        // activity.  In this case it's walking.
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);

        AwarenessFence stayFence = DetectedActivityFence.during(DetectedActivityFence.STILL);

        // There are lots of cases where it's handy for the device to know if headphones have been
        // plugged in or unplugged.  For instance, if a music app detected your headphones fell out
        // when you were in a library, it'd be pretty considerate of the app to pause itself before
        // the user got in trouble.
        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        // Combines multiple fences into a compound fence.  While the first two fences trigger
        // individually, this fence will only trigger its callback when all of its member fences
        // hit a true state.
        AwarenessFence walkingWithHeadphones = AwarenessFence.and(walkingFence, headphoneFence);

        // We can even nest compound fences.  Using both "and" and "or" compound fences, this
        // compound fence will determine when the user has headphones in and is engaging in at least
        // one form of exercise.
        // The below breaks down to "(headphones plugged in) AND (walking OR running OR bicycling)"
        AwarenessFence exercisingWithHeadphonesFence = AwarenessFence.and(
                headphoneFence,
                AwarenessFence.or(
                        walkingFence,
                        DetectedActivityFence.during(DetectedActivityFence.RUNNING),
                        DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE)));


        // Now that we have an interesting, complex condition, register the fence to receive
        // callbacks.
        Log.i(TAG, "Activity Fence now registered again.");
        Utils.appendLog(TAG,"I","Activity Fence now registered again.");
        // Register the fence to receive callbacks.
        Awareness.getFenceClient(this.getApplicationContext()).updateFences(new FenceUpdateRequest.Builder()
                .addFence(ACTIVITY_FENCE_KEY, stayFence,PendingIntentUtils.getFenceAwareNessPendingIntent(getApplicationContext()))
                .build())
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "Activity Fence was successfully registered again.");
                    Utils.appendLog(TAG,"I","Activity Fence was successfully registered again.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activity Fence could not be registered again: " + e);
                    Utils.appendLog(TAG,"E","Activity Fence could not be registered again: " + e);
                });
    }
}
