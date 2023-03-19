package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
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

import java.util.LinkedList;
import java.util.List;

public class RegisterPage extends AppCompatActivity {

    private AppCompatButton Button;

    private String surname, name,email, password;

    private EditText Surname, Name, Email, Password;

    private TextView login_link;

    public UserInfo new_user;

    private DatabaseReference users_info_Reference;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        mAuth = FirebaseAuth.getInstance();
        users_info_Reference = FirebaseDatabase.getInstance().getReference("Users/");
        setupUI();
        onClickListeners();
    }

    private void setupUI(){
        Surname = findViewById(R.id.surname);
        Name = findViewById(R.id.name4);
        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Button = findViewById(R.id.button);
        login_link = findViewById(R.id.textView7);
    }

    private void onClickListeners() {
        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(LoginPage.class);
            }
        });

        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surname = Surname.getText().toString();
                name = Name.getText().toString();
                email = Email.getText().toString();
                password = Password.getText().toString();

                if (surname.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    if (surname.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Surname", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (name.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (email.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (!email.isEmpty() && !surname.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
                    if (email.contains(".com") && password.length() >= 6) {
                        System.out.println("Surname:" + surname);
                        System.out.println("Name:" + name);
                        System.out.println("Email:" + email);
                        System.out.println("Password:" + password);

                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    new_user = new UserInfo(name,surname,email,password);
                                    users_info_Reference.child(mAuth.getUid().toString()).setValue(new_user);
                                    Toast.makeText(view.getContext(), "User Successfully Added", Toast.LENGTH_SHORT).show();
                                    startActivity(UserProfile.class);
                                } else {
                                    Toast.makeText(view.getContext(), "Email already exists, consider loging in", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    if (!email.contains(".com")) {
                            Toast.makeText(view.getContext(), "Email Address is not valid", Toast.LENGTH_SHORT).show();
                            return;
                    }
                    if (!(password.length() >= 6)) {
                            Toast.makeText(view.getContext(), "Password must contain 6 characters at least.", Toast.LENGTH_SHORT).show();
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