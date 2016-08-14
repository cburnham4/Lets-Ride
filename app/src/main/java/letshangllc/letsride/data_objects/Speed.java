package letshangllc.letsride.data_objects;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carl on 8/5/2016.
 * All Speeds are to be recorded and saved as Meters per seconds
 */
public class Speed {
    private String TAG = Speed.class.getSimpleName();
    public ArrayList<Double> allSpeeds;
    public ArrayList<Double> normalSpeeds;
    public ArrayList<Double> outlierSpeeds;

    public Speed() {
        allSpeeds = new ArrayList<>();
        normalSpeeds = new ArrayList<>();
        outlierSpeeds = new ArrayList<>();
    }

    private double getAverageAllSpeed(){
        double count = 0;
        double sum = 0;
        for(Double speed: allSpeeds){
            /* Do not count speeds of 0  in average */
            if(speed > 0){
                count++;
                sum += speed;
            }

        }
        if(count == 0){
            return  0;
        }
        return sum/count;
    }

    private double getAverageFromList(ArrayList<Double> speeds){
        double count = 0;
        double sum = 0;
        for(Double speed: allSpeeds){
            /* Do not count speeds of 0  in average */
            if(speed > 0){
                count++;
                sum += speed;
            }
        }
        if(count == 0){
            return  0;
        }
        return sum/count;
    }

    /* Return the average speed but after removing suspected outliers */
    public double getAverageWithoutOutliers()
    {
        if (allSpeeds.size() <= 1)
            return 0;

        /* Update Normal speeds whenever an average is needed */
        normalSpeeds = new ArrayList<>();
        outlierSpeeds = new ArrayList<Double>();
        double previousSpeed = allSpeeds.get(0);
        double standardDeviation = standardDeviation();
        for(int i = 1; i < allSpeeds.size(); i++){
            double speed = allSpeeds.get(i);
            if ((Math.abs(speed - previousSpeed)) > (2 * standardDeviation))
                outlierSpeeds.add(speed);
            else
                normalSpeeds.add(speed);
            previousSpeed = speed;
        }

        return getAverageFromList(normalSpeeds);
    }

    /* Get standard deviation of speeds */
    private double standardDeviation (){
        if(allSpeeds.size()== 1){
            return 0;
        }
        int sum = 0;
        double avg = getAverageAllSpeed();

        for(Double speed: allSpeeds){
            sum+= Math.pow(speed - avg, 2);
        }

        return Math.sqrt( sum / ( allSpeeds.size() - 1 ) );
    }

    /* Get max from the normal speeds */
    public double getMaxSpeeds(){
        double max = -1;
        for(Double speed: normalSpeeds){
            /* Do not count speeds of 0 in average */
            if(max < speed){
                max = speed;
            }
        }
        return max==-1 ? 0 : max;
    }
}
