package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

    private DatabaseReference databaseGPSReference, databasePulseReference, databaseStepReference, databaseUserReference, currentActivity;

    boolean timerStarted = false;

    private long step, bpm, longitude, latitude, altitude;
    private String date, currentTime, currentActivityTime, userKey;

    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    public ArrayList<LatLng> markers;
    homeActivity home = new homeActivity();

    /*
    How the activity logs work:
    Gets a reference to the user's folder using user.getuid()
    Creates a new folder with a number based on a "activity_count" in database (only once)
    Through startTimer(), calls addNewEntry() to add sensor values in database. Called every second
     */

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

                                // Adds sensor values as new entry in database
                                addNewEntry(currentActivity, getData());
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

    private void stopTimer(){
        timerStarted = false;
        timerTask.cancel();
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

                stopTimer();
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
                stopTimer();
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
        userKey = user.getUid(); // Userkey is unique to whoever logged in


        databasePulseReference = FirebaseDatabase.getInstance().getReference("Pulse Sensor/");
        databaseStepReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration/");
        databaseGPSReference = FirebaseDatabase.getInstance().getReference("GPSData/");
        databaseUserReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities");


        createNewFolder(databaseUserReference, new OnNewFolderCreatedListener() {
            @Override
            public void onNewFolderCreated(DatabaseReference newFolder) {
                // Save the new folder reference as a global variable
                currentActivity = newFolder;
            }
        });

    }

    private void createNewFolder(DatabaseReference rootRef, OnNewFolderCreatedListener listener) {
        // Get the latest activity count
        rootRef.child("activity_count").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Long latestNodeId = task.getResult().getValue(Long.class);

                // Check if the latest node ID exists
                if (latestNodeId == null) {
                    // If it doesn't, start at 1
                    latestNodeId = 1L;
                } else {
                    // If it does, increment it
                    latestNodeId++;
                }

                // Create new folder with latest ID in the key
                DatabaseReference newFolder = rootRef.child("Activity_" + String.valueOf(latestNodeId));

                // Update the activity count with incremented value
                rootRef.child("activity_count").setValue(latestNodeId).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        // Return the new folder reference to the listener
                        // Needed to implement a listener because I was getting some Lambda and reference errors otherwise
                        listener.onNewFolderCreated(newFolder);
                    }
                });
            }
        });
    }

    private void addNewEntry(DatabaseReference rootRef, String message) {
        if (rootRef != null) {

            // Get latest entry_count
            rootRef.child("entry_count").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Long latestNodeId = task.getResult().getValue(Long.class);

                    // Check if there is an entry
                    if (latestNodeId == null) {
                        // If it doesn't, start at 1
                        latestNodeId = 1L;
                    } else {
                        // If it does, increment it
                        latestNodeId++;
                    }
                    if (latestNodeId >= 50) {
                        //Limit the amount of entries for the time being
                        stopTimer(); // Stops the timer and hence the queries
                    }

                    // Set the new entry with the latest ID as the key
                    rootRef.child(String.valueOf(latestNodeId)).setValue(message);

                    // Update the entry count to reflect the new node
                    rootRef.child("entry_count").setValue(latestNodeId);
                }
            });
        }


    }

    private interface OnNewFolderCreatedListener {
        void onNewFolderCreated(DatabaseReference newFolder);
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

        return("Date: " + date + " | Time: " + currentTime + " | Running Time: " + currentActivityTime + " | HeartBeat: " + bpm + " | Steps: " + step + " | Latitude: " + latitude + " | Longitude: " + longitude + " | Altitude: " + altitude);
    }

    public List<LatLng> getList(){
        return markers;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check if the user clicked the back button in the top left corner
        if (item.getItemId() == android.R.id.home) {

            //Stops the timer to ensure the database does not keep getting filled
           stopTimer();
            // Return home
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Do something when the user clicks the back button on the bottom left
        //Stops the timer to ensure the database does not keep getting filled
        stopTimer();
        // Return home
        finish();
    }

}