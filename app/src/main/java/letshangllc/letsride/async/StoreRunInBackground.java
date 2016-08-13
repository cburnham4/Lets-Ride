package letshangllc.letsride.async;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;

import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.objects.RecordRunItem;

/**
 * Created by Carl on 8/8/2016.
 */
public class StoreRunInBackground extends AsyncTask<Void, Void, Void>{

    private ArrayList<PastLocation> pastLocations;
    private RecordRunItem recordRunItem;
    private LocationDatabaseHelper locationDatabaseHelper;

    private StoringDataComplete callback;
    private ProgressDialog dialog;
    private Context context;

    public StoreRunInBackground(ArrayList<PastLocation> pastLocations, RecordRunItem recordRunItem,
                                LocationDatabaseHelper locationDatabaseHelper, Context context, StoringDataComplete callback) {
        this.pastLocations = pastLocations;
        this.recordRunItem = recordRunItem;
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
                " = " + recordRunItem.dayId;

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
        values.put(DBTableConstants.DATE_ID, recordRunItem.dayId);
        values.put(DBTableConstants.RUN_NUMBER, runNum);
        values.put(DBTableConstants.RUN_DURATION, recordRunItem.duration);
        values.put(DBTableConstants.RUN_DISTANCE, recordRunItem.distance);
        values.put(DBTableConstants.RUN_START_TIME, recordRunItem.startTime);
        values.put(DBTableConstants.RUN_MAX_SPEED, recordRunItem.maxSpeed);
        values.put(DBTableConstants.RUN_AVG_SPEED, recordRunItem.avgSpeed);
        values.put(DBTableConstants.RUN_MAX_ELEVATION, recordRunItem.maxElevation);
        values.put(DBTableConstants.RUN_MIN_ELEVATION, recordRunItem.minElevation);

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