package com.example.ui_code;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.EventListener;

public class GraphPage extends AppCompatActivity implements EventListener {

    private DatabaseReference databasePulseReference,databaseStepsReference;

    private Button button;
    private GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_page);
        button = findViewById(R.id.btn);
        graph= findViewById(R.id.graph);
        databasePulseReference = FirebaseDatabase.getInstance().getReference("Pulse Sensor/");
        databaseStepsReference = FirebaseDatabase.getInstance().getReference("AccelerometerData/Acceleration");
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupUI();
    }

    private void setupUI(){
        GraphPointValue pointValue = new GraphPointValue();

        databasePulseReference.child("State").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String x_value = snapshot.getValue().toString();
                    pointValue.setxValue(Integer.parseInt(x_value));
                }
            }
        });

        databaseStepsReference.child("Step").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String y_value = snapshot.getValue().toString();
                    pointValue.setyValue(Integer.parseInt(y_value));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        DataPoint dataPoint = new DataPoint(pointValue.getxValue(),pointValue.getyValue());
//
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>( new DataPoint[]{
//            dataPoint
//        });
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//
//        graph.addSeries(series);
    }
}
//This ia  test