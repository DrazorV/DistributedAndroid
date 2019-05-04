package com.example.distributedandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ArrayList<Topic> topics;
    private volatile static HashMap<String, ArrayList<Topic>> hashed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("busLinesNew.txt");
            topics = BroUtilities.CreateBusLines(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.9940527,23.7324592),13f));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        mMap.setMyLocationEnabled(true);
    }

    public void onMapSearch(View view) throws InterruptedException {
        EditText locationSearch = findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        final String[] broker = {null};

        Thread t = new Thread(() -> hashed = BroUtilities.MD5(topics));
        t.start();
        t.join();
        Thread t2 = new Thread( () -> {
            for (Topic topic : topics) {
                if (topic.getLineId().equals(location.trim())) {
                    for (Map.Entry<String, ArrayList<Topic>> hash : hashed.entrySet()) {
                        for (Topic topic1 : hash.getValue()) {
                            if (topic1.getLineId().equals(topic.getLineId()))
                                broker[0] = hash.getKey();
                        }
                    }
                }
            }
        });
        t2.start();
        t2.join();
        int port = 0;
        if (broker[0].equals("BrokerA")) port = 4322;
        if (broker[0].equals("BrokerB")) port = 5432;
        if (broker[0].equals("BrokerC")) port = 7654;
        int finalPort = port;
        Thread thread = new Thread( () ->{
            try (Socket clientSocket = new Socket("10.0.2.2", finalPort)) {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.writeObject("Consumer");
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                outToServer.println(location);
                ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
                HashMap<String, Value> answer = (HashMap<String, Value>) inFromServer.readObject();
                runOnUiThread(() -> {
                    if(answer == null){
                        Toast toast = Toast.makeText(MapsActivity.this,"We couldn't find what you asked for, we may experience a problem with our servers",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }else{
                        for (Map.Entry<String, Value> answer_1: answer.entrySet()){
                            MarkerOptions m = new MarkerOptions()
                                    .position(new LatLng(answer_1.getValue().getLatitude(),answer_1.getValue().getLongitude()))
                                    .snippet(answer_1.getValue().getBus().getTime().toLocaleString())
                                    .title(answer_1.getValue().getBus().getLineName() + " [" + answer_1.getValue().getBus().getBuslineId()+ "]")
                                    .infoWindowAnchor(1,1);
                            mMap.addMarker(m);
                        }
                    }
                });
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
