package letshangllc.letsride.data.inserts;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;

import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;

/**
 * Created by Carl on 8/8/2016.
 */
public class StoreLocationInBackground extends AsyncTask<Void, Void, Void>{

    private Location location;
    private int dayId;
    private int runId;
    private LocationDatabaseHelper locationDatabaseHelper;

    public StoreLocationInBackground(Location location, int dayId, int runId, LocationDatabaseHelper locationDatabaseHelper) {
        this.location = location;
        this.dayId = dayId;
        this.runId = runId;
        this.locationDatabaseHelper = locationDatabaseHelper;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double speed = location.getSpeed();
        double elevation = location.getAltitude();

        SQLiteDatabase db = locationDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBTableConstants.LOCATION_LAT, lat);
        contentValues.put(DBTableConstants.LOCATION_LONG, lon);
        contentValues.put(DBTableConstants.LOCATION_SPEED, speed);
        contentValues.put(DBTableConstants.LOCATION_ELEVATION, elevation);
        contentValues.put(DBTableConstants.RUN_ID, runId);

        db.insert(DBTableConstants.LOCATION_TABLE_NAME, null, contentValues);

        db.close();

        return null;
    }
}