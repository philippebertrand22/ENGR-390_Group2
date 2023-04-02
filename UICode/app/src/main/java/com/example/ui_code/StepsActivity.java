package com.example.ui_code;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StepsActivity extends AppCompatActivity {

    private TextView accx, accy, accz, magx, magy, magz, gyrox, gyroy, gyroz;

    private DatabaseReference databaseAccelReference, databaseGyroReference, databaseMagReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        setupUI();
    }

    private void setupUI() {
        accx = findViewById(R.id.AccelX);
        accy = findViewById(R.id.AccelY);
        accz = findViewById(R.id.AccelZ);

        gyrox = findViewById(R.id.gyroX);
        gyroy = findViewById(R.id.GyroY);
        gyroz = findViewById(R.id.GyroZ);

        magx = findViewById(R.id.MagX);
        magy = findViewById(R.id.MagY);
        magz = findViewById(R.id.MagZ);

        databaseAccelReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration/");
        databaseGyroReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Gyroscope/");
        databaseMagReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Mag/");

        databaseAccelReference.child("x").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String output = "Acceleration X : " + dataSnapshot.getValue().toString();
                accx.setText(output);
            }
        });

        databaseAccelReference.child("y").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String output = "Acceleration Y : " + dataSnapshot.getValue().toString();
                accy.setText(output);
            }
        });

        databaseAccelReference.child("z").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String output = "Acceleration Z : " + dataSnapshot.getValue().toString();
                accz.setText(output);
            }
        });

        databaseGyroReference.child("x").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Gyroscope X : " + dataSnapshot.getValue().toString();
                    gyrox.setText(output);
                }
            }
        });

        databaseGyroReference.child("y").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Gyroscope Y: " + dataSnapshot.getValue().toString();
                    gyroy.setText(output);
                }
            }
        });

        databaseGyroReference.child("z").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Gyroscope Z: " + dataSnapshot.getValue().toString();
                    gyroz.setText(output);
                }
            }
        });

        databaseMagReference.child("x").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Mag X: " + dataSnapshot.getValue().toString();
                    magx.setText(output);
                }
            }
        });

        databaseMagReference.child("y").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Mag Y: " + dataSnapshot.getValue().toString();
                    magy.setText(output);
                }
            }
        });

        databaseMagReference.child("z").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String output = "Mag Z: " + dataSnapshot.getValue().toString();
                    magz.setText(output);
                }
            }
        });
    }
}