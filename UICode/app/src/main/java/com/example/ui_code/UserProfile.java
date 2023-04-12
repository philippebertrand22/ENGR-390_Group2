package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserProfile extends AppCompatActivity {

    private String Gender;

    private EditText gender, age, weight, height;

    private FirebaseUser new_user;

    private FirebaseAuth mAuth;

    private DatabaseReference reference;

    private AppCompatButton confirm;
    private boolean checkAge, checkGender, checkWeight, checkHeight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mAuth = FirebaseAuth.getInstance();
        setupUI();
        onClickListeners();
    }

    //function to generate UI and fetch data from database
    private void setupUI() {
        gender = findViewById(R.id.gender);
        age = findViewById(R.id.age);
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        confirm = findViewById(R.id.confirmButton);
        new_user = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users/" + mAuth.getUid().toString());
    }

    //this function check if the information entered by the user is correct
    private void onClickListeners() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Gender = gender.getText().toString().toLowerCase();
                    gender.setText(Gender);

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

                    if(Gender.equalsIgnoreCase("male") || Gender.equalsIgnoreCase("female")){
                        checkGender = true;
                        gender.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                    }
                    else{
                        checkGender = false;
                        gender.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                    }

                    if(checkAge && checkHeight && checkGender && checkWeight){
                        reference.child("gender").setValue(gender.getText().toString());
                        reference.child("age").setValue(age.getText().toString());
                        reference.child("height").setValue(height.getText().toString());
                        reference.child("weight").setValue(weight.getText().toString());

                        startActivity(LoginPage.class);
                    }
            }
        });
    }
    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
}