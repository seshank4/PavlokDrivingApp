package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.os.AsyncTask;
import android.util.Log;


public class SpeedCheckTask extends AsyncTask {

    public static boolean stopTrip = false;

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

    private double getSpeedLimit() {
        double speedLimit = 0.0;


        return speedLimit;
    }

    private boolean isSpeedIllegal(double speed) {
        boolean isSpeedLegal = true;

        double speedLimit = getSpeedLimit();


        return isSpeedLegal;
    }

    @Override
    protected void onPostExecute(Object o) {


        super.onPostExecute(o);
    }
}
