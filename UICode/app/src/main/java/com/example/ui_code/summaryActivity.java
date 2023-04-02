package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class summaryActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    TextView text;
    private MainActivity main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        text = findViewById(R.id.textView);

        main = new MainActivity();
        List<LatLng> markers = main.getList();

//        if(markers.size() == 0){
//            System.out.println("THE ARRAY LIST IS EMPTY");
//        }
//        else {
//            System.out.println(markers.get(0));
//        }
    }
//This is a test
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        //loop to create path
        for(int n = 0;n < main.markers.size();n++) {
            Polyline line = map.addPolyline(new PolylineOptions().add(main.markers.get(n), main.markers.get(n + 1)).width(5).color(Color.RED));
        }
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
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("weight");

    public void CaloriesBurned(DataSnapshot dataSnapshot){
        int weight = dataSnapshot.child("weight").getValue(Integer.class);

        double CaloriesBurned =  ((11.6 * 3.5 * Float.parseFloat(String.valueOf(weight))) / 200);

        TextView resultTextView = findViewById(R.id.resultCalBurned);
        resultTextView.setText(String.valueOf(CaloriesBurned));
    }

}