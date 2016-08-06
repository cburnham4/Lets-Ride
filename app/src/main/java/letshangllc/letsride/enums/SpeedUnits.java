package letshangllc.letsride.enums;

/**
 * Created by Carl on 8/6/2016.
 */
public enum SpeedUnits {
    MILES_PER_HOUR("mph", 0, 2.236936),
    METER_PER_SECOND("m/s", 1, 1.0),
    KILOMETER_PER_HOUR("km/h", 2, 3.6);

    public String label;
    public int index;
    public double multiplier;

    SpeedUnits(String label, int index, double multiplier) {
        this.label = label;
        this.index = index;
        this.multiplier = multiplier;
    }

    public static SpeedUnits getSpeedUnit(int index){
        switch (index){
            case 0:
                return MILES_PER_HOUR;
            case 1:
                return METER_PER_SECOND;
            case 2:
                return KILOMETER_PER_HOUR;
            case 3:
                //return TIMED;
        }
        return MILES_PER_HOUR;
    }

    public String toString(){
        return label;
    }
}
