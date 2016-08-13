package letshangllc.letsride.objects;

/**
 * Created by Carl on 8/13/2016.
 */
public class RecordRunItem {
    public int dayId;
    public double duration;
    public double distance;
    public double startTime;
    public double maxSpeed;
    public double avgSpeed;
    public double maxElevation;
    public double minElevation;

    public RecordRunItem(int dayId, double duration, double distance, double startTime, double maxSpeed, double avgSpeed, double maxElevation, double minElevation) {
        this.dayId = dayId;
        this.duration = duration;
        this.distance = distance;
        this.startTime = startTime;
        this.maxSpeed = maxSpeed;
        this.avgSpeed = avgSpeed;
        this.maxElevation = maxElevation;
        this.minElevation = minElevation;
    }
}
