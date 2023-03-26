package com.example.ui_code;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeInfoActivity extends AppCompatActivity {
    private String name, surname, email, gender, Date_of_birth, Weight, Height, userKey;
    private EditText genderEditText, date_of_birth, weight, height;
    private Button confirm, edit;
    private FirebaseAuth mAuth;

    private FirebaseUser user;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);
        setupUI();
        confirm.setVisibility(View.GONE);
        onClickListeners();
    }

    private void setupUI() {
        genderEditText = findViewById(R.id.gender);
        date_of_birth = findViewById(R.id.dob);
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        confirm = findViewById(R.id.confirmButton);
        edit = findViewById(R.id.editInfo);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
        userKey = user.getUid(); // Userkey is unique to whoever logged in
        reference = FirebaseDatabase.getInstance().getReference("Users/" + userKey);

        reference.child("gender").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                genderEditText.setText((String) dataSnapshot.getValue());
                genderEditText.setEnabled(false);
            }
        });

        reference.child("data_of_birth").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                date_of_birth.setText((String) dataSnapshot.getValue());
                date_of_birth.setEnabled(false);
            }
        });

        reference.child("height").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                height.setText((String) dataSnapshot.getValue());
                height.setEnabled(false);
            }
        });

        reference.child("weight").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                weight.setText((String) dataSnapshot.getValue());
                weight.setEnabled(false);
            }
        });
    }

    private void onClickListeners() {

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.GONE);
                confirm.setVisibility(View.VISIBLE);
                genderEditText.setEnabled(true);
                date_of_birth.setEnabled(true);
                height.setEnabled(true);
                weight.setEnabled(true);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.GONE);
                genderEditText.setEnabled(false);
                date_of_birth.setEnabled(false);
                height.setEnabled(false);
                weight.setEnabled(false);

                reference.child("gender").setValue(genderEditText.getText().toString());
                reference.child("data_of_birth").setValue(date_of_birth.getText().toString());
                reference.child("height").setValue(height.getText().toString());
                reference.child("weight").setValue(weight.getText().toString());
            }
        });
    }
    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
}