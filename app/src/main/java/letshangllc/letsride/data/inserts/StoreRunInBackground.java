package letshangllc.letsride.data.inserts;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;

import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data.StoringDataComplete;
import letshangllc.letsride.data_objects.PastLocation;

/**
 * Created by Carl on 8/8/2016.
 */
public class StoreRunInBackground extends AsyncTask<Void, Void, Void>{

    private ArrayList<PastLocation> pastLocations;
    private int dayId;
    private double duration;
    private LocationDatabaseHelper locationDatabaseHelper;

    private StoringDataComplete callback;
    private ProgressDialog dialog;
    private Context context;

    public StoreRunInBackground(ArrayList<PastLocation> pastLocations, int dayId, double duration,
                                LocationDatabaseHelper locationDatabaseHelper, Context context, StoringDataComplete callback) {
        this.pastLocations = pastLocations;
        this.dayId = dayId;
        this.duration = duration;
        this.locationDatabaseHelper = locationDatabaseHelper;
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Saving Data");
        dialog.show();
    }


    @Override
    protected Void doInBackground(Void... voids) {
        SQLiteDatabase db = locationDatabaseHelper.getWritableDatabase();

         /* Get the max run num for that day */
        String sql = "SELECT MAX("+DBTableConstants.RUN_NUMBER+")FROM " + DBTableConstants.RUNS_TABLE +
                " WHERE " +DBTableConstants.DATE_ID +
                " = " + dayId;

        int runNum = 1;
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        /* if the cursor contains an int then add one to it for the new runNum */
        if(c.getCount() == 1){
            runNum = c.getInt(0) + 1;
        }

        c.close();

         /* Insert a new run with the new run */
        ContentValues values = new ContentValues();
        values.put(DBTableConstants.DATE_ID, dayId);
        values.put(DBTableConstants.RUN_NUMBER, runNum);
        values.put(DBTableConstants.RUN_DURATION, duration);

         /* Insert values into db */
        int runId = (int) db.insert(DBTableConstants.RUNS_TABLE, null, values);

        /* Insert all the locations into the database */
        for(PastLocation pastLocation:pastLocations){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBTableConstants.LOCATION_LAT, pastLocation.lat);
            contentValues.put(DBTableConstants.LOCATION_LONG, pastLocation.lon);
            contentValues.put(DBTableConstants.LOCATION_SPEED, pastLocation.speed);
            contentValues.put(DBTableConstants.LOCATION_ELEVATION, pastLocation.elevation);
            contentValues.put(DBTableConstants.RUN_ID, runId);

            db.insert(DBTableConstants.LOCATION_TABLE_NAME, null, contentValues);
        }

        db.close();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        callback.onDataStored();
    }
}