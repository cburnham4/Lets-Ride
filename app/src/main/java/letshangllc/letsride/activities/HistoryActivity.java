package letshangllc.letsride.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.adapter.HistoryItemsAdapter;
import letshangllc.letsride.adapter.RecyclerViewClickListener;
import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewClickListener {
    private final static String TAG = HistoryActivity.class.getSimpleName();

    /* Recycleview items */
    private ArrayList<PastRunItem> pastRunItems;
    private HistoryItemsAdapter historyItemsAdapter;

    private LocationDatabaseHelper databaseHelper;

    private int dayId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupToolbar();
        this.getDay();
        this.getRunData();
        this.setupRecycleView();
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
                DBTableConstants.RUNS_TABLE +"." + DBTableConstants.DATE_ID; //+


//                " WHERE " + DBTableConstants.RUNS_TABLE +"." +DBTableConstants.DATE_ID + " = " +
//                dayId;

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        /* Run the query */
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        int prevNum = 1;
        if(c.getCount() == 0){
            c.close();
            db.close();
            return;
        }
        String date = c.getString(c.getColumnIndex(DBTableConstants.DATE_STRING));
        PastRunItem pastRunItem = new PastRunItem(prevNum, dayId, date);
        pastRunItems.add(pastRunItem);
        while(!c.isAfterLast()){
            int runNum = c.getInt(c.getColumnIndex(DBTableConstants.RUN_NUMBER));
            if(runNum != prevNum){
                prevNum = runNum;
                date = c.getString(c.getColumnIndex(DBTableConstants.DATE_STRING));
                pastRunItem = new PastRunItem(runNum, dayId, date);
                pastRunItems.add(pastRunItem);
            }

            double lat = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_LAT));
            double lon = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_LONG));
            double speed = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_SPEED));
            double elevation = c.getDouble(c.getColumnIndex(DBTableConstants.LOCATION_ELEVATION));
            pastRunItem.pastLocations.add(new PastLocation(lat, lon, speed, elevation));
            Log.i(TAG, "Lat: "+ lat +" LONG: "+lon + " RUN: " + runNum);
            c.moveToNext();
        }
        c.close();
        db.close();
    }

    private void setupRecycleView() {
        historyItemsAdapter = new HistoryItemsAdapter(pastRunItems, this, this);

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

        Intent intent = new Intent(HistoryActivity.this, MapsActivity.class);
        startActivity(intent);
    }
}
