package letshangllc.letsride.enums;

/**
 * Created by Carl on 8/6/2016.
 */
public enum ElevationUnits {
    FEET("ft", 0, 3.28084),
    MILE("mi", 1, 0.0006213712121212),
    METER("m",2, 1.0),
    KILOMETER("km",3, 0.001);

    public String label;
    public int index;
    public double multiplier;

    ElevationUnits(String label, int index, double multiplier) {
        this.label = label;
        this.index = index;
        this.multiplier = multiplier;
    }
}
