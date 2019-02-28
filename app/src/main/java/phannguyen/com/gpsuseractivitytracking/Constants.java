package phannguyen.com.gpsuseractivitytracking;

public class Constants {
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000;

    public static final int CONFIDENCE = 70;

    public static long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    public static long FASTEST_INTERVAL = 2000; /* 2 sec */

    public static final String KEY_REMOVE_LIST = "remove";
    public static final String KEY_ADD_NEW_LIST = "add_new";
    public static final String GEO_ID_PLIT_CHAR = "_";
}
