package com.example.DistributedSystems;
import java.io.Serializable;

class Topic implements Serializable {
    private String busLine;

    Topic(String LineId){
        this.busLine = LineId;
    }

    String getLineId() {
        return busLine;
    }
}

