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
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private AppCompatButton Button;

    private String Email, Password;

    private EditText email, password;

    private TextView register_link, run, master;

    private UserInfo user;

    private FirebaseAuth mAuth;
    boolean checkPass, checkEmail = false;

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
        email = findViewById(R.id.email3);
        password = findViewById(R.id.password);
        Button = findViewById(R.id.button3);
        register_link = findViewById(R.id.textView12);
        run = findViewById(R.id.textView2);
        master = findViewById(R.id.textView3);
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
                Email = email.getText().toString();
                Password = password.getText().toString();


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


                if(checkEmail == true && checkPass == true){
                    System.out.println("Email:" + Email);
                    System.out.println("Password:" + Password);

                    mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                System.out.println("Email " + user.getEmail());
                                System.out.println("ID " + user.getUid());
                                startActivity(homeActivity.class);
                            }
                            else{
                                checkEmail = false;
                                email.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                                checkPass = false;
                                password.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP); //red
                                Toast.makeText(view.getContext(), "Email or Password is Incorrect", Toast.LENGTH_LONG).show();
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