package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by karunesh on 11/13/2016.
 */
public class LoginTask extends AsyncTask{

    boolean login = false;
    Activity activity = null;

    LoginTask(Activity activity){
        this.activity = activity;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            int count=0;
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306","ateam","theateam");
            Log.i("raand",conn.toString());
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pavlokdb.users WHERE EMAIL = '"+params[0]+"' AND PASSWORD = '"+params[1]+"'");
            if(rs.next()){
                count = rs.getInt(1);
            }
            if(count==1) {
                login = true;
            }
            else {
                login = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        if(login){
            Intent intent = new Intent(this.activity,MainActivity.class);
            activity.startActivity(intent);
        }
        else{
            Toast toast = Toast.makeText(this.activity,"Invalid credentials. Please try again.",Toast.LENGTH_LONG);
            toast.show();
        }
    }

}