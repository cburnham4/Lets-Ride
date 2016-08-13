package letshangllc.letsride.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import letshangllc.letsride.R;

import letshangllc.letsride.activities.runinfo.RunInfoTabbedActivity;
import letshangllc.letsride.adapter.HistoryItemsAdapter;
import letshangllc.letsride.adapter.RecyclerViewClickListener;
import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewClickListener {
    private final static String TAG = HistoryActivity.class.getSimpleName();

    /* SEtting intent request */
    private final static int SETTING_REQUEST = 10;

    /* Recycleview items */
    private ArrayList<PastRunItem> pastRunItems;
    private HistoryItemsAdapter historyItemsAdapter;

    /* Database helper */
    private LocationDatabaseHelper databaseHelper;

    /* Day Id */
    private int dayId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        this.setupToolbar();
        this.getDay();
        this.findViews();
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getRunData(){
        pastRunItems = new ArrayList<>();
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

        if(c.getCount() == 0){
            c.close();
            db.close();
            return;
        }
        int prevRunId = -1;
        PastRunItem pastRunItem = null;
        String date;
        double duration, distance, maxSpeed, avgSpeed, maxElevation, minElevation;

        while(!c.isAfterLast()){
            int runId = c.getInt(c.getColumnIndex(DBTableConstants.RUN_ID));
            if(runId != prevRunId){
                prevRunId = runId;
                date = c.getString(c.getColumnIndex(DBTableConstants.DATE_STRING));
                duration = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_DURATION));
                distance = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_DISTANCE));
                maxSpeed = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_MAX_SPEED));
                avgSpeed = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_AVG_SPEED));
                maxElevation = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_MAX_ELEVATION));
                minElevation = c.getDouble(c.getColumnIndex(DBTableConstants.RUN_MIN_ELEVATION));
                pastRunItem = new PastRunItem(runId, dayId, date, duration, distance, maxSpeed, avgSpeed,
                        maxElevation, minElevation);
                pastRunItems.add(pastRunItem);
            }

            double lat = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_LAT));
            double lon = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_LONG));
            double speed = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_SPEED));
            double elevation = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_ELEVATION));
            pastRunItem.pastLocations.add(new PastLocation(lat, lon, speed, elevation));

            c.moveToNext();
        }
        c.close();
        db.close();
    }

    private void setupRecycleView() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int speedUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_speed_unit_index), "0"));
        int elevationUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_elevation_index), "0"));
        int distanceUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_distance_index), "0"));

        SpeedUnits speedUnits = SpeedUnits.getSpeedUnit(speedUnitIndex);
        LengthUnits lengthUnits = LengthUnits.getLengthUnits(elevationUnitIndex);
        LengthUnits distanceUnits =LengthUnits.getLengthUnits(distanceUnitIndex);
        historyItemsAdapter = new HistoryItemsAdapter(pastRunItems, this, distanceUnits,
                speedUnits, this);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvHistoryOfRuns);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(historyItemsAdapter);
    }

    private void findViews(){
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fabGoToRecord);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, RecordRunActivity.class);
                intent.putExtra(getString(R.string.day_id_extra), dayId);
                startActivity(intent);
            }
        });
    }

    private void getDay(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        databaseHelper = new LocationDatabaseHelper(this);
                /* First check if the db row has already been created */
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String[] projection = {DBTableConstants.DATE_ID};

        /* Query the exercise table based on the muscle id to get all the associated exercises */
        Cursor c = db.query(DBTableConstants.DATES_TABLE, projection, DBTableConstants.DATE_STRING
                + " = '" + currentDate +"'", null, null, null, null);

        c.moveToFirst();
        /* If there already exists a dayId for today then return it */
        if(!c.isAfterLast()){
            Log.e(TAG, "Day exists");
            dayId = c.getInt(0);
            c.close();
        }else{
            /* If there is no day id for the current day then insert it */
            Log.e(TAG, "Day does not exist");

            /* Else insert in a new day */
            ContentValues values = new ContentValues();
            values.put(DBTableConstants.DATE_STRING, currentDate);

            /* Insert values into db */
            dayId = (int) db.insert(DBTableConstants.DATES_TABLE, null, values);
            db.close();
        }

    }


    /* Run when a machine has been clicked */
    @Override
    public void recyclerViewListClicked(View v, final int position) {
        final PastRunItem pastRunItem = pastRunItems.get(position);

        Intent intent = new Intent(HistoryActivity.this, RunInfoTabbedActivity.class);
        intent.putExtra(getString(R.string.past_run_item_extra), pastRunItem);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent =  new Intent(HistoryActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTING_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTING_REQUEST){
            /* TODO: Change amounts when returned */
            Log.i(TAG, "Returned from settings ");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* TODO Structure these to only do it when coming from certain activities */
        this.getRunData();
        this.setupRecycleView();
    }
}
