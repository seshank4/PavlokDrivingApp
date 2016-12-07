package edu.bu.cs591.ateam.pavlokdrivingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        final Button registerBtn = (Button) findViewById(R.id.registerBtn);
        final EditText firstName = (EditText) findViewById(R.id.etFirstName);
        final EditText lastName = (EditText) findViewById(R.id.etLastName);
        final EditText email = (EditText) findViewById(R.id.etEmail);
        final EditText password = (EditText) findViewById(R.id.etPassword);
        final EditText rePass = (EditText) findViewById(R.id.etPasswordConfirm);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validationPass = true;
                if(firstName.getText().toString().trim().equals("") || lastName.getText().toString().trim().equals("") || email.getText().toString().trim().equals("") || password.getText().toString().trim().equals("") || rePass.getText().toString().trim().equals("")){
                    Toast.makeText(getApplicationContext(), "Please fill all the fields to register successfully", Toast.LENGTH_SHORT).show();
                    validationPass = false;
                }
                if(!password.getText().toString().equals(rePass.getText().toString()))
                {
                    //PopUp msg
                    Toast toast = Toast.makeText(getApplicationContext(),"Passwords don't match!",Toast.LENGTH_LONG);
                    toast.show();
                    validationPass = false;
                }
                if(validationPass){
                    RegisterTask registerTask = new RegisterTask(RegisterUserActivity.this);
                    registerTask.execute(firstName.getText().toString(),lastName.getText().toString(),email.getText().toString(),password.getText().toString());
                }
            }
        });
    }
}
