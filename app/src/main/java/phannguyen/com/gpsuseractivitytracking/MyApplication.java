package phannguyen.com.gpsuseractivitytracking;

import android.app.Application;
import android.content.Context;

import io.paperdb.Paper;

public class MyApplication extends Application {
    public static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
        appContext = this;
    }


}
