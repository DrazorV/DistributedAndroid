package com.example.DistributedSystems;
import java.io.Serializable;

public class Topic implements Serializable {
    private String busLine;

    Topic(String LineId){
        this.busLine = LineId;
    }

    public String getLineId() {
        return busLine;
    }
}

