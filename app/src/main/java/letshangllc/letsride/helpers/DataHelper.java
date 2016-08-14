package letshangllc.letsride.helpers;

import java.util.ArrayList;

/**
 * Created by Carl on 8/14/2016.
 */
public final class DataHelper {

    /* Return a list not containing the outliers */
    public static ArrayList<Double> getNormalDataSet(ArrayList<Double> values)
    {
        if (values.size() <= 1)
            return new ArrayList<>();;

        /* Update Normal speeds whenever an average is needed */
        ArrayList<Double> normal = new ArrayList<>();
        ArrayList<Double> outliers = new ArrayList<Double>();
        double previousValue = values.get(0);
        double standardDeviation = standardDeviation(values);
        for(int i = 1; i < values.size(); i++){
            double speed = values.get(i);
            if ((Math.abs(speed - previousValue)) > (2 * standardDeviation))
                outliers.add(speed);
            else
                normal.add(speed);
            previousValue = speed;
        }

        return normal;
    }

    /* Get standard deviation of speeds */
    private static double standardDeviation (ArrayList<Double> values){
        if(values.size()== 1){
            return 0;
        }
        int sum = 0;
        double avg = getAverageFromList(values);

        for(Double value: values){
            sum+= Math.pow(value - avg, 2);
        }

        return Math.sqrt( sum / (values.size() - 1 ) );
    }

    private static double getAverageFromList(ArrayList<Double> values){
        double count = 0;
        double sum = 0;
        for(Double value: values){
            /* Do not count speeds of 0  in average */
            if(value > 0){
                count++;
                sum += value;
            }
        }
        if(count == 0){
            return  0;
        }
        return sum/count;
    }

}
