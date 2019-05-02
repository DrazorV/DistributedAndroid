package com.example.distributedandroid;

import java.io.Serializable;

class Value implements Serializable {

    private Bus bus;
    private double latitude;
    private double longitude;

    Value(Bus bus, double latitude, double longitude){
        this.latitude = latitude;
        this.bus = bus;
        this.longitude = longitude;
    }

    double getLatitude(){
        return latitude;
    }

    double getLongitude(){
        return longitude;
    }

    Bus getBus() {
        return bus;
    }
}
