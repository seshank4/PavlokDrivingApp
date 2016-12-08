package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Async Task that performs user registration.
 */
public class RegisterTask extends AsyncTask {

    Activity activity = null;
    boolean userAlreadyExists = false;
    boolean registered = false;

    RegisterTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        Statement stmt = null;
        Statement stmt1 = null;
        boolean status = false;
        int count = 0;
        Connection conn = null;
        String firstName = params[0].toString();
        String lastName = params[1].toString();
        String email = params[2].toString();
        String password = params[3].toString();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://pavlokdb.cwxhunrrsqfb.us-east-2.rds.amazonaws.com:3306", "ateam", "theateam");
            stmt1 = conn.createStatement();
            ResultSet rs = stmt1.executeQuery("SELECT first_name FROM pavlokdb.users WHERE EMAIL = '" + email + "'");
            if (rs.next()) {
                //If we reach here it means that the user is already registered with our app
                userAlreadyExists = true;
                registered = false;
            } else {
                // new user so insert a record to database
                try {
                    stmt = conn.createStatement();
                    count = stmt.executeUpdate("INSERT INTO pavlokdb.users(first_name,last_name,email,password) VALUES('" + firstName + "','" + lastName + "','" + email + "',aes_encrypt('" + password + "','pavlok'))");

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (count == 1) {
                    //Insert was successful, Go Back to Login Activity to allow the user to login with the credentials just created
                    Intent intent = new Intent(this.activity, LoginActivity.class);
                    activity.startActivity(intent);
                    registered = true;
                } else {
                    registered = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        if (registered) {
            //Notify user of a successful registration
            Toast toast = Toast.makeText(this.activity, "Registered successfully!", Toast.LENGTH_SHORT);
            toast.show();
        }
        if (userAlreadyExists) {
            //Notify user of a failed registration
            Toast toast = Toast.makeText(this.activity, "This email is already registered.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
