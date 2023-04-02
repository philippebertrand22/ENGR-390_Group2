package com.example.ui_code;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {

    //root reference
    private DatabaseReference rootRef;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    public int pulse;


    public FirebaseHelper() {
        rootRef = FirebaseDatabase.getInstance().getReference("Pulse Sensor/");
        db = FirebaseDatabase.getInstance();
        ref = db.getReference();
    }

    public void readPulse() {
        ref.child("pulse").addValueEventListener(new ValueEventListener() {

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pulse = dataSnapshot.getValue(int.class);
                int bpm = pulse / (60*1000);
                Log.d("FirebaseHelper", "Pulse: " + pulse + "  BPM: " + bpm);

            }

            /**
             * This method reads data from Firebase, converts it to BPM
             * and returns it to the caller
             * @return
             * String containing BPM value
             */

            // Calculate Beats Per Minute. BPM = (1.0 / PulseInterval) / (60.0 * 1000);
            // Missing the interval, which is the App running time
            public String getBPM() {

                // Return BPM as a String
                int bpm = pulse / (60*1000);
                return String.valueOf(bpm);
            }
        });
    }

}

/**
 * This method takes an int representing the raw pulse data
 * and converts it to BPM
**/