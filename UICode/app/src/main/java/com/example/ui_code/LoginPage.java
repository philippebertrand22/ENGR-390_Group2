package com.example.ui_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private AppCompatButton Button;

    private String email, password;

    private EditText Email, Password;

    private TextView register_link;

    private UserInfo user;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();
        setupUI();
        onClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setupUI(){
        Email = findViewById(R.id.email3);
        Password = findViewById(R.id.password5);
        Button = findViewById(R.id.button3);
        register_link = findViewById(R.id.textView12);
    }

    private void onClickListeners() {
        register_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(RegisterPage.class);
            }
        });

        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = Email.getText().toString();
                password = Password.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    if (email.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(view.getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (!email.isEmpty()  && !password.isEmpty()) {
                    if (email.contains("@") && email.contains(".com") && password.length() >= 6) {
                        System.out.println("Email:" + email);
                        System.out.println("Password:" + password);

                        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                 if(task.isSuccessful()){
                                     FirebaseUser user = mAuth.getCurrentUser();
                                     System.out.println("Email " + user.getEmail());
                                     System.out.println("ID " + user.getUid());
                                     Toast.makeText(view.getContext(), "Login Success", Toast.LENGTH_SHORT).show();
                                     startActivity(homeActivity.class);
                                 }
                            }
                        });

                    }
                    if (!(email.contains("@") && email.contains(".com"))) {
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