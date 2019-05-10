package com.example.DistributedSystems;
import java.io.Serializable;
import java.util.Date;

public class Bus implements Serializable {
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

    public String getLineNumber(){
        return  lineNumber;
    }

    public String getRouteCode(){
        return  routeCode;
    }

    String getVehicleId(){
        return  vehicleId;
    }

    public Date getTime(){
        return time;
    }

    public String getBuslineId() {
        return buslineId;
    }

    public String getLineName() {
        return lineName;
    }
}
