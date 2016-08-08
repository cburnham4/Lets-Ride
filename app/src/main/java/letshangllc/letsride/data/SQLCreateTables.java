package letshangllc.letsride.data;

/**
 * Created by Carl on 8/8/2016.
 */
public class SQLCreateTables {
    public static final String CREATE_LOCATIONS_TABLE =
            "CREATE TABLE " + DBTableConstants.LOCATION_TABLE_NAME + " ( " +
                    DBTableConstants.LOCATION_ID + " integer primary key AUTOINCREMENT, " +
                    DBTableConstants.LOCATION_LAT + " real, " +
                    DBTableConstants.LOCATION_LONG + " real, " +
                    DBTableConstants.LOCATION_SPEED + " real, " +
                    DBTableConstants.LOCATION_ELEVATION +" real, "+
                    DBTableConstants.RUN_ID + " integer " +
                    "FOREIGN KEY(" + DBTableConstants.RUN_ID  + ") " +
                    "REFERENCES " + DBTableConstants.RUNS_TABLE + "(" + DBTableConstants.RUN_ID + ")" +
                    " )";

    public static final String CREATE_RUNS_TABLE =
            "CREATE TABLE " + DBTableConstants.RUNS_TABLE + " ( " +
                    DBTableConstants.RUN_ID + " integer primary key AUTOINCREMENT, " +
                    DBTableConstants.RUN_NUMBER + " real, " +
                    DBTableConstants.DATE_ID + " integer " +
                    "FOREIGN KEY(" + DBTableConstants.DATE_ID  + ") " +
                    "REFERENCES " + DBTableConstants.DATES_TABLE + "(" + DBTableConstants.DATE_ID + ")" +
                    " )";

    public static final String CREATE_DATES_TABLE =
            "CREATE TABLE " + DBTableConstants.DATES_TABLE + " ( " +
                    DBTableConstants.DATE_ID + " integer primary key AUTOINCREMENT, " +
                    DBTableConstants.DATE_STRING + " text, " +
                    " )";
}
