package letshangllc.letsride.async;

import android.os.AsyncTask;

import java.util.ArrayList;

import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.Speed;

/**
 * Created by Carl on 8/11/2016.
 */
public class CalculateRunStatsAsync extends AsyncTask<Void, Void, Double> {
    private ArrayList<PastLocation> pastLocations;
    private RunCaclulationsListener caclulationsListener;
    private double currentDistance;
    private Speed speed;
    private double maxSpeed;
    private double avgSpeed;
    private Elevation elevation;
    private double maxElevation;
    private double minElevation;


    public CalculateRunStatsAsync(ArrayList<PastLocation> pastLocations, double currentDistance,
                                  Speed speed, Elevation elevation, RunCaclulationsListener caclulationsListener) {

        this.pastLocations = pastLocations;
        this.currentDistance = currentDistance;
        this.speed = speed;
        this.elevation = elevation;
        this.caclulationsListener = caclulationsListener;
    }

    protected Double doInBackground(Void... voids) {
        /* Calculate the distance */
        if(pastLocations.size() <= 1){
            return 0D;
        }
        int size = pastLocations.size();
        PastLocation firstLoc = pastLocations.get(size-2);
        PastLocation endLoc = pastLocations.get(size-1);

        currentDistance += PastLocation.distance(firstLoc.lat, firstLoc.lon, endLoc.lat, endLoc.lon);

        /* Speed Calculations */
        avgSpeed = speed.getAverageWithoutOutliers();
        maxSpeed = speed.getMaxSpeeds();

        /* Elevation Calculations */
        maxElevation = elevation.getMaxElevation();
        minElevation = elevation.getMinElevation();

        return currentDistance;
    }

    @Override
    protected void onPostExecute(Double distance) {
        super.onPostExecute(distance);
        caclulationsListener.onCalculationsComplete(distance, avgSpeed, maxSpeed, maxElevation, minElevation);
    }
}
