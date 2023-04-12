package com.example.ui_code;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeInfoActivity extends AppCompatActivity {
    private String gender, userKey;
    private EditText genderEditText, age, weight, height;
    private Button confirm, edit;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private boolean checkAge, checkGender, checkWeight, checkHeight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);
        setupUI();
        confirm.setVisibility(View.GONE);
        onClickListeners();
    }

    //function to generate UI and fetch data from database
    private void setupUI() {
        genderEditText = findViewById(R.id.gender);
        age = findViewById(R.id.age);
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
                genderEditText.setInputType(0);
            }
        });

        reference.child("age").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                age.setText((String) dataSnapshot.getValue());
                age.setInputType(0);
            }
        });

        reference.child("height").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                height.setText((String) dataSnapshot.getValue());
                height.setInputType(0);
            }
        });

        reference.child("weight").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                weight.setText((String) dataSnapshot.getValue());
                weight.setInputType(0);
            }
        });
    }

    //this function check if the information entered by the user is correct
    private void onClickListeners() {

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setVisibility(View.GONE);
                confirm.setVisibility(View.VISIBLE);
                genderEditText.setInputType(1);
                age.setInputType(1);
                height.setInputType(1);
                weight.setInputType(1);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gender = genderEditText.getText().toString().toLowerCase();
                genderEditText.setText(gender);

                if(age.length() != 2){
                    checkAge = false;
                    age.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                }
                else{
                    checkAge = true;
                    age.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                }

                if(weight.length() <= 3 && weight.length() >= 2){
                    checkWeight = true;
                    weight.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                }
                else{
                    checkWeight = false;
                    weight.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                }

                if(height.length() <= 3 && height.length() >= 2){
                    checkHeight = true;
                    height.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                }
                else{
                    checkHeight = false;
                    height.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                }

                if(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female")){
                    checkGender = true;
                    genderEditText.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                }
                else{
                    checkGender = false;
                    genderEditText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                }


                if(checkAge == true && checkHeight == true && checkGender == true && checkWeight == true){
                    reference.child("gender").setValue(genderEditText.getText().toString());
                    reference.child("age").setValue(age.getText().toString());
                    reference.child("height").setValue(height.getText().toString());
                    reference.child("weight").setValue(weight.getText().toString());

                    edit.setVisibility(View.VISIBLE);
                    confirm.setVisibility(View.GONE);
                    genderEditText.setInputType(0);
                    age.setInputType(0);
                    height.setInputType(0);
                    weight.setInputType(0);
                }

            }
        });
    }
    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
}