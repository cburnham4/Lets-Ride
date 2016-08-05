package letshangllc.letsride;

import java.util.ArrayList;

/**
 * Created by Carl on 8/5/2016.
 */
public class Speed {
    public ArrayList<Double> speeds;
    private double maxSpeed;

    public Speed() {
        this.speeds = speeds;
        this.maxSpeed = maxSpeed;
        speeds = new ArrayList<>();
    }

    /* TODO Take out obvious outliers */
    public double getAverageSpeed(){
        double count = 0;
        double sum = 0;
        for(Double speed: speeds){
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
}
