package com.example.ui_code;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;

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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        //loop to create path
        for(int n = 0;n < main.markers.size();n++) {
            Polyline line = map.addPolyline(new PolylineOptions().add(main.markers.get(n), main.markers.get(n + 1)).width(5).color(Color.RED));
        }
    }
}