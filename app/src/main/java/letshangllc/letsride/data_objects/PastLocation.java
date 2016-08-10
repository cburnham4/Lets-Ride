package letshangllc.letsride.data_objects;

/**
 * Created by Carl on 8/10/2016.
 */
public class PastLocation{
    public double lat;
    public double lon;
    public double speed;
    public double elevation;

    public PastLocation(double lat, double lon, double speed, double elevation) {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.elevation = elevation;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double p = 0.017453292519943295;    // Math.PI / 180

        double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p))/2;

        return 12742 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km
    }
}