package letshangllc.letsride.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Carl on 8/8/2016.
 */
public class LocationDatabaseHelper  extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationsDatabase.db";


    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        //if(checkDataBase()){
        db.execSQL(SQLCreateTables.CREATE_LOCATIONS_TABLE);
        db.execSQL(SQLCreateTables.CREATE_DATES_TABLE);
        db.execSQL(SQLCreateTables.CREATE_RUNS_TABLE);

        //
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}