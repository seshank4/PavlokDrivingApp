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
            conn.execute("beep", 255, this.code);
        }
    }

}
