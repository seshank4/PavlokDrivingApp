package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Async task that performs the important function of periodically comparing the vehicle speed with the speedlimit of the
 * current road and sending beep+vibrate+led flash in case of an infraction and just a beep in case the speed is
 * approaching the speedlimit.
 */
public class SpeedCheckTask extends AsyncTask {

    public static boolean stopTrip = false;
    private int speedLimit;
    public static String token = "";
    private  String code;
    public double vehicleSpeed;
    private LocationManager locationManager;
    private Activity activity;
    private int tripId;
    public static ArrayList<Location> routeTrace = new ArrayList<>();

    public SpeedCheckTask(String code, LocationManager locationManager,Activity activity,int tripId){
        this.locationManager = locationManager;
        this.code = code;
        this.activity = activity;
        this.speedLimit = 20;
        this.tripId = tripId;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        token = authorizeAndGetToken(code);
        //check for GPS permission
        if (ActivityCompat.checkSelfPermission(this.activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }else {
            //Keep the thread alive until the stop trip button is clicked by the user
            routeTrace = new ArrayList<>();
            SpeedCheckTask.stopTrip = false;
            while (!stopTrip) {
                //get updated location
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //add the current coordinates to the route list
                if(routeTrace != null) {
                    routeTrace.add(loc);
                }
                TomTomResponse responseObj = null;
                if(loc != null) {
                    //get the speed of the vehicle from the Location Manager which user the GPS to calculate the speed
                    // to a very precise value
                    vehicleSpeed = loc.getSpeed();
                    //convert meters per second to miles per hour
                    vehicleSpeed = vehicleSpeed / 0.44704;
                    //vehicleSpeed = 35.0;
                    //Call the TomTom Api to get the SpeedLimit
                    responseObj = TomTomUtil.getTomTomResponse(loc.getLatitude(),loc.getLongitude());
                }
                Log.d("Spped", "Current speed is " + String.valueOf(vehicleSpeed));
                Log.d("Spped", "speed limit is " + String.valueOf(speedLimit));
                Log.d("Frequent TomTom", "Calling tomtom api frequient");
                if(null != responseObj) {
                    //Map speedlimit form the TomTom response
                    String speedLim = responseObj.getSpeedLimit();
                    String speedL = speedLim.substring(0, speedLim.indexOf("."));
                    speedLimit = Integer.parseInt(speedL);
                }
                if(speedLimit==0){
                    //set speed limit to 20 if the road does not have a speed limit
                    speedLimit=20;
                }
                //speedLimit = 40;
                if (isSpeedIllegal(vehicleSpeed)) {
                    //perform a beep+vibrate+led flash in case of infraction
                    doBeep();
                    doVibrate();
                    flashLED();
                    insertSpeedViolationInfo(speedLimit,vehicleSpeed);
                } else if (isSpeedNearWarning(vehicleSpeed)) {
                    //Just a beep for a warning
                    doBeep();
                }
                try {
                    //Sleep for a second before checking the speed and speedlimit again
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Insert the infraction information into the database periodically
     * @param speedLimit
     * @param vehicleSpeed
     */
    private void insertSpeedViolationInfo(int speedLimit, double vehicleSpeed) {

        SharedPreferences prefs = this.activity.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId",0);
        Statement stmt = null;
        Connection conn= null;
        Date currTime = Calendar.getInstance().getTime();
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(currTime.getTime());
        double lat = 0.0;
        double lon = 0.0;
        String violationType = "Speeding";
        if (ActivityCompat.checkSelfPermission(this.activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }else {
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            stmt.executeUpdate("INSERT INTO pavlokdb.trip_detail(trip_id,user_id,violation_type,speed_limit,vehicle_speed,latitude,longitude) VALUES('"+tripId+"','"+userId+"','"+violationType+"','"+speedLimit+"','"+vehicleSpeed+"','"+lat+"','"+lon+"')");
            conn.commit();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * function to check if the user needs to be warned of the speed approaching the speedlimit
     * @param speed
     * @return
     */
    private boolean isSpeedNearWarning(double speed) {
        Log.d("Spped", "Current speed is " + String.valueOf(vehicleSpeed));
        if(speed>=speedLimit-5){
            return true;
        }
        return false;
    }

    /**
     * function to check if the user need to be alerted about an infraction
     * @param speed
     * @return
     */
    private boolean isSpeedIllegal(double speed) {

        if(speed>speedLimit){
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

    /**
     * function to beep the pavlok device
     */
    private void doBeep() {
        PavlokConnection conn = new PavlokConnection();
        if(this.token!=null && this.token!="") {
            try {
                doAction(token, "beep", 255);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * function to vibrate the pavlok device
     */
    private void doVibrate() {
        PavlokConnection conn = new PavlokConnection();
        if(this.token!=null && this.token!="") {
            try {
                doAction(token, "vibration", 255);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * function to flash the LED of the pavlok device
     */
    private void flashLED() {
        PavlokConnection conn = new PavlokConnection();
        if(this.token!=null && this.token!="") {
            try {
                doAction(token, "led", 4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function to get the token from the Pavlok APi by passing in the Authorization code obtained
     * after oAuth Authorization while logging in
     * @param code
     * @return
     */
    private String authorizeAndGetToken(String code) {
        URL url = null;
        try {
            url = new URL("http://pavlok-mvp.herokuapp.com/oauth/token");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        connection = getHttpURLConnection(url, connection);
        //JSON Parameter that will be sent with the request
        JSONObject obj = new JSONObject();
        try {
            obj.put("client_id", "8882d3c9f67eff55ff7b0c535d2a6ccd189d47cd7a7b42c531ad25d413baadd4");
            obj.put("client_secret", "07eaa8a1d37b2cfb029d910d467af98ee1d90daf685a477cadf2069ec00add4f");
            obj.put("code", code);
            obj.put("grant_type", "authorization_code");
            obj.put("redirect_uri","http://pavlok-bu-cs591/auth/pavlok/result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataOutputStream printout = null;
        DataInputStream input;
        try {
            printout = new DataOutputStream(connection.getOutputStream());
            printout.write(obj.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        Jakson mapper to automatically map the response to a model class
         */
        ObjectMapper mapper = new ObjectMapper();
        Authorized responseObj = null;
        try {
            System.out.println(connection.getResponseCode());
            responseObj = mapper.readValue(connection.getInputStream(), Authorized.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(responseObj !=null){
            return responseObj.getAccess_token();
        }
        return "";
    }

    /**
     * Generic method that performs an "Action" based on the input parameters.
     * The action could be one of Beep/Vibrate/Flash LED
     * @param access_token
     * @param action
     * @param intensity
     * @throws IOException
     */
    public void doAction(String access_token, String action, int intensity) throws IOException {

        URL url = new URL("http://pavlok-mvp.herokuapp.com/api/v1/stimuli/"+action+"/"+intensity);
        HttpURLConnection connection = null;
        connection = getHttpURLConnection(url, connection);
        JSONObject obj = new JSONObject();
        try {
            obj.put("access_token", access_token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataOutputStream printout = null;
        DataInputStream input;

        try {
            printout = new DataOutputStream(connection.getOutputStream());
            printout.write(obj.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
       Log.i("pavres",String.valueOf(connection.getResponseCode()));
    }

    /**
     * Function to get a connection. Refractored this to new method to avoid code repetition
     * @param url
     * @param connection
     * @return
     */
    @NonNull
    private HttpURLConnection getHttpURLConnection(URL url, HttpURLConnection connection) {
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
