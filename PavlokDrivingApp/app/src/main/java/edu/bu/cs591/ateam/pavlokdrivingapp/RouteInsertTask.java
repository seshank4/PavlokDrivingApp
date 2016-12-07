package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.location.Location;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Aync task that saves the route coordinates data to the database to be used later in
 * the trip summary page to show the route on google map.
 * Created by karun on 12/6/2016.
 */
public class RouteInsertTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] params) {

        int tripId = (Integer) params[0];
        //ArrayList with all the coordinates which represent the entire route of a trip
        ArrayList<Location> routeLocList = (ArrayList<Location>) params[1];

        if(routeLocList!=null){
            Statement stmt = null;
            Connection conn = null;
            int count = 0;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
                stmt = conn.createStatement();
                conn.setAutoCommit(false);
                for(Location loc : routeLocList) {
                    if(count%2==0){
                        stmt.executeUpdate("INSERT INTO pavlokdb.trip_route(trip_id,lat,lon) VALUES('" + tripId + "','" + loc.getLatitude() + "','" + loc.getLongitude() + "')");
                    }
                    count++;
                }
                conn.commit();
                conn.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
