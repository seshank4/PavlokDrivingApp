package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sesha on 11/26/2016.
 */

public class SpeedCheckTask extends AsyncTask {

    public static boolean stopTrip = false;


    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;
    private String BASE_URL = "api.tomtom.com/";
    private int VERSION_NUMBER = 2;
    private String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";
    private Context context;


    SpeedCheckTask(Context context){
        this.context = context;
        locationMangaer = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
    protected Object doInBackground(Object[] params) {

        double speedLimit = 50.0;
        double speed = 50.0;
        
        while(!stopTrip){

            //Log.i("SpeedCheck", "in While :"+stopTrip);

            if(isSpeedIllegal(speed)){

                // TODO: 11/27/2016   beep and vibrate
            }else if(isSpeedNearWarning(speed)){
                // TODO: 11/27/2016 beep
            }else{
                continue;
            }


            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        
        Log.i("SpeedCheck"," I'am out :"+stopTrip);
        return null;
    }

    private boolean isSpeedNearWarning(double speed) {
        boolean isSpeedNearWarning = true;

        double speedLimit = getSpeedLimit();


        return isSpeedNearWarning;
    }

    // use tomtom api to get speedlimit here
    protected double getSpeedLimit() {

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        //if permissions have already been granted, grab a reference to the class defined
        // MyLocationListener
        else {
            locationListener = new MyLocationListener(this.context, SpeedCheckTask.this);

            // gets the gps coords every 5 seconds and when you have moved more than 1 meter
            // leave at 0 for testing
            Log.e("calling requestlocation", "calling requestlocation");
            locationMangaer.requestLocationUpdates(LocationManager
                    .GPS_PROVIDER, 10000, 0, locationListener);
        }



        int speedLimit = getTomTomResponse();




        return speedLimit;
    }

    private int getTomTomResponse() {

        return 0;
    }

    private boolean isSpeedIllegal(double speed) {
        boolean isSpeedLegal = true;

        //double speedLimit = getSpeedLimit();


        return isSpeedLegal;
    }

    @Override
    protected void onPostExecute(Object o) {


        super.onPostExecute(o);
    }


    protected void getSpeedLimitFromTomTom(final String latitude, final String longitude){
        final String string_url = "https://" + BASE_URL + "search/" + VERSION_NUMBER + "/reverseGeocode/" +
                latitude + "," + longitude + "." + EXT + "?key=" + API_KEY + "&returnSpeedLimit=true"
                + "&returnRoadUse=true" + "&roadUse=" + "[\"Arterial\"]";
        //StrictMode stuff has to be here because there was an error being thrown.
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Creating the URL object to pass to the HTTP request function
        // must put in try catch since url may be invalid
        URL url = null;
        try {
            url = new URL(string_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.e("in SpeedCheckTask.get",url.toString());
        Toast.makeText(context, "lat and long are" + longitude + "," + latitude, Toast.LENGTH_SHORT).show();

        //sb variable is for testing
        StringBuilder sb = new StringBuilder();
        //pass url and sb to getHTTPConnection
        sb = getHttpURLConnection(url,sb);
        Log.e("printing tomtom", String.valueOf(sb));
    }

    //returns a stringbuilder object for now.
    @NonNull
    private StringBuilder getHttpURLConnection(URL url, StringBuilder sb) {
        HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");

            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader((InputStream) connection.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return sb;
    }






}
