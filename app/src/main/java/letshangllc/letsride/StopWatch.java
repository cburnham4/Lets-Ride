package letshangllc.letsride;

/**
 * Created by Carl on 8/5/2016.
 */
public class StopWatch {
    private long startTime = 0;
    private long stopTime = 0;
    private long pauseTime = 0;
    private long currentIntervalTime = 0;
    private boolean running = false;


    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }


    public void stop() {
        this.stopTime = System.currentTimeMillis();
        pauseTime = 0;
        this.running = false;
    }

    public void pause(){
        pauseTime += System.currentTimeMillis() - startTime;
        this.running =false;
    }


    // elaspsed time in milliseconds
    private long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime +pauseTime;
        }
        return stopTime - startTime;
    }


    // elaspsed time in seconds
    public int getElapsedTimeSecs() {
        if (running) {
            return (int) ((System.currentTimeMillis() - startTime) / 1000);
        }
        return (int) ((stopTime - startTime) / 1000);
    }

    public int[] getHourMinSecs(){
        int[] times = new int[3];

        currentIntervalTime = System.currentTimeMillis() - startTime;
        long milliseconds = currentIntervalTime + pauseTime;
        int hours = (int) milliseconds / (60 * 60 * 1000);
        milliseconds -= hours * (60 * 60 * 1000);
        int minutes = (int) milliseconds / (60 * 1000);
        milliseconds -= minutes * (60 * 1000);
        int seconds = (int) milliseconds / 1000;

        times[0] = hours;
        times[1] = minutes;
        times[2] = seconds;

        return times;
    }
}
