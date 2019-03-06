package phannguyen.com.gpsuseractivitytracking;

public class Constants {
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000;

    public static final int CONFIDENCE = 70;

    public static long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    public static long FASTEST_INTERVAL = 2000; /* 2 sec */
    public static final int STAY_DISTANCE_IN_MET = 100;//in this distance, device consider as not move
    public static long TIMEOUT_STAY_LOCATION = 5*60*1000;  /* 5 MINS USER stay a location in 5 mins consider as STILL*/

    public static final String KEY_REMOVE_LIST = "remove";
    public static final String KEY_ADD_NEW_LIST = "add_new";
    public static final String GEO_ID_PLIT_CHAR = "_";
    public static final String SIGNAL_KEY  = "signal";
    public static  final String ACTIVITY_FENCE_KEY = "activity_fence_key";
    public static final String REGISTER_ACTIVTY_WORK_TAG  = "register_activity_work_tag";

    public enum SIGNAL {
        NOT_STILL("NOT_STILL");

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
}
