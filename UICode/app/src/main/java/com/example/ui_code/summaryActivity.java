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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ValueEventListener;

import java.util.EventListener;
import java.util.ArrayList;

public class summaryActivity extends AppCompatActivity implements OnMapReadyCallback, EventListener {

    GoogleMap pathMap;
    TextView text, text8, resultTextView, heartbeat_text, stepsTextView, distanceTextView;
    Button  button;
    private DatabaseReference databaseUserReference, databaseWeightReference, databaseStepsReference, databasePulseReference, currentActivityReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userKey;
    private double avg_hb;
    private int entry_count;

    private long totalEntries;

 //   private ArrayList<String> dateArray, timeArray, runningTimeArray, heartBeatArray, stepsArray, latitudeArray, longitudeArray, distanceArray;

    ArrayList<Double> Heartbeats = new ArrayList<>();
    ArrayList<LatLng> Locations = new ArrayList<>();

   ArrayList<String> dateArray = new ArrayList<>();
   ArrayList<String> timeArray = new ArrayList<>();
   ArrayList<String> runningTimeArray = new ArrayList<>();
   ArrayList<String> heartBeatArray = new ArrayList<>();
   ArrayList<String> stepsArray = new ArrayList<>();
   ArrayList<String> latitudeArray = new ArrayList<>();
   ArrayList<String> longitudeArray = new ArrayList<>();
   ArrayList<String> distanceArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        setupUI();
        displayData();
        //findLatLng();
        //CaloriesBurned();
        //HeartBeat();
        //TotalSteps();
        //TotalDistance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //function to generate UI and fetch data from database
    private void setupUI() {
        totalEntries = 0; // Stores the number of entries in the current activity

        text = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        text8 = findViewById(R.id.textView8);
        resultTextView = findViewById(R.id.resultCalBurned);
        heartbeat_text = findViewById(R.id.avg_heartbeat_id);
        stepsTextView = findViewById(R.id.resultTotalSteps);
        distanceTextView = findViewById(R.id.resultTotalDistance);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); // Use this to get any user info from the database
        userKey = user.getUid(); // Userkey is unique to whoever logged in

        currentActivityReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities/Activity_" + MainActivity.latestNodeId);

        databasePulseReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities/Activity_" + MainActivity.latestNodeId);
        databaseStepsReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration");
        databaseWeightReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey);

        getData(currentActivityReference, new OnDataRetrievedListener() {
            //Call getDate. Gets a string array
            //Then call parseData to split it into individual arrays
            @Override
            public void onDataRetrieved(ArrayList<String> data) {
                parseData(data);
            }
        });


    }

//This is a test
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        pathMap = map;
    }


// Interface to handle retrieved data
    public interface OnDataRetrievedListener {
        void onDataRetrieved(ArrayList<String> data);
    }

    private void getData(DatabaseReference databaseReference, final OnDataRetrievedListener listener) {
        final ArrayList<String> data = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String key = childSnapshot.getKey();
                        Object value = childSnapshot.getValue();
                        if (key != null && key.equals("entry_count") && value instanceof Long) {
                            totalEntries = (Long) value;
                            System.out.println("Total Entries: " + totalEntries);
                        } else {
                            // Assuming each child entry contains a string value
                            String stringValue = childSnapshot.getValue(String.class);
                            System.out.println("Entry: " + stringValue);
                            if (stringValue != null) {
                                data.add(stringValue);
                            }
                        }
                    }
                    // Call the listener with the retrieved data
                    listener.onDataRetrieved(data);
                } else {
                    // Handle case where there are no child entries
                    System.out.println("No child entries found in dataSnapshot");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurred during data retrieval
                System.out.println("Error: " + databaseError.getMessage());
            }
        });
    }

    private void parseData(ArrayList<String> data) {

        for (String entry : data) {
            String[] fields = entry.split(" \\| ");
            dateArray.add(fields[0].substring(6)); // remove "Date: " prefix
            timeArray.add(fields[1].substring(6)); // remove "Time: " prefix
            runningTimeArray.add(fields[2].substring(14)); // remove "Running Time: " prefix
            heartBeatArray.add(fields[3].substring(11)); // remove "HeartBeat: " prefix
            stepsArray.add(fields[4].substring(7)); // remove "Steps: " prefix
            latitudeArray.add(fields[5].substring(11)); // remove "Latitude: " prefix
            longitudeArray.add(fields[6].substring(12)); // remove "Longitude: " prefix
            distanceArray.add(fields[7].substring(10)); // remove "Distance: " prefix


        }

        // Step
        if (stepsArray != null && stepsArray.size() > 0) {
            int lastIndex = stepsArray.size() - 1;
            String lastStep = stepsArray.get(lastIndex);
            stepsTextView.setText("Total Steps: " + lastStep);
        }

        // Distance
        if (distanceArray != null && distanceArray.size() > 0) {
            int lastIndex = distanceArray.size() - 1;
            String lastStep = distanceArray.get(lastIndex);
            distanceTextView.setText("Total Distance: " + lastStep);
        }

        // Distance
        if (distanceArray != null && distanceArray.size() > 0) {
            int lastIndex = distanceArray.size() - 1;
            String lastStep = distanceArray.get(lastIndex);
            distanceTextView.setText("Total Distance: " + lastStep);
        }

        resultTextView.setVisibility(View.GONE);
        heartbeat_text.setVisibility(View.GONE);
        text.setVisibility(View.GONE);



    }

    private void displayData(){
    }


    private void findLatLng() {
        databaseUserReference = FirebaseDatabase.getInstance().getReference("Users/" + userKey + "/Activities/Activity_" + MainActivity.latestNodeId);
        PolylineOptions path = new PolylineOptions();

        databaseUserReference.child("entry_count").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    entry_count = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }
        });

        for(int n = 1; n <= entry_count; n++) {
            databaseUserReference.child(String.valueOf(n)).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
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

        //hardcoded latitudes and longitudes to generate a line on the map
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
                    resultTextView.setText("Calories burned: " + String.valueOf(CaloriesBurned));
            }
        });
    }

    private void HeartBeat(){
        for(int n = 1; n <= entry_count; n++) {
            databasePulseReference.child(String.valueOf(n)).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
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
        }
        double sum = 0;
        for(int n = 0; n < Heartbeats.size(); n++) {
            sum += Heartbeats.get(n);
        }
        avg_hb = sum / (entry_count * 4);
        //heartbeart.setText(String.valueOf(avg_hb));
    }

    public void TotalSteps(){
        databaseStepsReference.child("Step").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String totalSteps = dataSnapshot.getValue().toString();
                stepsTextView.setText("Total Steps: " + totalSteps);
            }
        });
    }

    public void TotalDistance(){
        MainActivity main = new MainActivity();
        distanceTextView.setText("Total Distance: " + main.distance);
    }
}