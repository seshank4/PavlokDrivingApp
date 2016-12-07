package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by jlemus on 11/27/16.
 */

public class MyLocationListener implements LocationListener {
    private Context context;
    private Activity activity=null;
    private SpeedCheckTask speedCheckTask;
    private String BASE_URL = "api.tomtom.com/";
    private int VERSION_NUMBER = 2;
    private String EXT = "json/"; // the extension of the response. (json, jsonp, js, or xml)
    private String API_KEY = "h8fxx4ptxbtb4y7xv5r9x7ga";
    public static boolean flag = false;
    public static boolean stopFlag = false;

    MyLocationListener(Activity activity){
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location loc) {
        String longitude = "Longitude: " +loc.getLongitude();
        Log.e("in MyLocationListener", longitude);
        String latitude = "Latitude: " +loc.getLatitude();
        Log.e("in MyLocationListener", latitude);
        String speedLimit  = getSpeedLimitFromTomTom(loc.getLatitude()+"", loc.getLongitude()+"");
        String speedL = speedLimit.substring(0,speedLimit.indexOf("."));
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
        HttpURLConnection connection;
        TomTomResponse responseObj = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            ObjectMapper mapper = new ObjectMapper();
            try {
                System.out.println(connection.getResponseCode());
                JsonNode node  = mapper.readTree(connection.getInputStream());
                JsonNode subNode = node.get("addresses").get(0).get("address");
                responseObj = mapper.readValue(subNode,TomTomResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String startAddr = "";
            String startSubDiv = "";
            String startLat = "";
            String startLong = "";
            Date startTime = null;
            String destAddr = "";
            String destSubDiv = "";
            String destLat = "";
            String destLong = "";
            Date endTime = null;

            if(!flag){
                startAddr = responseObj.getFreeformAddress();
                startSubDiv = responseObj.getMunicipalitySubdivision();
                startLat = latitude;
                startLong = longitude;
                startTime = Calendar.getInstance().getTime();
                int tripId =  insertSourceInfo(startAddr,startSubDiv,startLat,startLong,startTime);
                flag = true;
                SharedPreferences prefs = this.activity.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp", Context.MODE_PRIVATE);
                prefs.edit().putInt("tripId",tripId).commit();
            }

            if(stopFlag){
                destAddr = responseObj.getFreeformAddress();
                destSubDiv = responseObj.getMunicipalitySubdivision();
                destLat = latitude;
                destLong = longitude;
                endTime = Calendar.getInstance().getTime();
                insertDestInfo(destAddr,destSubDiv,destLat,destLong,endTime);
                stopFlag = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseObj.getSpeedLimit();
    }

    private void insertDestInfo(String destAddr, String destSubDiv, String destLat, String destLong, Date destTime) {
        Statement stmt = null;
        Connection conn= null;
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(destTime.getTime());
        int userId=0;
        SharedPreferences prefs = this.activity.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp",Context.MODE_PRIVATE);
        int destTripId = prefs.getInt("tripId",0);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            stmt.executeUpdate("update pavlokdb.trip_summary set destination_addr='"+destAddr+"',dest_subdiv='"+destSubDiv+"',dest_lat='"+destLat+"',dest_long='"+destLong+"' where trip_id = "+destTripId);
            conn.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int insertSourceInfo(String startAddr, String startSubDiv, String startLat, String startLong, Date startTime) {
        Statement stmt = null;
        boolean status=false;
        Connection conn= null;
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(startTime.getTime());
        int tripId = 0;
        int userId=0;
        SharedPreferences prefs = this.activity.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp",Context.MODE_PRIVATE);
        userId = prefs.getInt("userId",0);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            stmt.executeUpdate("INSERT INTO pavlokdb.trip_summary(user_id,trip_start_dt,source_addr,source_subdiv,source_lat,source_long) VALUES('"+userId+"','"+sqlDate+"','"+startAddr+"','"+startSubDiv+"','"+startLat+"','"+startLong+"')",Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()){
                tripId = rs.getInt(1);
            }
            conn.commit();
        } catch (ClassNotFoundException e) {
                e.printStackTrace();
                } catch (SQLException e) {
                e.printStackTrace();
                }
        return tripId;
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
