package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.os.StrictMode;
import android.util.Log;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class that has method to get the address information based on Lat Lon from TomTom API
 * Created by karun on 12/2/2016.
 */
public class TomTomUtil {

    private static String BASE_URL = "api.tomtom.com/";
    private static int VERSION_NUMBER = 2;
    private static String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private static String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";

    public static TomTomResponse getTomTomResponse(double lat, double lon) {
        final String string_url = "https://" + BASE_URL + "search/" + VERSION_NUMBER + "/reverseGeocode/" +
                lat + "," + lon + "." + EXT + "?key=" + API_KEY + "&returnSpeedLimit=true"
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
        Log.e("in SpeedCheckTask.get", url.toString());
        HttpURLConnection connection;
        TomTomResponse responseObj = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(connection.getResponseCode());
            JsonNode node = mapper.readTree(connection.getInputStream());
            JsonNode subNode = node.get("addresses").get(0).get("address");
            responseObj = mapper.readValue(subNode, TomTomResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseObj;
    }
}
