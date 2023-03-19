package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    private DatabaseReference databaseGPSReference, databasePulseReference, databaseStepReference;

    boolean timerStarted = false;

    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer = new Timer();
        setupUI();
        onClickListeners();
        stopButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        startTimer();
        getData();
    }

    private void startTimer() {
            if(timerStarted == false){
                timerStarted = true;
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                time++;
                                timeValue.setText(getTimerText());
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

    private float step, longitude, latitude, altitude, date;

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

        databasePulseReference = FirebaseDatabase.getInstance().getReference("Pulse Sensor/");
        databaseStepReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration/");
        databaseGPSReference = FirebaseDatabase.getInstance().getReference("GPSData/");
    }

    private void getData(){

        databasePulseReference.child("State").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = dataSnapshot.getValue().toString();
                    BPMValue.setText(output);
                }
            }
        });

        databaseStepReference.child("Step").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = dataSnapshot.getValue().toString();
                    stepValue.setText(output);
                }
            }
        });

        databaseGPSReference.child("Latitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Latitude : " + dataSnapshot.getValue().toString();
                    // latitudeValue.setText(output);
                }
            }
        });

        databaseGPSReference.child("Longitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Longitude : " + dataSnapshot.getValue().toString();
                    //Longitude.setText(output);
                }
            }
        });

        databaseGPSReference.child("Altitude").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Altitude : " + dataSnapshot.getValue().toString();
                    //Altitude.setText(output);
                }
            }
        });

        databaseGPSReference.child("Date").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = dataSnapshot.getValue().toString();
                    //dateValue.setText(output);
                }
            }
        });

        databaseGPSReference.child("Time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String output = "Date : " + snapshot.getValue().toString();
                //Time.setText(output);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}