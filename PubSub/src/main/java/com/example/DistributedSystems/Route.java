import java.io.Serializable;

public class Route implements Serializable {
    private String description;
    private int LineCode;
    private int RouteCode;
    private int RouteType;

    public Route(int  RouteCode ,int LineCode,int RouteType,String description){
        this.description = description;
        this.RouteCode = RouteCode;
        this.LineCode = LineCode;
        this.RouteType = RouteType;
    }

    String getLineCode(){
        return Integer.toString(LineCode);
    }

    public String getRouteCode() {
        return Integer.toString(RouteCode);
    }

    public int getRouteType() {
        return RouteType;
    }

    public String getDescription() {
        return description;
    }
}
