package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
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

    private String name, surname, email, gender, Date_of_birth, Weight, Height;

    private EditText Gender, date_of_birth, weight, height;

    private List<UserInfo> user_queue;

    private UserInfo user_info;

    private FirebaseUser new_user;

    private FirebaseAuth mAuth;

    private DatabaseReference reference;

    private AppCompatButton Button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mAuth = FirebaseAuth.getInstance();
        setupUI();
        onClickListeners();
    }

    private void setupUI() {
        Gender = findViewById(R.id.gender);
        date_of_birth = findViewById(R.id.dob);
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        Button = findViewById(R.id.confirmButton);
        new_user = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users/" + mAuth.getUid().toString());
    }

    private void onClickListeners() {
        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gender = Gender.getText().toString();
                Date_of_birth = date_of_birth.getText().toString();
                Weight = weight.getText().toString();
                Height = height.getText().toString();

                if (gender.isEmpty() || Date_of_birth.isEmpty() || Weight.isEmpty() || Height.isEmpty()) {
                    if (gender.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Gender: Male or Female", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Date_of_birth.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Date of Birth", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Weight.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Weight", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Height.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Height", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (!gender.isEmpty() && !Date_of_birth.isEmpty() && !Weight.isEmpty() && !Height.isEmpty()) {
                    if ((gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female")) &&
                            Date_of_birth.length() == 10 && Date_of_birth.contains("/") && Weight.length() <= 3 &&  Weight.length() >= 2
                            && Height.length() <=3 && Height.length() >=2) {
                        reference.child("gender").setValue(gender);
                        reference.child("data_of_birth").setValue(Date_of_birth);
                        reference.child("height").setValue(Height);
                        reference.child("weight").setValue(Weight);

                        Toast.makeText(view.getContext(),"Your profile is complete",Toast.LENGTH_SHORT).show();
                        startActivity(LoginPage.class);
                    }

                    if (!(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
                        Toast.makeText(view.getContext(), "Gender must be either Male or Female.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!(Date_of_birth.length() == 10 && Date_of_birth.contains("/"))){
                        Toast.makeText(view.getContext(), "Date of birth must be of format DD/MM/YYYY.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!(Weight.length() <= 3 &&  Weight.length() >= 2 )){
                        Toast.makeText(view.getContext(), "Weight is not valid, please enter a 2-digit or a " +
                                "3-digit number for your weight in kilograms.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!(Height.length() <= 3 &&  Height.length() >= 2 )){
                        Toast.makeText(view.getContext(), "Height is not valid, please enter a 2-digit or a " +
                                "3-digit number for your height in centimeters.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            }
        });
    }

    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
}