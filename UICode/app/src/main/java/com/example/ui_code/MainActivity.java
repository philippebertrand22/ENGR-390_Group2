package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button buttonGPS;

    private Button buttonHR;

    private Button buttonAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setupUI();

        onClickListeners();
    }

    private void onClickListeners() {
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNextActivity(GPSActivity.class);
            }
        });

        buttonAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNextActivity(StepsActivity.class);
            }
        });

        buttonHR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNextActivity(BPMActivity.class);
            }
        });
    }

    // Function to start a new activity. We pass in the destination Activity name as param
    private void startNextActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }

    private void setupUI() {
        buttonGPS = findViewById(R.id.gpsButtonId);
        buttonHR = findViewById(R.id.heartrateButtonId);
        buttonAcc = findViewById(R.id.accelerometerButtonId);
    }

}