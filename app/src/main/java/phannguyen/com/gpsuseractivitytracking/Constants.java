package phannguyen.com.gpsuseractivitytracking;

public class Constants {
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000;

    public static final int CONFIDENCE = 70;
    public static long DWELL_TIME_IN_MS = 5 * 60 * 1000;  /* 5 MINS */
    public static long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    public static long FASTEST_INTERVAL = 2000; /* 2 sec */
    public static final int STAY_DISTANCE_IN_MET = 30;//in this distance, device consider as not move
    public static long TIMEOUT_STAY_LOCATION = 5*60*1000;  /* 5 MINS USER stay a location in 5 mins consider as STILL*/
    public static final int INTERVAL_VERY_SLOW_MOVE_IN_MS = 120*1000;//2mins
    public static final int INTERVAL_SLOW_MOVE_IN_MS = 60*1000;//1min
    public static final int INTERVAL_FAST_MOVE_IN_MS = 30*1000;//30S
    public static final int INTERVAL_CHECK_STILL_IN_MS = 3*60*1000;//3mins
    public static int INTERVAL_REGISTER_ACTIVITY_IN_MIN = 12*60;;  /* 12 hours */

    public static final String KEY_REMOVE_LIST = "remove";
    public static final String KEY_ADD_NEW_LIST = "add_new";
    public static final String GEO_ID_PLIT_CHAR = "_";
    public static final String SIGNAL_KEY  = "signal";
    public static  final String ACTIVITY_STILL_FENCE_KEY = "activity_still_fence_key";
    public static  final String ACTIVITY_MOVE_FENCE_KEY = "activity_move_fence_key";
    public static final String REGISTER_ACTIVTY_WORK_TAG  = "register_activity_work_tag";
    public static final String REGISTER_ACTIVTY_INTERVAL_WORK_UNIQUE_NAME  = "register_activity_interval_work_unique_name";
    public static final String LOCATION_TRACKING_INTERVAL_WORK_TAG  = "location_tracking_interval_work_tag";
    public static final String LOCATION_TRACKING_INTERVAL_WORK_UNIQUE_NAME  = "location_tracking_interval_work_unique_name";
    //
    public static final String EXITING_LOCATION_FENCE_KEY = "EXITING_LOCATION_FENCE_KEY";
    public static final String ENTERING_LOCATION_FENCE_KEY = "ENTERING_LOCATION_FENCE_KEY";

    public enum SIGNAL {
        MOVE("MOVE");

        private final String text;
        /**
         * @param text
         */
        SIGNAL(final String text) {
            this.text = text;
        }
        @Override
        public String toString() {
            return text;
        }
    }

    public enum TRANSITION{
        ENTER,
        EXIT,
        UNKNOWN;
    }
}
