import java.io.Serializable;

class BusHelper implements Serializable {
    private String busLineId;
    private String busLineCode;
    private String lineName;

    BusHelper(String LineId, String busLineCode, String lineName){
        this.busLineId = LineId;
        this.busLineCode = busLineCode;
        this.lineName = lineName;
    }

    String getLineId() {
        return busLineId;
    }

    String getLineName() {
        return lineName;
    }

    String getBusLineCode() {
        return busLineCode;
    }
}

