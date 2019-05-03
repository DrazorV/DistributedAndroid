package com.example.distributedandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.concurrent.atomic.AtomicReference;


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

    public void onMapSearch(View view){
        EditText locationSearch = findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        String broker = null;

        Thread t = new Thread(() -> hashed = BroUtilities.MD5(topics));
        t.start();
        try {
            t.join();
            for(Topic topic : topics)
                if(topic.getLineId().equals(location.trim()))
                    for(Map.Entry<String, ArrayList<Topic>> hash : hashed.entrySet())
                        for(Topic topic1 : hash.getValue())
                            if(topic1.getLineId().equals(topic.getLineId())) broker = hash.getKey();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int port = 0;
        if (broker.equals("BrokerA")) port = 4322;
        if (broker.equals("BrokerB")) port = 5432;
        if (broker.equals("BrokerC")) port = 7654;
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
                    for (Map.Entry<String, Value> answer_1: answer.entrySet()){
                        MarkerOptions m = new MarkerOptions()
                                .position(new LatLng(answer_1.getValue().getLatitude(),answer_1.getValue().getLongitude()))
                                .snippet(answer_1.getValue().getBus().getTime().toLocaleString())
                                .title(answer_1.getValue().getBus().getLineName() + " [" + answer_1.getValue().getBus().getBuslineId()+ "]")
                                .infoWindowAnchor(1,1)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                        mMap.addMarker(m);
                    }
                });

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
