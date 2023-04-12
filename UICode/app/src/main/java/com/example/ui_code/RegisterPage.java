package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterPage extends AppCompatActivity {

    private AppCompatButton registerButton;

    private String Surname, Name, Email, Password;

    private EditText surname, name, email, password;

    private TextView login_link;

    public UserInfo new_user;

    private DatabaseReference users_info_Reference;

    private FirebaseAuth mAuth;

    boolean checkEmail, checkPass, checkName, checkSurname = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        mAuth = FirebaseAuth.getInstance();
        users_info_Reference = FirebaseDatabase.getInstance().getReference("Users/");
        setupUI();
        onClickListeners();
    }

    //generates the UI
    private void setupUI(){
        surname = findViewById(R.id.surname);
        name = findViewById(R.id.name4);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.button);
        login_link = findViewById(R.id.textView7);
    }

    //this function check if the information entered by the user is correct
    private void onClickListeners() {
        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(LoginPage.class);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Surname = surname.getText().toString();
                Name = name.getText().toString();
                Email = email.getText().toString();
                Password = password.getText().toString();

                    if(Name.length() < 1){
                        checkName = false;
                        name.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                    }
                    else{
                        checkName = true;
                        name.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                    }

                    if(Surname.length() < 1){
                        checkSurname = false;
                        surname.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                    }
                    else{
                        checkSurname = true;
                        surname.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                    }

                    if(Password.length() < 6){
                        checkPass = false;
                        password.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                    }
                    else{
                        checkPass = true;
                        password.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                    }

                    if(Email.contains(".com") && Email.contains("@")){
                        checkEmail = true;
                        email.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null), PorterDuff.Mode.SRC_ATOP); //normal
                    }
                    else{
                        checkEmail = false;
                        email.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                    }

                    if(checkEmail == true && checkName == true && checkSurname == true && checkPass == true){
                        System.out.println("Surname:" + Surname);
                        System.out.println("Name:" + Name);
                        System.out.println("Email:" + Email);
                        System.out.println("Password:" + Password);

                        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    new_user = new UserInfo(Name, Surname, Email, Password);
                                    users_info_Reference.child(mAuth.getUid().toString()).setValue(new_user);
                                    startActivity(UserProfile.class);
                                } else {
                                    Toast.makeText(view.getContext(), "Email already exists, consider logging in", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

            }
        });
    }

    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }

}