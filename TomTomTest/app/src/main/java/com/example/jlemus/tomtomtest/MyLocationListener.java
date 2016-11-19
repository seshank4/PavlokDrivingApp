package com.example.jlemus.tomtomtest;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jlemus on 11/18/16.
 */

public class MyLocationListener implements LocationListener {



    @Override
    public void onLocationChanged(Location location) {
        //Log.d("LOCATION_CALLED, "onLocationChanged");
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
