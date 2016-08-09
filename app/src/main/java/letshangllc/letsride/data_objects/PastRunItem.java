package letshangllc.letsride.data_objects;

/**
 * Created by Carl on 8/9/2016.
 */
public class PastRunItem {
    public int runId;
    public int dayId;
    public String date;
    public double distance;
    public double maxSpeed;
    public double timeInMilli;

    public PastRunItem(int runId, int dayId, String date, double distance, double maxSpeed, double timeInMilli) {
        this.runId = runId;
        this.dayId = dayId;
        this.date = date;
        this.distance = distance;
        this.maxSpeed = maxSpeed;
        this.timeInMilli = timeInMilli;
    }
}
