package letshangllc.letsride.data_objects;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carl on 8/5/2016.
 * All Speeds are to be recorded and saved as Meters per seconds
 */
public class Speed {
    public ArrayList<Double> allSpeeds;
    public ArrayList<Double> normalSpeeds;
    public ArrayList<Double> outlierSpeeds;
    private double maxSpeed;

    public Speed() {
        this.maxSpeed = 0;
        allSpeeds = new ArrayList<>();
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
        if (allSpeeds.size() == 0)
            return 0;

        ArrayList<Double> normalNumbers = new ArrayList<>();
        ArrayList<Double> outLierNumbers = new ArrayList<Double>();
        double avg = getAverageAllSpeed();
        double standardDeviation = standardDeviation();
        for(Double speed: allSpeeds){
            if ((Math.abs(speed - avg)) > (2 * standardDeviation))
                outLierNumbers.add(speed);
            else
                normalNumbers.add(speed);
        }

        return getAverageFromList(normalNumbers);
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
}
