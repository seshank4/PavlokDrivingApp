package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by karun on 12/5/2016.
 */
public class TripHistoryTask extends AsyncTask {
    Activity activity;
    private ArrayList<Trip> userTrips;

    TripHistoryTask(Activity activity){
        this.activity = activity;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        SharedPreferences prefs = this.activity.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", 0);
        Connection conn = null;
        userTrips = new ArrayList<>();
        try {
            int infractionsCount = 0;
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            Statement stmt = conn.createStatement();
            conn.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery("SELECT trip_id,source_addr,destination_addr,trip_start_dt FROM pavlokdb.trip_summary WHERE user_id = '" + userId + "'");
            while (rs.next()) {
                Trip userTrip = new Trip();
                userTrip.setTripId(rs.getInt("trip_id"));
                userTrip.setSource(rs.getString("source_addr"));
                userTrip.setSource(rs.getString("destination_addr"));
                userTrip.setTripStartDate(String.valueOf(rs.getDate("trip_start_dt")));
                userTrips.add(userTrip);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        Intent intent = new Intent(this.activity,MainActivity.class);
        intent.putExtra("userTrips", userTrips);
        intent.putExtra("isRedirect",true);
        intent.putExtra("isFromHistory",true);
        activity.startActivity(intent);
    }
}
