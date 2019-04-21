package phannguyen.com.gpsuseractivitytracking.receivers;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;

import phannguyen.com.gpsuseractivitytracking.Utils;

/**
 * 1. Day la service va van hoat dong du app killed
 * 2. Co tiep tuc listening take picture nhung lan sau nua ko? cai nay cai write log de test vai ngay
 * 3. Lam cho code cleaner, simpler, delete nhung gi ko can thiet
 * 4. Test voi android < N (6-) xem co nhan dc broadcast take new picture ko? de chia ra voi android <N va >N
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class PhotoJobService extends JobService {
    private static final String TAG = "PhotoJobService";//PhotoJobService.class.getSimpleName();

    //static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");
    public static final int JOBSERVICE_JOB_ID = 496; // any number but avoid conflicts
    private static JobInfo JOB_INFO;

    static final List<String> EXTERNAL_PATH_SEGMENTS
            = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();
    /**
     * Check photo job is registered or not
     * @param context
     * @return
     */
    private static boolean isRegistered(Context context) {
        Utils.appendLog(TAG,"I","Check isRegistered");
        JobScheduler js = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = js.getAllPendingJobs();
        if (jobs == null) {
            Log.d(TAG, "JobService not registered ");
            Utils.appendLog(TAG,"I","JobService not registered ");
            return false;
        }
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId() == JOBSERVICE_JOB_ID) {
                Utils.appendLog(TAG,"I","JobService is registered ");
                return true;
            }
        }
        Utils.appendLog(TAG,"I","JobService not registered ");
        Log.d(TAG, "JobService is not registered");
        return false;
    }

    public static void startJobService(Context context) {
        Log.d(TAG, "registerJob(): JobService init");
        Utils.appendLog(TAG,"I","JobService start now");
        if (!isRegistered(context)) {
            Log.d(TAG, "JobBuilder executes");
            Utils.appendLog(TAG,"I","JobService create photo job now");
            JobInfo.Builder builder = new JobInfo.Builder(JOBSERVICE_JOB_ID,
                    new ComponentName(context, PhotoJobService.class.getName()));
            // Look for specific changes to images in the provider.
            builder.addTriggerContentUri(
                    new JobInfo.TriggerContentUri(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
            // Also look for general reports of changes in the overall provider.
            //builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));

            // Get all media changes within a tenth of a second.
            builder.setTriggerContentUpdateDelay(1);
            builder.setTriggerContentMaxDelay(100);

            JOB_INFO = builder.build();
            Log.d(TAG, "JOB_INFO created ");
            Utils.appendLog(TAG,"I","Photo Job created");

            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int result = scheduler.schedule(JOB_INFO);
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "JobScheduler OK");
                Utils.appendLog(TAG,"I","JobScheduler OK");
            } else {
                Log.d(TAG, "JobScheduler fails " + result);
                Utils.appendLog(TAG,"I","JobScheduler fails " + result);
            }
        }
    }

    public static void stopJobService(Context context) {
        Log.d(TAG, "Cancel Photo Job");
        Utils.appendLog(TAG,"I","Cancel Photo Job");
        JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancel(JOBSERVICE_JOB_ID);
        isRegistered(context);//make sure job will not be registered
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() " + this.toString() + ", "
                + ((JOB_INFO == null) ? "null" : JOB_INFO.getClass().getSimpleName() + "@" + Integer.toHexString(java.lang.System.identityHashCode(JOB_INFO))));
        Utils.appendLog(TAG,"I","Photo Job onStart, maybe picture just taken");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (params.getJobId() == JOBSERVICE_JOB_ID) {
                if (params.getTriggeredContentAuthorities() != null) {
                    for (Uri uri : params.getTriggeredContentUris()) {
                        Log.d(TAG, "onStartJob() JobService Uri=" + uri.toString());
                        Utils.appendLog(TAG,"I","Something change at "+uri.toString());
                        List<String> path = uri.getPathSegments();
                        if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size()+1) {
                            // This is a specific file.
                            Utils.appendLog(TAG,"I","ah Ha,picture took with ID "+path.get(path.size()-1));
                        }
                    }
                }
            }
        }
        Utils.appendLog(TAG,"I","Photo Job finish and reschedule manually");
        this.jobFinished(params, false);  // false = do not reschedule

        // manual reschedule
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOBSERVICE_JOB_ID);
        startJobService(getApplicationContext());

        return true; // false =  no threads inside
    }

    //This method is called if the system has determined that you must stop execution of your job
    //even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "on Stop Job");
        Utils.appendLog(TAG,"I","Photo Job onStop");
        return false; // no restart from here
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.appendLog(TAG,"I","Photo Job Service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.appendLog(TAG,"I","Photo Job Service onStartCommand " + startId);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.appendLog(TAG,"I","Photo Job Service onDestroy");
    }
}
