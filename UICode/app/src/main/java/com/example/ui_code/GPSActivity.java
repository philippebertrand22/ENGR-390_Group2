package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class GPSActivity extends AppCompatActivity {

    private TextView GPSdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsactivity);

        //generate visuals
        setupUI();
    }

    private void setupUI() {
        GPSdata = findViewById(R.id.textViewGPSId);

    }
}