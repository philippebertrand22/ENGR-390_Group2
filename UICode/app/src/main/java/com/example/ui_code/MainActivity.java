package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ImageView stopButton, playButton, pauseButton;

    private TextView timeValue, timeText, distanceValue, distanceText, speedValue, speedText, BPMValue, BPMText, stepText, stepValue;

    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private DatabaseReference databaseGPSReference, databasePulseReference, databaseStepReference, databaseUserReference, latestActivity, currentActivity;

    boolean timerStarted = false;

    private long step, bpm, longitude, latitude, altitude;
    private String date, currentTime, currentActivityTime, userKey;


    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    public ArrayList<LatLng> markers;

    homeActivity home = new homeActivity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        markers = new ArrayList<>();
        timer = new Timer();
        setupUI();
        getData();
        onClickListeners();
        stopButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        startTimer();

    }

    private void startTimer() {
            if(!timerStarted){
                timerStarted = true;
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            // This code runs once a second
                            public void run() {
                                time++;
                                //this shows the timer time
                                timeValue.setText(getTimerText());

                                //add a marker in the list
                                if(time%5 == 0){
                                    markers.add(new LatLng(home.locationLatitude, home.locationLongitude));
                                }

                                // Sends to database
                                getData();
                                //sendData();
                            }
                        });
                    }
                };
                timer.scheduleAtFixedRate(timerTask, 0, 1000);
            }
            else{
                timerStarted = false;
                timerTask.cancel();
            }
    }

    private String getTimerText() {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = (rounded % 86400) / 3600;

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }

    private void onClickListeners() {

        // add here whatever happens when you click the stop button
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeValue.setText(formatTime(0,0,0));

                startActivity(summaryActivity.class);
            }
        });

        // add here whatever happens when you click the pause button
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
                startTimer();
            }
        });

        // add here whatever happens when you click the play button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopButton.setVisibility(View.GONE);
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);

                startTimer();
            }
        });
    }

    // Function to start a new activity. We pass in the destination Activity name as param
    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }

    private void setupUI() {
        stopButton = findViewById(R.id.stopButtonID);
        playButton = findViewById(R.id.playButtonID);
        pauseButton = findViewById(R.id.pauseButtonId);
        timeValue = findViewById(R.id.timeValueID);
        timeText = findViewById(R.id.timeTextID);
        distanceText = findViewById(R.id.distanceTextID);
        distanceValue = findViewById(R.id.distanceValueID);
        speedValue = findViewById(R.id.speedValueID);
        speedText = findViewById(R.id.speedTextID);
        BPMValue = findViewById(R.id.BPMValueID);
        BPMText = findViewById(R.id.BPMTextID);
        stepText = findViewById(R.id.stepTextID);
        stepValue = findViewById(R.id.stepValueID);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
        userKey = user.getUid();


        databasePulseReference = FirebaseDatabase.getInstance().getReference("Pulse Sensor/");
        databaseStepReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration/");
        databaseGPSReference = FirebaseDatabase.getInstance().getReference("GPSData/");
        databaseUserReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activity");


        // Get the latest node ID
        databaseUserReference.child("node_count").get().addOnCompleteListener(task -> {
        if (task.isSuccessful()){
        Long latestNodeId=task.getResult().getValue(Long.class);

        // Check if the latest node ID exists
        if(latestNodeId==null){
        // If it doesn't, start at 1
        latestNodeId=1L;
        }else{
        // If it does, increment it
        latestNodeId++;
        }

        // Update the node count to reflect the new node
        databaseUserReference.child("node_count").setValue(latestNodeId);

        // Set the new node with the latest ID as the key
        latestActivity = databaseUserReference.child("Activity_" + String.valueOf(latestNodeId));
        latestActivity.child("Run").setValue(" Records");
        }
        });

    }

    private String getData(){

        currentActivityTime = getTimerText();

        databasePulseReference.child("State").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    bpm = (long) dataSnapshot.getValue();
                    String output = dataSnapshot.getValue().toString();
                    BPMValue.setText(output);
                }
            }
        });

        databaseStepReference.child("Step").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    step = (long) dataSnapshot.getValue();
                    String output = dataSnapshot.getValue().toString();
                    stepValue.setText(output);
                }
            }
        });

        databaseGPSReference.child("Latitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    latitude = (long) dataSnapshot.getValue();
                    String output = dataSnapshot.getValue().toString();
                }
            }
        });

        databaseGPSReference.child("Longitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    longitude = (long) dataSnapshot.getValue();
                    String output = dataSnapshot.getValue().toString();
                }
            }
        });

        databaseGPSReference.child("Altitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    altitude = (long) dataSnapshot.getValue();
                    String output = "Altitude : " + dataSnapshot.getValue().toString();
                }
            }
        });

        databaseGPSReference.child("Date").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    date = dataSnapshot.getValue().toString();
                }
            }
        });

        databaseGPSReference.child("Time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentTime = snapshot.getValue().toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return(date + "|" + currentTime + "|" + currentActivityTime + "|" + bpm + "|" + step + "|" + latitude + "|" + longitude + "|" + altitude);
    }

    private void sendData(){

        // Get the latest node ID
        latestActivity.child("number_of_entries").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Long latestEntryId=task.getResult().getValue(Long.class);

                // Check if the latest node ID exists
                if(latestEntryId==null){
                    // If it doesn't, start at 1
                    latestEntryId=1L;
                }else{
                    // If it does, increment it
                    latestEntryId++;
                }

                // Update the node count to reflect the new node
                latestActivity.child("number_of_entries").setValue(latestEntryId);


                // Set the new node with the latest ID as the key
                currentActivity = latestActivity.child(String.valueOf(latestEntryId));

                // Store the string array in the new node
                currentActivity.setValue("Test");
            }
        });



    }

    public List<LatLng> getList(){
        return markers;
    }
}