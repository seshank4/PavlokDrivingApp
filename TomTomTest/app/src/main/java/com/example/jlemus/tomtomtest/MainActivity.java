package com.example.jlemus.tomtomtest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //final TextView mTextView; = (TextView) findViewById(R.id.text);
    private TextView mTextView;
    private String TAG = "tag_tom";

    //tomtom documentation for api:
    // http://developer.tomtom.com/products/onlinenavigation/onlinesearch/documentation/Reverse_Geocoding/ReverseGeocode
    //volley documentation:
    //https://developer.android.com/training/volley/simple.html

    //tomtom api variables setup here
    private String BASE_URL = "api.tomtom.com/";
    private int VERSION_NUMBER = 2;
    private String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";

    // example string request
    //GET https://<baseURL>/search/<versionNumber>/reverseGeocode/<position>.<ext>?key=<apiKey>
    // [&spatialKeys=<spatialKeys>][&returnSpeedLimit=<returnSpeedLimit>][&heading=<heading>]
    // [&radius=<radius>][&streetNumber=<streetNumber>][&returnRoadUse=<returnRoadUse>]
    // [&roadUse=<roadUse>]


    //---------------------------------------------------------------//


    //google maps api stuff set here
    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;

    private Button btnGetLocation = null;
    private EditText editLocation = null;
    private ProgressBar pb = null;
    private static final int GPS_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up initial progress bar state
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        editLocation = (EditText) findViewById(R.id.etEditLocation);

        btnGetLocation = (Button) findViewById(R.id.btnGetCoordinates);
        locationMangaer = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // check if GPS is enabled
        if (locationMangaer.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(getApplicationContext(), "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check if GPS is enabled
                if (locationMangaer.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(getApplicationContext(), "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
                }else{
                    showGPSDisabledAlertToUser();
                }

                pb.setVisibility(View.VISIBLE);


                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            GPS_REQUEST_CODE);
                }

                //}
                else {
                    locationListener = new MyLocationListener();
                    //Toast.makeText(MainActivity.this, "in else activity granted", Toast.LENGTH_LONG).show();
                    // gets the gps coords every 5 seconds and when you have moved more than 1 meter
                    // leave at 0 for testing
                    locationMangaer.requestLocationUpdates(LocationManager
                            .GPS_PROVIDER, 5000, 0, locationListener);
                }
            }
        });
    }



    private void getJsonStuff(final String latitude, final String longitude){
        //        // Request a string response from the provided URL.
        final String url2 = "https://" + BASE_URL + "search/" + VERSION_NUMBER + "/reverseGeocode/" +
                latitude + "," + longitude + "." + EXT + "?key=" + API_KEY + "&returnSpeedLimit=true"
                + "&returnRoadUse=true" + "&roadUse=" + "[\"Arterial\"]";

        mTextView = (TextView) findViewById(R.id.text2);
        Log.e("My App",url2);
        Toast.makeText(MainActivity.this, "lattt and long are" + longitude + "," + latitude, Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

                        try {

                            JSONObject obj = new JSONObject(response.substring(0));
                            String speedLimit = obj.getJSONArray("addresses").getJSONObject(0)
                                    .getJSONObject("address").getString("speedLimit");
                            Log.e("My App", obj.getJSONArray("addresses").getJSONObject(0).getJSONObject("address").getString("speedLimit"));
                            mTextView.setText("Response is: "+ response.substring(0)
                                    + "\n\n speedlimit is " + speedLimit);


                        } catch (Throwable t) {
                            Log.e("My App", "Could not parse malformed JSON: \"" + response.substring(0) + "\"");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });
 //Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){

        switch (requestCode) {
            case GPS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "in else activity granted", Toast.LENGTH_LONG).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }


    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(MainActivity.this, "In onLocationChanged", Toast.LENGTH_SHORT).show();
            editLocation.setText("");
            pb.setVisibility(View.INVISIBLE);
            String longitude = "Longitude: " +loc.getLongitude();
            Log.d(TAG, longitude);
            String latitude = "Latitude: " +loc.getLatitude();
            Log.d(TAG, latitude);

            getJsonStuff(loc.getLatitude()+"", loc.getLongitude()+"");
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }


}

