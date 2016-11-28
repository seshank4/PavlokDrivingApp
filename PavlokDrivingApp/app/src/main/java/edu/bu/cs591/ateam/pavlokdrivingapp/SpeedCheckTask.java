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

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    public static int speedLimit;
    private  String code;


    private LocationManager locationMangaer = null;

    public SpeedCheckTask(String code){
        this.code = code;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        double speed = 50.0;

        while(!stopTrip){

            //Log.i("SpeedCheck", "in While :"+stopTrip);

            if(isSpeedIllegal(speed)){
                doBeep();
            }else if(isSpeedNearWarning(speed)){
                // TODO: 11/27/2016 beep
            }else{
                continue;
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        
        Log.i("SpeedCheck"," I'am out :"+stopTrip);
        return null;
    }

    private boolean isSpeedNearWarning(double speed) {
        boolean isSpeedNearWarning = true;

        //double speedLimit = getSpeedLimit();


        return isSpeedNearWarning;
    }


    private boolean isSpeedIllegal(double speed) {
        boolean isSpeedLegal = false;

        //double speedLimit = getSpeedLimit();

        if(speed>speedLimit){
            return true;
        }
        return isSpeedLegal;
    }

    @Override
    protected void onPostExecute(Object o) {


        super.onPostExecute(o);
    }

    private void doBeep() {
        PavlokConnection conn = new PavlokConnection();
        if(this.code!=null && this.code!="") {
           // conn.execute("beep", 255, );
            String token = authorizeAndGetToken(code);
            try {
                doAction(token, "beep", 255);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String authorizeAndGetToken(String code) {
        URL url = null;
        try {
            url = new URL("http://pavlok-mvp.herokuapp.com/oauth/token");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection connection = null;

        connection = getHttpURLConnection(url, connection);

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

        System.out.println(connection.getResponseCode());

    }

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
