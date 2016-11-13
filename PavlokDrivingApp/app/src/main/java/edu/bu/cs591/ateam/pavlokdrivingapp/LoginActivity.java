package edu.bu.cs591.ateam.pavlokdrivingapp;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText uName = (EditText) findViewById(R.id.etUserName);
        final EditText password = (EditText) findViewById(R.id.etPassword);

        loginButton = (Button) findViewById(R.id.Blogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginTask loginTask = new LoginTask(LoginActivity.this);

                loginTask.execute(uName.getText().toString(), password.getText().toString());

            }
        });

    }
}
