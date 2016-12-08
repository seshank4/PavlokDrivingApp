package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Contains functions related to the Login Module.
 */
public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Intent mainActivityIntent;

    /**
     * Oncreate for Login Activity. Class has functions required for authorizing the user
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp", Context.MODE_PRIVATE);
        //For logging in the user automatically if he/she closes and reopens the app without logging out
        String loggedInUserName = prefs.getString("username", "-1");
        String loggedInUserPassword = prefs.getString("password","-1");
        //required for alllowing network operations on main thread. We know that this is a small operation with minimum impact on performance
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final EditText uName = (EditText) findViewById(R.id.etUserName);
        final EditText password = (EditText) findViewById(R.id.etPassword);

        loginButton = (Button) findViewById(R.id.Blogin);
        Button registerBtn = (Button) findViewById(R.id.signUpBtn);

        //-1 means that the user has to login again, since it is the default return valued
        // for the shared prefs.
        if (loggedInUserName != "-1" || loggedInUserPassword != "-1"){
            mainActivityIntent = new Intent(this,MainActivity.class);
            startActivity(mainActivityIntent);
        }
        //executes on click of the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginTask loginTask = new LoginTask(LoginActivity.this);
                //launching an async task that verifies the user credentials
                loginTask.execute(uName.getText().toString(), password.getText().toString());
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterUserActivity.class);
                startActivity(intent);
            }
        });
    }
}
