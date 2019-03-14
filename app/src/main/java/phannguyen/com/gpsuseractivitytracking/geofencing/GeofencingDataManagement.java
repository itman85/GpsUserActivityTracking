package phannguyen.com.gpsuseractivitytracking.geofencing;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;
import phannguyen.com.gpsuseractivitytracking.Constants;

import static io.paperdb.Paper.book;

public class GeofencingDataManagement {
    public static final String BOOK_GEO_POINTS_ON_PROGRESS_NAME = "bookName_geopoint_onprogress";
    public static final String BOOK_GEO_POINTS_LIST_NAME = "bookName_geopoint_list";
    //public static final String BOOK_GEO_POINT_STATUS_ITEMS_NAME = "bookName_geopoint_status_items";
    public static final String HASH_ITEM_KEY_GEOPOINTS_LIST = "hashItemKey_geopoints_list";
    private static GeofencingDataManagement instance;
    private List<GeoFencingPlaceModel> geoPointsList;
    private Map<String,GeoFencingPlaceStatusModel> geoPointsOnProgressMap;

    private static Object lock = new Object();

    private Context mContext;

    public static GeofencingDataManagement Instance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new GeofencingDataManagement();
            }
        }

        return instance;
    }

    public GeofencingDataManagement(){
        geoPointsOnProgressMap = new HashMap<>();
        geoPointsList = new ArrayList<>();
    }

    public void addGeopointsList(List<GeoFencingPlaceModel> geoList){
        book(BOOK_GEO_POINTS_LIST_NAME).write(HASH_ITEM_KEY_GEOPOINTS_LIST,geoList);
        geoPointsList = geoList;
        /*for(GeoFencingPlaceModel geoModel:geoList){
            GeoFencingPlaceStatusModel geoStatusModel = new GeoFencingPlaceStatusModel(geoModel);
            book(BOOK_GEO_POINT_STATUS_ITEMS_NAME).write(geoStatusModel.getName(),geoStatusModel);
        }*/
    }

    public void addOrUpdateGeoOnProgress(GeoFencingPlaceStatusModel geoStatusModel){
        /*if(geoStatusModel.getTransition()==Constants.TRANSITION.ENTER){
            if(!book(BOOK_GEO_POINTS_ON_PROGRESS_NAME).contains(geoStatusModel.getName())) {
                book(BOOK_GEO_POINTS_ON_PROGRESS_NAME).write(geoStatusModel.getName(), geoStatusModel);
                geoPointsOnProgressMap.put(geoStatusModel.getName(), geoStatusModel);
            }
        }else if(geoStatusModel.getTransition()==Constants.TRANSITION.EXIT ||
                geoStatusModel.getTransition()==Constants.TRANSITION.UNKNOWN){
            if(book(BOOK_GEO_POINTS_ON_PROGRESS_NAME).contains(geoStatusModel.getName())) {
                book(BOOK_GEO_POINTS_ON_PROGRESS_NAME).delete(geoStatusModel.getName());
                geoPointsOnProgressMap.remove(geoStatusModel.getName());
            }
        }*/
        book(BOOK_GEO_POINTS_ON_PROGRESS_NAME).write(geoStatusModel.getName(), geoStatusModel);
        if(geoPointsOnProgressMap==null || geoPointsOnProgressMap.isEmpty())
            initGeoPointsOnProgressMap();
        geoPointsOnProgressMap.put(geoStatusModel.getName(), geoStatusModel);
    }

    public List<GeoFencingPlaceModel> getAllGeoPoints(){
        if(geoPointsList==null || geoPointsList.size()==0){
            geoPointsList = book(BOOK_GEO_POINTS_LIST_NAME).read(HASH_ITEM_KEY_GEOPOINTS_LIST,null);
        }
        return geoPointsList;
    }

    public GeoFencingPlaceStatusModel getGeoFencingPointOnProgress(String geoPointKey){
        if(geoPointsOnProgressMap!=null && !geoPointsOnProgressMap.isEmpty()){
            if(geoPointsOnProgressMap.containsKey(geoPointKey))
                return geoPointsOnProgressMap.get(geoPointKey);
            return null;//not existed
        }else{
            initGeoPointsOnProgressMap();
            if(geoPointsOnProgressMap!=null && !geoPointsOnProgressMap.isEmpty()){
                if(geoPointsOnProgressMap.containsKey(geoPointKey))
                    return geoPointsOnProgressMap.get(geoPointKey);
                return null;//not existed
            }
        }
        return null;
    }

    private void initGeoPointsOnProgressMap(){
        getAllGeoPoints();
        if(geoPointsList!=null && !geoPointsList.isEmpty()){
            for(GeoFencingPlaceModel geoModel:geoPointsList){
                GeoFencingPlaceStatusModel geoPointStatusModel = book(BOOK_GEO_POINTS_ON_PROGRESS_NAME).read(geoModel.getName(),null);
                if(geoPointStatusModel!=null){
                    geoPointsOnProgressMap.put(geoModel.getName(),geoPointStatusModel);
                }
            }
        }

    }


}
