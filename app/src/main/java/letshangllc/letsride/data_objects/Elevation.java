package letshangllc.letsride.data_objects;

import java.util.ArrayList;

/**
 * Created by Carl on 8/6/2016.
 */
public class Elevation {
    public ArrayList<Double> allElevations;
    public ArrayList<Double> normalElevations;
    public ArrayList<Double> outlierElevations;

    public Elevation() {
        allElevations = new ArrayList<>();
        normalElevations = new ArrayList<>();
        outlierElevations = new ArrayList<>();
    }

    /* Create the list of outliers and normal data points */
    public void createOutliers()
    {
        /* Update Normal speeds whenever an average is needed */
        normalElevations = new ArrayList<>();
        outlierElevations = new ArrayList<Double>();
        double avg = getAverageAllElevations();
        double standardDeviation = standardDeviation();
        for(Double elevation: allElevations){
            if ((Math.abs(elevation - avg)) > (2 * standardDeviation))
                outlierElevations.add(elevation);
            else
                normalElevations.add(elevation);
        }
    }

    public double getMaxElevation(){
        double max = -1;
        for(Double elevation: allElevations){
            if (max < elevation){
                max = elevation;
            }
        }
        return max;
    }

    public double getMinElevation(){
        if (normalElevations.size() == 0){
            return 0;
        }
        double min = Double.MAX_VALUE;
        for(Double elevation: allElevations){
            if (min > elevation && elevation != 0){
                min = elevation;
            }
        }
        return min;
    }

    /* Get the average from all elevations */
    private double getAverageAllElevations(){
        double count = 0;
        double sum = 0;
        for(Double speed: allElevations){
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

    /* Get standard deviation of speeds */
    private double standardDeviation (){
        if(allElevations.size()== 1){
            return 0;
        }
        int sum = 0;
        double avg = getAverageAllElevations();

        for(Double speed: allElevations){
            sum+= Math.pow(speed - avg, 2);
        }

        return Math.sqrt( sum / ( allElevations.size() - 1 ) );
    }
}
