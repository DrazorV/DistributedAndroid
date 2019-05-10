package com.example.DistributedSystems;

import java.io.Serializable;

public class Value implements Serializable {

    private Bus bus;
    private double latitude;
    private double longitude;

    Value(Bus bus, double latitude, double longitude){
        this.latitude = latitude;
        this.bus = bus;
        this.longitude = longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public Bus getBus() {
        return bus;
    }
}
