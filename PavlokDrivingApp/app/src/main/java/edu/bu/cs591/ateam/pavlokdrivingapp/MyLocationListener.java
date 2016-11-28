package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by jlemus on 11/27/16.
 */

public class MyLocationListener implements LocationListener {
    private Context context;
    private SpeedCheckTask speedCheckTask;
    private String BASE_URL = "api.tomtom.com/";
    private int VERSION_NUMBER = 2;
    private String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";

    @Override
    public void onLocationChanged(Location loc) {
        //Toast.makeText(this.context, "In onLocationChanged", Toast.LENGTH_SHORT).show();


        String longitude = "Longitude: " +loc.getLongitude();
        Log.e("in MyLocationListener", longitude);
        String latitude = "Latitude: " +loc.getLatitude();
        Log.e("in MyLocationListener", latitude);

        String speedLimit  = getSpeedLimitFromTomTom(loc.getLatitude()+"", loc.getLongitude()+"");

        String speedL = speedLimit.substring(0,speedLimit.indexOf("."));
        SpeedCheckTask.speedLimit = Integer.parseInt(speedL);
    }

    protected String getSpeedLimitFromTomTom(final String latitude, final String longitude){
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
//        Toast.makeText(context, "lat and long are" + longitude + "," + latitude, Toast.LENGTH_SHORT).show();

        /*//sb variable is for testing
        StringBuilder sb = new StringBuilder();
        //pass url and sb to getHTTPConnection
        sb = getHttpURLConnection(url,sb);*/
        HttpURLConnection connection;
        TomTomResponse responseObj = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            ObjectMapper mapper = new ObjectMapper();
            try {
                System.out.println(connection.getResponseCode());
                //responseObj = mapper.readValue(connection.getInputStream(), TomTomResponse.class);
                JsonNode node  = mapper.readTree(connection.getInputStream());
                JsonNode subNode = node.get("addresses").get(0).get("address");
                responseObj = mapper.readValue(subNode,TomTomResponse.class);
               // subNode.get("address")
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader((InputStream) connection.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.e("printing tomtom", String.valueOf(sb));
        return responseObj.getSpeedLimit();
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
