package letshangllc.letsride.activities;

import android.os.AsyncTask;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.data_objects.Speed;
import letshangllc.letsride.enums.LengthUnits;

/**
 * Created by Carl on 8/11/2016.
 */
public class CalculateDistanceAsync extends AsyncTask<Void, Void, Double> {
    private ArrayList<PastLocation> pastLocations;
    private Speed speed;
    private Elevation elevation;
    private LengthUnits distanceUnits;
    private double distanceInMeters;
    private TextView tvDistance;

    public CalculateDistanceAsync(ArrayList<PastLocation> pastLocations, Speed speed, Elevation elevation,
                                  TextView tvDistance, double currentDistance) {
        this.pastLocations = pastLocations;
        this.speed = speed;
        this.elevation = elevation;
        this.tvDistance = tvDistance;
        this.distanceInMeters = currentDistance;
    }

    protected Double doInBackground(Void... voids) {
        if(pastLocations.size() <= 1){
            return 0D;
        }
        int size = pastLocations.size();
        PastLocation firstLoc = pastLocations.get(size-2);
        PastLocation endLoc = pastLocations.get(size-1);

        distanceInMeters += PastLocation.distance(firstLoc.lat, firstLoc.lon, endLoc.lat, endLoc.lon);
        return distanceInMeters;
    }

    @Override
    protected void onPostExecute(Double distance) {
        super.onPostExecute(distance);
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f", distance));
    }
}
