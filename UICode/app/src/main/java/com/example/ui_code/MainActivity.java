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

        //generate visuals
        setupUI();

        onClickListeners();
    }

    private void onClickListeners() {
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoGPSActivity();
            }
        });
    }

    private void gotoGPSActivity() {
        Intent intent = new Intent(this, GPSActivity.class);
        startActivity(intent);
    }

    private void setupUI() {
        buttonGPS = findViewById(R.id.gpsButtonId);
        buttonHR = findViewById(R.id.heartrateButtonId);
        buttonAcc = findViewById(R.id.accelerometerButtonId);
    }


}