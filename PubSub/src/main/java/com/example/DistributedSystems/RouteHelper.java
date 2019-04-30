package com.example.DistributedSystems;
import java.io.Serializable;

class RouteHelper implements Serializable {
    private String lineCode;
    private String RouteCode;
    private String Desc;
    private String lineId;

    RouteHelper(String lineCode, String RouteCode, String Desc){
        this.lineCode = lineCode;
        this.RouteCode = RouteCode;
        this.Desc = Desc;
    }

    String getLineCode() {
        return lineCode;
    }

    String getDesc() {
        return Desc;
    }

    String getRouteCode() {
        return RouteCode;
    }

    String getLineId() {
        return lineId;
    }

    void setLineId(String lineId) {
        this.lineId = lineId;
    }
}

