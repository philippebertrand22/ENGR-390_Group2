package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class summaryActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap pathMap;
    TextView text, text1;

    Button  button;

    private DatabaseReference databaseLocationReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userKey;

    private LatLng here;
    private double latitude, longitude;

    private int entry_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
       setupUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void setupUI() {
        text = findViewById(R.id.textView);
        text1 = findViewById(R.id.textView1);
        button = findViewById(R.id.button);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
        userKey = user.getUid(); // Userkey is unique to whoever logged in
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        pathMap = map;
        findLatLng();
    }

    private void findLatLng() {
        databaseLocationReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities/Activity_" + MainActivity.latestNodeId);
        PolylineOptions path = new PolylineOptions().width(8).color(Color.RED);

        databaseLocationReference.child("entry_count").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    entry_count = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }
        });

 //       for(int n = 1; n <= entry_count; n++) {
        int n = 1;
            databaseLocationReference.child(String.valueOf(n)).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String data_string = dataSnapshot.getValue().toString();
                        latitude = Double.parseDouble(data_string.substring(data_string.indexOf('[') + 1, data_string.indexOf(']')));
                        longitude = Double.parseDouble(data_string.substring(data_string.indexOf('{') + 1, data_string.indexOf('}')));
                        text.setText(String.valueOf(longitude));
                        path.add(new LatLng(latitude, longitude));
                    }
                }
            });
 //       }
        path.add(new LatLng(0, 0));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Polyline polyline = pathMap.addPolyline(path);
                pathMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                Toast.makeText(view.getContext(), "ADDING PATH", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startActivity(Class<?> destinationActivity) {
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.dropdown_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // go to settings
                startActivity(ChangeInfoActivity.class);
                return true;
            case R.id.logout:
                // go to logout
                startActivity(LoginPage.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}