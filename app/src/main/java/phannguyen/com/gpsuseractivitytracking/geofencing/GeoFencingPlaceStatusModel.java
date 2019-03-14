package phannguyen.com.gpsuseractivitytracking.geofencing;

import phannguyen.com.gpsuseractivitytracking.Constants;

public class GeoFencingPlaceStatusModel extends GeoFencingPlaceModel {
    private Constants.TRANSITION transition;
    private long lastActiveTime;//!=0 when user enter or exit

    public GeoFencingPlaceStatusModel(double lat, double lng, int radius, String name) {
        super(lat, lng, radius, name);
    }

    public GeoFencingPlaceStatusModel(GeoFencingPlaceModel model){
        super(model.getLat(),model.getLng(),model.getRadius(),model.getName());
        transition = Constants.TRANSITION.UNKNOWN;
        lastActiveTime = 0;
    }
    public Constants.TRANSITION getTransition() {
        return transition;
    }

    public void setTransition(Constants.TRANSITION transition) {
        this.transition = transition;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
}
