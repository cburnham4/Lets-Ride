package letshangllc.letsride.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import letshangllc.letsride.R;
import letshangllc.letsride.activities.runinfo.RunInfoTabbedActivity;
import letshangllc.letsride.adapter.HistoryItemsAdapter;
import letshangllc.letsride.adapter.RecyclerViewClickListener;
import letshangllc.letsride.async.RetrievePastComplete;
import letshangllc.letsride.async.RetrievePastRunsAsync;
import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;
import letshangllc.letsride.helpers.AdsHelper;
import letshangllc.letsride.helpers.EdgeToEdgeHelper;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewClickListener {
    private final static String TAG = HistoryActivity.class.getSimpleName();

    private final static int SETTING_REQUEST = 10;
    private final static int RECORD_TRACKER_REQUEST = 11;

    private ArrayList<PastRunItem> pastRunItems;
    private HistoryItemsAdapter historyItemsAdapter;

    private LocationDatabaseHelper databaseHelper;
    private int dayId;
    private AdsHelper adsHelper;
    private Timer adTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        EdgeToEdgeHelper.apply(this);

        this.setupToolbar();
        this.getDay();
        this.findViews();
        this.getRunData();
        this.runAds();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getRunData() {
        new RetrievePastRunsAsync(databaseHelper, this, dayId, new RetrievePastComplete() {
            @Override
            public void onRetrievalComplete(ArrayList<PastRunItem> pastRunItems) {
                Log.i(TAG, "RUN ITEMS: " + pastRunItems.size());
                HistoryActivity.this.pastRunItems = pastRunItems;
                setupRecycleView();
            }
        }).execute();
    }

    private void getRecentRun() {
        String sql = "SELECT * FROM " + DBTableConstants.LOCATION_TABLE_NAME + " INNER JOIN "
                + DBTableConstants.RUNS_TABLE + " ON " +
                DBTableConstants.LOCATION_TABLE_NAME + "." + DBTableConstants.RUN_ID + " = " +
                DBTableConstants.RUNS_TABLE + "." + DBTableConstants.RUN_ID +

                " WHERE " + DBTableConstants.RUNS_TABLE + "." + DBTableConstants.RUN_ID + " = " +
                "(SELECT MAX(" + DBTableConstants.RUNS_TABLE + "." + DBTableConstants.RUN_ID + ")" +
                "FROM " + DBTableConstants.RUNS_TABLE + " ) ";

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            c.close();
            db.close();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        PastRunItem pastRunItem = null;

        if (!c.isAfterLast()) {
            int runId = c.getInt(c.getColumnIndexOrThrow(DBTableConstants.RUN_ID));
            double duration = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.RUN_DURATION));
            double distance = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.RUN_DISTANCE));
            double maxSpeed = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.RUN_MAX_SPEED));
            double avgSpeed = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.RUN_AVG_SPEED));
            double maxElevation = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.RUN_MAX_ELEVATION));
            double minElevation = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.RUN_MIN_ELEVATION));
            long startTime = c.getLong(c.getColumnIndexOrThrow(DBTableConstants.RUN_START_TIME));
            pastRunItem = new PastRunItem(runId, dayId, currentDate, duration, distance, startTime, maxSpeed, avgSpeed,
                    maxElevation, minElevation);
            pastRunItems.add(0, pastRunItem);
        }

        while (!c.isAfterLast()) {
            double lat = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.LOCATION_LAT));
            double lon = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.LOCATION_LONG));
            double speed = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.LOCATION_SPEED));
            double elevation = c.getDouble(c.getColumnIndexOrThrow(DBTableConstants.LOCATION_ELEVATION));
            pastRunItem.pastLocations.add(new PastLocation(lat, lon, speed, elevation));
            c.moveToNext();
        }
        historyItemsAdapter.notifyDataSetChanged();
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
        LengthUnits distanceUnits = LengthUnits.getLengthUnits(distanceUnitIndex);
        historyItemsAdapter = new HistoryItemsAdapter(pastRunItems, this, distanceUnits,
                speedUnits, this);

        final RecyclerView recyclerView = findViewById(R.id.rvHistoryOfRuns);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(historyItemsAdapter);
    }

    private void findViews() {
        Button btnStartTracking = findViewById(R.id.btnTrackActivity);
        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, RecordRunActivity.class);
                intent.putExtra(getString(R.string.day_id_extra), dayId);
                startActivityForResult(intent, RECORD_TRACKER_REQUEST);
            }
        });
    }

    private void getDay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        databaseHelper = new LocationDatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String[] projection = {DBTableConstants.DATE_ID};

        Cursor c = db.query(DBTableConstants.DATES_TABLE, projection, DBTableConstants.DATE_STRING
                + " = '" + currentDate + "'", null, null, null, null);

        c.moveToFirst();
        if (!c.isAfterLast()) {
            Log.e(TAG, "Day exists");
            dayId = c.getInt(0);
            c.close();
        } else {
            Log.e(TAG, "Day does not exist");
            ContentValues values = new ContentValues();
            values.put(DBTableConstants.DATE_STRING, currentDate);
            dayId = (int) db.insert(DBTableConstants.DATES_TABLE, null, values);
            db.close();
        }
    }

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
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(HistoryActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTING_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTING_REQUEST:
                HistoryActivity.this.setupRecycleView();
                break;
            case RECORD_TRACKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    this.getRecentRun();
                }
                break;
        }
    }

    public void runAds() {
        adsHelper = new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_id_history), this);
        adsHelper.setUpAds();
        int delay = 1000;
        int period = getResources().getInteger(R.integer.ad_refresh_rate);
        adTimer = new Timer();
        adTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (adsHelper != null) {
                    adsHelper.refreshAd();
                }
            }
        }, delay, period);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adsHelper != null) {
            adsHelper.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adsHelper != null) {
            adsHelper.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adTimer != null) {
            adTimer.cancel();
            adTimer = null;
        }
        if (adsHelper != null) {
            adsHelper.onDestroy();
        }
        super.onDestroy();
    }
}
