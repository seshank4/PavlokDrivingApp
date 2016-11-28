package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by jlemus on 11/27/16.
 */

public class MyLocationListener implements LocationListener {
    private Context context;
    private SpeedCheckTask speedCheckTask;

    MyLocationListener(Context context, SpeedCheckTask speedCheckTask){
        this.context = context;
        this.speedCheckTask = speedCheckTask;
    }

    @Override
    public void onLocationChanged(Location loc) {
        Toast.makeText(this.context, "In onLocationChanged", Toast.LENGTH_SHORT).show();


        String longitude = "Longitude: " +loc.getLongitude();
        Log.e("in MyLocationListener", longitude);
        String latitude = "Latitude: " +loc.getLatitude();
        Log.e("in MyLocationListener", latitude);

        speedCheckTask.getSpeedLimitFromTomTom(loc.getLatitude()+"", loc.getLongitude()+"");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
