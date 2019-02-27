package phannguyen.com.gpsuseractivitytracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startService(new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class));
                //
                Intent serviceIntent = new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class);
                serviceIntent.putExtra("action","START");
                startService(serviceIntent);

            }
        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopService(new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class));
                //
                Intent serviceIntent = new Intent(MainActivity.this,ActivitiesTransitionRequestUpdateService.class);
                serviceIntent.putExtra("action","STOP");
                startService(serviceIntent);

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }
    }


}
