package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ImageView stopButton, playButton, pauseButton;

    private TextView timeValue, timeText, distanceValue, distanceText, speedValue, speedText, BPMValue, BPMText;

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
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add here whatever happens when you click the stop button
                timeValue.setText(formatTime(0,0,0));

                startActivity(summaryActivity.class);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
                //add here whatever happens when you click the pause button
                startTimer();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopButton.setVisibility(View.GONE);
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                //add here whatever happens when you click the play button
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
    }

}