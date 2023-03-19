package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.EventTarget;

import java.util.EventListener;

public class homeActivity extends AppCompatActivity implements EventListener,OnMapReadyCallback {
    private Button startRunning, steps;

    private TextView Pulse, Longitude,Latitude,Altitude, Date, Time, Step;

    private DatabaseReference databaseGPSReference, databasePulseReference, databaseStepReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

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
        Step = findViewById(R.id.step);

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

        databaseStepReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration/");

        databaseStepReference.child("Step").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Step : " + dataSnapshot.getValue().toString();
                    Step.setText(output);
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

        databaseGPSReference.child("Time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String output = snapshot.getValue().toString();
                        Time.setText(output);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
    }
}