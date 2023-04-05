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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.ArrayList;

public class summaryActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap pathMap;
    TextView text, text8;
    Button  button;
    
    private DatabaseReference databaseLocationReference, databaseWeightReference, databaseStepsReference, databaseDistanceReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userKey;

    private double latitude, longitude;
    private int entry_count;

    ArrayList<Double> Latitudes = new ArrayList<>();
    ArrayList<Double> Longitudes = new ArrayList<>();
    ArrayList<LatLng> Locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setupUI();
        findLatLng();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupUI() {
        text = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        text8 = findViewById(R.id.textView8);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
        userKey = user.getUid(); // Userkey is unique to whoever logged in
        databaseWeightReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey);
        databaseStepsReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey);
        databaseDistanceReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey);

    }
//This is a test
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        pathMap = map;
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


            databaseLocationReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            String data_string = snapshot.getValue().toString();
                            if(data_string.length() > 4) {
                                Latitudes.add(Double.parseDouble(data_string.substring(data_string.indexOf('[') + 1, data_string.indexOf(']'))));
                                Longitudes.add(Double.parseDouble(data_string.substring(data_string.indexOf('{') + 1, data_string.indexOf('}'))));
                            }
                        }
                    }
                }
            });
        for(int n = 0;n < Latitudes.size(); n++){
            LatLng location = new LatLng(Latitudes.get(n), Longitudes.get(n));
            Locations.add(location);
 //           path.add(Locations.get(n));
        }

        path.add(new LatLng(45.3685642, -73.981979));
        path.add(new LatLng(45.368895, -73.980917));
//        path.add(new LatLng(45.368567, -73.979796));
//        path.add(new LatLng(45.368005, -73.980864));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Polyline polyline = pathMap.addPolyline(path);
                pathMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.3685642, -73.981979), 17));
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
                mAuth.signOut();
                startActivity(LoginPage.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private DatabaseReference reference;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("weight");

    public void CaloriesBurned(DataSnapshot dataSnapshot){
        int weight = dataSnapshot.child("weight").getValue(Integer.class);

        double CaloriesBurned =  ((11.6 * 3.5 * Float.parseFloat(String.valueOf(weight))) / 200);

        TextView resultTextView = findViewById(R.id.resultCalBurned);
        resultTextView.setText("Calories Burned: " + String.valueOf(CaloriesBurned));
    }

    public void TotalSteps(){
        databaseStepsReference.child("Step").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                int totalSteps = dataSnapshot.child("Step").getValue(Integer.class);

                TextView resultTextView = findViewById(R.id.resultTotalSteps);
                resultTextView.setText("Total Steps: " + totalSteps);
            }
        });

    }

    public void TotalDistance(DataSnapshot dataSnapshot){
        Task<DataSnapshot> dataSnapshotTask = databaseDistanceReference.child("Distance").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                int totalDistance = dataSnapshot.child("Distance").getValue(Integer.class);
                TextView resultTextView = findViewById(R.id.resultTotalDistance);
                resultTextView.setText("Total Distance: " + totalDistance);
            }
        });
    };
}