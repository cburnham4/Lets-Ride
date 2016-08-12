package letshangllc.letsride.data_objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Carl on 8/10/2016.
 */
public class PastLocation implements Parcelable{
    public double lat;
    public double lon;
    public double speed;
    public double elevation;
    /* Change to LatLng */

    public PastLocation(double lat, double lon, double speed, double elevation) {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.elevation = elevation;
    }

    protected PastLocation(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
        speed = in.readDouble();
        elevation = in.readDouble();
    }

    public static final Creator<PastLocation> CREATOR = new Creator<PastLocation>() {
        @Override
        public PastLocation createFromParcel(Parcel in) {
            return new PastLocation(in);
        }

        @Override
        public PastLocation[] newArray(int size) {
            return new PastLocation[size];
        }
    };

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double p = 0.017453292519943295;    // Math.PI / 180

        double a = 0.5 - Math.cos((lat2 - lat1) * p)/2 +
                Math.cos(lat1 * p) * Math.cos(lat2 * p) *
                        (1 - Math.cos((lon2 - lon1) * p))/2;

        return 12742 * Math.asin(Math.sqrt(a))*1000; // 2 * R; R = 6371 km
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
        parcel.writeDouble(speed);
        parcel.writeDouble(elevation);
    }
}