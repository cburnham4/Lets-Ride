package letshangllc.letsride.activities;

import android.os.AsyncTask;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.PastLocation;
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
    private double distanceInUnits;
    private TextView tvDistance;

    public CalculateDistanceAsync(ArrayList<PastLocation> pastLocations, LengthUnits distanceUnits,
                                  TextView tvDistance, double currentDistance) {
        this.pastLocations = pastLocations;
        this.distanceUnits = distanceUnits;
        this.tvDistance = tvDistance;
        this.distanceInUnits = currentDistance;
    }

    protected Double doInBackground(Void... voids) {
        if(pastLocations.size() <= 1){
            return 0D;
        }
        int size = pastLocations.size();
        PastLocation firstLoc = pastLocations.get(size-2);
        PastLocation endLoc = pastLocations.get(size-1);

        distanceInUnits += PastLocation.distance(firstLoc.lat, firstLoc.lon, endLoc.lat, endLoc.lon) *distanceUnits.multiplier;
        return distanceInUnits;
    }

    @Override
    protected void onPostExecute(Double distance) {
        super.onPostExecute(distance);
        tvDistance.setText(String.format(Locale.getDefault(), "%.1f", distance));
    }
}
