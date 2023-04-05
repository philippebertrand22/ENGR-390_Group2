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

import com.google.android.gms.maps.SupportMapFragment;
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
import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.GraphView;

import java.util.EventListener;
import java.util.ArrayList;

public class summaryActivity extends AppCompatActivity implements OnMapReadyCallback, EventListener {

    GoogleMap pathMap;
    TextView text, text8, resultTextView, heartbeat_text;
    Button  button;

    private DatabaseReference databasePulseReference;

    private Button HBEAT;

    GraphView graphView;
    private DatabaseReference databaseLocationReference, databaseWeightReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userKey;

    private double avg_hb;
    private int entry_count;

    ArrayList<Double> Heartbeats = new ArrayList<>();
    ArrayList<LatLng> Locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setupUI();
        findLatLng();
        CaloriesBurned();
        HeartBeat();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupUI() {
        text = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        text8 = findViewById(R.id.textView8);
        resultTextView = findViewById(R.id.resultCalBurned);
        heartbeat_text = findViewById(R.id.avg_heartbeat_id);
        HBEAT = findViewById(R.id.btn);
        graphView = findViewById(R.id.graph);
        databasePulseReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities/Activity_" + MainActivity.latestNodeId);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
        userKey = user.getUid(); // Userkey is unique to whoever logged in
        databaseWeightReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey);
    }
//This is a test
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        pathMap = map;
    }
    private void findLatLng() {
        databaseLocationReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities/Activity_" + MainActivity.latestNodeId);
        PolylineOptions path = new PolylineOptions();

        databaseLocationReference.child("entry_count").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    entry_count = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }
        });

        for(int n = 1; n <= entry_count; n++) {
            databaseLocationReference.child(String.valueOf(n)).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String data_string = dataSnapshot.getValue().toString();
                            double latitude = Double.parseDouble(data_string.substring(data_string.indexOf('[') + 1, data_string.indexOf(']')));
                            double longitude = Double.parseDouble(data_string.substring(data_string.indexOf('{') + 1, data_string.indexOf('}')));

                            LatLng location = new LatLng(latitude, longitude);
                            Locations.add(location);

//                            Latitudes.add(Double.parseDouble(data_string.substring(data_string.indexOf('[') + 1, data_string.indexOf(']'))));
//                            Longitudes.add(Double.parseDouble(data_string.substring(data_string.indexOf('{') + 1, data_string.indexOf('}'))));
                    }
                }
            });
        }

        path = path.color(Color.RED);
        path = path.width(8);

        for (int n = 0; n < Locations.size(); n++) {
            path = path.add(Locations.get(n));
        }


        //use these as example to show feature
//        path = path.add(new LatLng(45.3685642, -73.981979));
//        path = path.add(new LatLng(45.368895, -73.980917));
//        path = path.add(new LatLng(45.368567, -73.979796));
//        path = path.add(new LatLng(45.368005, -73.980864));
//        path = path.add(new LatLng(45.36758727681062, -73.98175357408874));
//        path = path.add(new LatLng(45.365683999463634, -73.98093281819594));
//        path = path.add(new LatLng(45.364221635949875, -73.97848664348376));
//        path = path.add(new LatLng(45.36518649859342, -73.97642670688406));
//        path = path.add(new LatLng(45.36601566427978, -73.97477446606969));
//        path = path.add(new LatLng(45.3676136584105, -73.9749246697801));
//        path = path.add(new LatLng(45.36880458694109, -73.97672711430485));
//        path = path.add(new LatLng(45.36915884565252, -73.9791518313441));
//        path = path.add(new LatLng(45.368601075614094, -73.97974728176744));

        if (path != null) {
//            Polyline polyline = pathMap.addPolyline(path);
//            pathMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.3685642, -73.981979), 17));
        }

        refreshFragment();

        PolylineOptions finalPath = path;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Polyline polyline = pathMap.addPolyline(finalPath);
               // pathMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Locations.get(0), 15));
            }
        });
    }
    private void refreshFragment(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                mAuth.signOut();
                startActivity(LoginPage.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void CaloriesBurned(){
        databaseWeightReference.child("weight").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                    String output = dataSnapshot.getValue().toString();
                    double weight = Double.parseDouble(output);
                    double CaloriesBurned =  (11.6 * 3.5 * weight) / 200;
                    resultTextView.setText(String.valueOf(CaloriesBurned));
            }
        });
    }

    private void HeartBeat(){
      //  for(int n = 1; n <= entry_count; n++) {
            databasePulseReference.child(String.valueOf(1)).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String x_value = snapshot.getValue().toString();
                        double heartbeat = Double.parseDouble(x_value.substring(x_value.indexOf('!') + 1, x_value.indexOf('?')));
                        heartbeat_text.setText(String.valueOf(heartbeat));
                        Heartbeats.add(heartbeat);
                    }
                }
            });
       // }
        double sum = 0;
        for(int n = 0; n < Heartbeats.size(); n++) {
            sum += Heartbeats.get(n);
        }
        avg_hb = sum / (entry_count * 4);
        //heartbeart.setText(String.valueOf(avg_hb));
    }
}