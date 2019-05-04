package com.example.distributedandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.*;
import java.net.Socket;
import java.util.*;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ArrayList<Topic> topics;
    private volatile static HashMap<String, ArrayList<Topic>> hashed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        InputStream inputStream = null;
        AssetManager assetManager = getAssets();
        try {
            inputStream = assetManager.open("busLinesNew.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream finalInputStream = inputStream;
        new Thread( () ->  {
            topics = BroUtilities.CreateBusLines(finalInputStream);
            hashed = BroUtilities.MD5(topics);
        }).start();
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
        mMap.clear();
        EditText locationSearch = findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        final int[] port = {0};
        final String[] broker = {null};
        broker[0] = "brokerErr";
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ignored) {

        }
        Thread t2 = new Thread( () -> {
            for (Topic topic : topics) {
                if (topic.getLineId().equals(location.trim())) {
                    for (Map.Entry<String, ArrayList<Topic>> hash : hashed.entrySet()) {
                        for (Topic topic1 : hash.getValue()) {
                            if (topic1.getLineId().equals(topic.getLineId())){
                                broker[0] = hash.getKey();
                            }
                        }
                    }
                }
            }
            if (broker[0].equals("BrokerA")) port[0] = 4322;
            if (broker[0].equals("BrokerB")) port[0] = 5432;
            if (broker[0].equals("BrokerC")) port[0] = 7654;
        });
        t2.start();
        t2.join();
        if(port[0] != 0){
            new Thread( () ->{
                try (Socket clientSocket = new Socket("10.0.2.2", port[0])) {
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    out.writeObject("Consumer");
                    PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                    outToServer.println(location);
                    ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
                    HashMap<String, Value> answer = (HashMap<String, Value>) inFromServer.readObject();
                    if(answer == null) showToast("We couldn't find what you asked for, we may experience a problem with our servers");
                    else{
                        final LatLng[] latLng = new LatLng[1];
                        runOnUiThread(new Thread(() -> {
                            for (Map.Entry<String, Value> answer_1: answer.entrySet()){
                                MarkerOptions m = new MarkerOptions()
                                        .position(new LatLng(answer_1.getValue().getLatitude(),answer_1.getValue().getLongitude()))
                                        .snippet(answer_1.getValue().getBus().getTime().toLocaleString())
                                        .title(answer_1.getValue().getBus().getLineName() + " [" + answer_1.getValue().getBus().getBuslineId()+ "]")
                                        .infoWindowAnchor(1,1);
                                mMap.addMarker(m);
                                latLng[0] = new LatLng(answer_1.getValue().getLatitude(),answer_1.getValue().getLongitude());
                            }
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng[0],13f));
                        }));
                    }
                    showToast("Choose the preferred bus for more info!");
                } catch (IOException | ClassNotFoundException e) {
                    showToast("Couldn't establish connection with server!");
                }
            }).start();
        }else showToast("The bus line you typed doesn't exist.");
    }


    public void showToast(final String toast) {
        runOnUiThread(() -> {
            Toast t = Toast.makeText(MapsActivity.this, toast, Toast.LENGTH_SHORT);
//            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        });
    }
}
