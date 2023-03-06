package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class homeActivity extends AppCompatActivity {
    private Button startRunning, steps;

    private TextView Pulse, Longitude,Latitude,Altitude, Date, Time;

    private DatabaseReference databaseGPSReference, databasePulseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupUI();
        onClickListeners();
    }

    private void onClickListeners() {
        startRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(MainActivity.class);
            }
        });

        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(StepsActivity.class);
            }
        });

    }
    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
    private void setupUI() {
        startRunning = findViewById(R.id.startRunningButtonID);
        steps = findViewById(R.id.StepsButton);

        Pulse = findViewById(R.id.Pulse);
        Longitude = findViewById(R.id.latitude);
        Latitude = findViewById(R.id.longitude);
        Altitude = findViewById(R.id.altitude);
        Date = findViewById(R.id.date);
        Time = findViewById(R.id.time);

        databasePulseReference = FirebaseDatabase.getInstance().getReference("Pulse Sensor/");

        databasePulseReference.child("State").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Pulse detected : " + dataSnapshot.getValue().toString();
                    Pulse.setText(output);
                }
            }
        });

        databaseGPSReference = FirebaseDatabase.getInstance().getReference("GPSData/");

        databaseGPSReference.child("Latitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String output = "Latitude : " + dataSnapshot.getValue().toString();
                        Latitude.setText(output);
                    }
            }
        });

        databaseGPSReference.child("Longitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Longitude : " + dataSnapshot.getValue().toString();
                    Longitude.setText(output);
                }
            }
        });

        databaseGPSReference.child("Altitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Altitude : " + dataSnapshot.getValue().toString();
                    Altitude.setText(output);
                }
            }
        });

        databaseGPSReference.child("Date").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Date : " + dataSnapshot.getValue().toString();
                    Date.setText(output);
                }
             }
        });

        databaseGPSReference.child("Time").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Time : " + dataSnapshot.getValue().toString();
                    Time.setText(output);
                }
            }
        });

    }

}