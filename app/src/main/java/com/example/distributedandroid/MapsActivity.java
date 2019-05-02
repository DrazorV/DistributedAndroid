package com.example.distributedandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Address;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ArrayList<Topic> topics;
    private volatile static HashMap<String, ArrayList<Topic>> hashed;
    private static Scanner input = new Scanner(System.in);

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
        LatLng sydney = new LatLng(27.746974, 85.301582);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Kathmandu, Nepal"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    public void onMapSearch(View view){
        EditText locationSearch = findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        final boolean[] temp = {true};
        String broker = null;
        Thread t = new Thread(() -> hashed = BroUtilities.MD5(topics));
        t.start();
        try {
            t.join();
            for(Topic topic : topics) {
                if(topic.getLineId().equals(location.trim())) {
                    for(Map.Entry<String, ArrayList<Topic>> hash : hashed.entrySet()){
                        for(Topic topic1 : hash.getValue()){
                            if(topic1.getLineId().equals(topic.getLineId())) {
                                broker = hash.getKey();
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(broker);

        int port = 0;
        if (broker.equals("BrokerA")) port = 4322;
        if (broker.equals("BrokerB")) port = 5432;
        if (broker.equals("BrokerC")) port = 7654;

        int finalPort = port;
        new Thread(() ->{
            try (Socket clientSocket = new Socket("localhost", finalPort)) {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.writeObject("Consumer");
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line = inFromServer.readLine();

                while (temp[0]) {
                    PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);

                    System.out.println("Type the bus lines you re interested in or type 'bye' to change broker.");

                    String busline = input.nextLine();

                    if (!busline.toLowerCase().equals("bye")) {
                        outToServer.println(busline);
                        String answer = inFromServer.readLine();

                        while (!answer.equals("next")) {
                            System.out.println(answer);
                            answer = inFromServer.readLine();
                        }
                        line = inFromServer.readLine();
                    } else {
                        temp[0] = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        // location == busline
        // if busline exists get last spot from bus
//        if (location != null || !location.equals("")) {
//            Geocoder geocoder = new Geocoder(this);
//            try {
//                addressList = geocoder.getFromLocationName(location, 1);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println(addressList.toString());
//            //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
//            //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//        }
    }


}
