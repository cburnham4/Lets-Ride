package letshangllc.letsride.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.adapter.HistoryItemsAdapter;
import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.PastRunItem;

public class HistoryActivity extends AppCompatActivity {
    private final static String TAG = HistoryActivity.class.getSimpleName();

    /* Recycleview items */
    private ArrayList<PastRunItem> pastRunItems;
    private HistoryItemsAdapter historyItemsAdapter;

    private int dayId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupToolbar();
        this.setupRecycleView();

        this.getDay();
        this.findViews();
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecycleView() {
        pastRunItems = new ArrayList<>();
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        pastRunItems.add(new PastRunItem(0,0, "Mon, 05/08/16", 4.59, 110.9, 100000) );
        historyItemsAdapter = new HistoryItemsAdapter(pastRunItems, this);

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
        LocationDatabaseHelper databaseHelper = new LocationDatabaseHelper(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.US);
        Date date = new Date();
        String currentDate = dateFormat.format(date);

                /* First check if the db row has already been created */
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
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
}
