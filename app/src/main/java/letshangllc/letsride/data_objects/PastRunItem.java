package letshangllc.letsride.data_objects;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Carl on 8/9/2016.
 */
public class PastRunItem {
    public int runId;
    public int dayId;
    public String date;
    public double maxSpeed = 123.123;
    public double timeInMilli = 111.22;
    public ArrayList<PastLocation> pastLocations;

    public PastRunItem(int runId, int dayId, String date) {
        this.runId = runId;
        this.dayId = dayId;
        this.date = date;
        pastLocations =new ArrayList<>();
    }

    public double getDistance(){
        double km = 0;
        PastLocation prevLocation;
        PastLocation pastLocation;
        for(int i  =1; i <pastLocations.size(); i++){
            prevLocation = pastLocations.get(i-1);
            pastLocation = pastLocations.get(i);
            km+= PastLocation.distance(prevLocation.lat, prevLocation.lon, pastLocation.lat, pastLocation.lon);
        }
        return km;
    }




}


