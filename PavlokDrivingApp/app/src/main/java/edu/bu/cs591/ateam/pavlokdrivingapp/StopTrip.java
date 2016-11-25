package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StopTrip extends AppCompatActivity {

    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_trip);

        btnStop = (Button)findViewById(R.id.btnStop);
        //TODO: make display turn off but keep running in background

        btnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //TODO: end trip and create log
            }
        });

    }
}
