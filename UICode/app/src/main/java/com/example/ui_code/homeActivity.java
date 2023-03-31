package com.example.ui_code;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class homeActivity extends AppCompatActivity implements EventListener, OnMapReadyCallback {
    private static final int REQUEST_CODE = 99;
    private Button startRunning, steps, graph;
    private TextView Pulse, Longitude, Latitude, Altitude, Date, Time, Step;

    private static final String TAG = homeActivity.class.getSimpleName();
    private GoogleMap map;

    public double locationLatitude;
    public double locationLongitude;
    private DatabaseReference databaseGPSReference, databasePulseReference, databaseStepReference;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupUI();
        onClickListeners();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
    }

    private void onClickListeners() {
        startRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(MainActivity.class);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                } else {
                                    // No location access granted.
                                }
                            }
                    );
// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        fusedLocationClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){

                    //THESE GET CURRENT LOCATION
                    Latitude.setText(String.valueOf(location.getLatitude()));
                    Longitude.setText(String.valueOf(location.getLongitude()));
                    locationLatitude = location.getLatitude();
                    locationLongitude = location.getLongitude();

                    //makes a marker at current location
                    LatLng here = new LatLng(locationLatitude,locationLongitude);
                    map.addMarker(new MarkerOptions().position(here).title("Marker").icon(BitmapFromVector(getApplicationContext(), R.drawable.baseline_circle_24)));
                    moveToCurrentLocation(here);
                }
            }
        });

        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(StepsActivity.class);
            }
        });

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(GraphPage.class);
            }
        });

    }
    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }

    private void setupUI() {
        startRunning = findViewById(R.id.startRunningButtonID);
        graph = findViewById(R.id.graph_btn);
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

//        databaseGPSReference.child("Latitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String output = "Latitude : " + dataSnapshot.getValue().toString();
//                    Latitude.setText(output);
//                    String latitude = dataSnapshot.getValue().toString();
//                    locationLatitude = Double.parseDouble(latitude);
//                }
//            }
//        });
//
//        databaseGPSReference.child("Longitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    String output = "Longitude : " + dataSnapshot.getValue().toString();
//                    Longitude.setText(output);
//                    String longitude = dataSnapshot.getValue().toString();
//                    locationLatitude = Double.parseDouble(longitude);
//                }
//            }
//        });

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
                    String output = "TEST : " + dataSnapshot.getValue().toString();
                    Date.setText(output);
                }
            }
        });

        databaseGPSReference.child("Time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String output = "Time : " + snapshot.getValue().toString();
                        Time.setText(output);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    //this function creates the map and the current location marker
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
    }



    //this function creates a custom marker
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);
        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);
        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //this function zooms to current location
    private void moveToCurrentLocation(LatLng currentLocation) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        map.animateCamera(CameraUpdateFactory.zoomIn());
        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dropdown_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // go to settings
                startActivity(ChangeInfoActivity.class);
                return true;
            case R.id.logout:
                // go to logout
                startActivity(LoginPage.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}