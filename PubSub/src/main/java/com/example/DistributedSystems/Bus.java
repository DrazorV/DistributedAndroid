package com.example.DistributedSystems;
import java.io.Serializable;
import java.util.Date;

class Bus implements Serializable {
    private String lineName;
    private String buslineId;
    private String lineNumber;
    private String routeCode;
    private String vehicleId;
    private Date time;

    Bus(String lineNumber, String routeCode, String vehicleId,String lineName,String buslineId , Date time){
        this.lineNumber = lineNumber;
        this.routeCode = routeCode;
        this.time = time;
        this.vehicleId = vehicleId;
        this.lineName = lineName;
        this.buslineId = buslineId;
    }

    String getLineNumber(){
        return  lineNumber;
    }

    String getRouteCode(){
        return  routeCode;
    }

    String getVehicleId(){
        return  vehicleId;
    }

    Date getTime(){
        return time;
    }

    String getBuslineId() {
        return buslineId;
    }

    String getLineName() {
        return lineName;
    }
}
