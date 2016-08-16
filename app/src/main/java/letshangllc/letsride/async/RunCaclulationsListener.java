package letshangllc.letsride.async;

/**
 * Created by Carl on 8/13/2016.
 */
public interface RunCaclulationsListener {
    void onCalculationsComplete(double distanceInMeters, double speedAvg, double maxSpeed,
                                double maxElevation, double minElevation);
}
