package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.BuildConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class homeActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button startRunning, steps;

    private TextView Pulse, Longitude,Latitude,Altitude, Date, Time;

    private DatabaseReference databaseGPSReference, databasePulseReference;
    private static final String TAG = homeActivity.class.getSimpleName();
    private GoogleMap map;

    double locationLatitude;
    double locationLongitude;

    Timer timer;

    TimerTask timerTask;

    Double time = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                    String latitude = dataSnapshot.getValue().toString();
                    locationLatitude = Double.parseDouble(latitude);
                }
            }
        });

        databaseGPSReference.child("Longitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Longitude : " + dataSnapshot.getValue().toString();
                    Longitude.setText(output);
                    String longitude = dataSnapshot.getValue().toString();
                    locationLatitude = Double.parseDouble(longitude);
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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.addMarker(new MarkerOptions().position(new LatLng(-15, 30)).title("Random"));
        LatLng here = new LatLng(locationLatitude,locationLongitude);
        map.addMarker(new MarkerOptions().position(here).title("Marker"));
        moveToCurrentLocation(here);
    }

    private void moveToCurrentLocation(LatLng currentLocation) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        map.animateCamera(CameraUpdateFactory.zoomIn());
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }
}