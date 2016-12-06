package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.Connection;
import java.sql.SQLException;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.DriverManager;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Intent mainActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences("edu.bu.cs591.ateam.pavlokdrivingapp", Context.MODE_PRIVATE);
        String loggedInUserName = prefs.getString("username", "-1");
        String loggedInUserPassword = prefs.getString("password","-1");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final EditText uName = (EditText) findViewById(R.id.etUserName);
        final EditText password = (EditText) findViewById(R.id.etPassword);

        loginButton = (Button) findViewById(R.id.Blogin);
        Button registerBtn = (Button) findViewById(R.id.signUpBtn);

        if (loggedInUserName != "-1" || loggedInUserPassword != "-1"){
            mainActivityIntent = new Intent(this,MainActivity.class);
            startActivity(mainActivityIntent);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginTask loginTask = new LoginTask(LoginActivity.this);

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
