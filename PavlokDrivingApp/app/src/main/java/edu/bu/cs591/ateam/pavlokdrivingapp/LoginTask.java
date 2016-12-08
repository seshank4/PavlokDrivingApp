package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Async Task that performs the network operations required for verifying the user credentials
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
        Connection conn = null;
        try {
            int count=0;
            //Initialize driver and connection
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306","ateam","theateam");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM pavlokdb.users WHERE EMAIL = '"+params[0]+"' AND cast(AES_DECRYPT(password,'pavlok') as char(50)) = '"+params[1]+"'");
            if(rs.next()){
                count = rs.getInt(1);
            }
            if(count==1) {
                //Count 1 means that the user is successfully authorized and will be logged in to the App
                login = true;
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery("SELECT user_id from pavlokdb.users where EMAIL = '"+params[0]+"' AND cast(AES_DECRYPT(password,'pavlok') as char(50)) = '"+params[1]+"'");
                SharedPreferences prefs = this.activity.getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp", Context.MODE_PRIVATE);
                if(rs1.next()) {
                    prefs.edit().putInt("userId", rs1.getInt(1)).commit();
                }
                //required for logging in the user automatically if he/she closes the app without logging out
                prefs.edit().putString("username", params[0].toString()).commit();
                prefs.edit().putString("password", params[1].toString()).commit();
                conn.close();
            }
            else {
                //User credentials do not match
                login = false;
                conn.close();
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
            //redirect to home page if login is successful
            Intent intent = new Intent(this.activity,MainActivity.class);
            activity.startActivity(intent);
        }
        else{
            //User being notified of a failed login attempt
            Toast toast = Toast.makeText(this.activity,"Invalid credentials. Please try again.",Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
