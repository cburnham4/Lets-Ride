package letshangllc.letsride.activities;

import android.os.AsyncTask;

import java.util.ArrayList;

import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.data_objects.Speed;

/**
 * Created by Carl on 8/11/2016.
 */
public class CalculateDistanceAsync extends AsyncTask<Void, Void, Long> {
    private ArrayList<PastLocation> pastLocations;
    private Speed speed;
    private Elevation elevation;
    private double distanceInMeters;

    public CalculateDistanceAsync(ArrayList<PastLocation> pastLocations, Speed speed, Elevation elevation) {
        this.pastLocations = pastLocations;
        this.speed = speed;
        this.elevation = elevation;
    }

    protected Long doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }
}
