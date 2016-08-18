package letshangllc.letsride.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.text.ParsePosition;
import java.util.ArrayList;

import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;

/**
 * Created by Carl on 8/18/2016.
 */
public class RetrievePastRunsAsync extends AsyncTask<Void, Void, ArrayList<PastRunItem>> {
    private static final String TAG = RetrievePastRunsAsync.class.getSimpleName();
    /* Database helper */
    private LocationDatabaseHelper databaseHelper;
    private RetrievePastComplete callback;
    private ProgressDialog dialog;
    private Context context;
    private int dayId;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Retrieving Data");
        dialog.show();
    }

    public RetrievePastRunsAsync(LocationDatabaseHelper databaseHelper, Context context,
                                 int dayId, RetrievePastComplete callback) {
        this.databaseHelper = databaseHelper;
        this.callback = callback;
        this.context = context;
        this.dayId = dayId;
    }

    @Override
    protected ArrayList doInBackground(Void... voids) {
        String sql = "SELECT * FROM " + DBTableConstants.LOCATION_TABLE_NAME + " INNER JOIN "
                + DBTableConstants.RUNS_TABLE + " ON " +
                DBTableConstants.LOCATION_TABLE_NAME + "." +DBTableConstants.RUN_ID +" = " +
                DBTableConstants.RUNS_TABLE +"." + DBTableConstants.RUN_ID +

                " INNER JOIN "
                + DBTableConstants.DATES_TABLE + " ON " +
                DBTableConstants.DATES_TABLE + "." +DBTableConstants.DATE_ID +" = " +
                DBTableConstants.RUNS_TABLE +"." + DBTableConstants.DATE_ID +

                " ORDER BY " + DBTableConstants.RUNS_TABLE +"." + DBTableConstants.RUN_ID + " DESC";

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        /* Run the query */
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        ArrayList<PastRunItem> pastRunItems = new ArrayList<>();
        if(c.getCount() == 0){
            c.close();
            db.close();
            return pastRunItems;
        }



        /* Create variables */
        int prevRunId = -1;
        PastRunItem pastRunItem = null;
        String date;
        double duration, distance, maxSpeed, avgSpeed, maxElevation, minElevation;
        long startTime;


        /* Iterate throught the cursor to get the runs */
        while(!c.isAfterLast()){
            int runId = c.getInt(c.getColumnIndex(DBTableConstants.RUN_ID));
            /* If it is a new runId then add all the run Id to that run */
            if(runId != prevRunId){
                prevRunId = runId;
                date = c.getString(c.getColumnIndex(DBTableConstants.DATE_STRING));
                duration = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_DURATION));
                distance = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_DISTANCE));
                maxSpeed = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_MAX_SPEED));
                avgSpeed = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_AVG_SPEED));
                maxElevation = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_MAX_ELEVATION));
                minElevation = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_MIN_ELEVATION));
                startTime = c.getLong(c.getColumnIndex(DBTableConstants.RUN_START_TIME));
                Log.i(TAG, "START TIME: " +startTime);
                pastRunItem = new PastRunItem(runId, dayId, date, duration, distance, startTime, maxSpeed, avgSpeed,
                        maxElevation, minElevation);
                pastRunItems.add(pastRunItem);
            }

            /* Add the locations, speed and elevation to the current run */
            double lat = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_LAT));
            double lon = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_LONG));
            double speed = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_SPEED));
            double elevation = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_ELEVATION));
            pastRunItem.pastLocations.add(new PastLocation(lat, lon, speed, elevation));

            c.moveToNext();
        }
        c.close();
        db.close();
        return pastRunItems;
    }

    @Override
    protected void onPostExecute(ArrayList<PastRunItem> pastRunItems) {
        super.onPostExecute(pastRunItems);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        callback.onRetrievalComplete(pastRunItems);
    }
}
