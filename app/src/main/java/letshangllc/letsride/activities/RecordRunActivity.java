package letshangllc.letsride.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import letshangllc.letsride.objects.AdsHelper;
import letshangllc.letsride.R;
import letshangllc.letsride.data.DBTableConstants;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data.inserts.StoreLocationInBackground;
import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.Speed;
import letshangllc.letsride.objects.StopWatch;
import letshangllc.letsride.enums.ElevationUnits;
import letshangllc.letsride.enums.SpeedUnits;


public class RecordRunActivity extends AppCompatActivity implements LocationListener {
    private final static String TAG = RecordRunActivity.class.getSimpleName();

    /* Permission Variables */
    private final static int REQUEST_LOCATION_PERMISSIONS = 0;
    private boolean locationPermissionsEnabled;

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) 0.5;

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 2000;

    /* Recording Variables
    *  UNITS ARE STORES AS METERS AND METERS PER SECOND*/
    private boolean isRecording = false;
    private Speed speed = new Speed();
    private Elevation elevation = new Elevation();

    /* Timing Variables */
    private StopWatch stopWatch = new StopWatch();
    private Handler handler = new Handler();

    /* Location Variables */
    private LocationManager locationManager;
    private boolean locationEnabled = false;

    /* Units */
    private SpeedUnits speedUnits;
    private ElevationUnits elevationUnits;

    /* VIEWS */
    private TextView tvCurrentSpeed, tvMaxSpeed, tvAvgSpeed, tvCurrentElevation, tvMaxElevation,
            tvMinElevation, tvDuration, tvSpeedUnits, tvElevationUnits;
    private FloatingActionButton fabStartPauseRecording, fabStopRecording;

    /* Data */
    private LocationDatabaseHelper databaseHelper;
    private int dayId;
    private int runId;
    private int runNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_run);

        adsHelper =  new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_ad_id_main), this);

        adsHelper.setUpAds();

        this.getUserData();
        this.getRunData();
        this.findViews();
        this.setupViews();
        this.requestPermission();
        this.requestLocationEnabled();
        this.runAds();
    }

    private void getUserData(){
        dayId = getIntent().getExtras().getInt(getString(R.string.day_id_extra), 0);
        SharedPreferences settings = getSharedPreferences(getString(R.string.user_preferences), 0);
        int speedUnitIndex = settings.getInt(getString(R.string.user_pref_speed_unit_index), 0 );
        int elevationUnitIndex = settings.getInt(getString(R.string.user_pref_elevation_index),0 );

        speedUnits = SpeedUnits.getSpeedUnit(speedUnitIndex);
        elevationUnits = ElevationUnits.getElevationUnits(elevationUnitIndex);

        databaseHelper = new LocationDatabaseHelper(this);
    }

    private void getRunData(){
        /* First check if the db row has already been created */
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        /* Get the max run num for that day */
        String sql = "SELECT MAX("+DBTableConstants.RUN_NUMBER+")FROM " + DBTableConstants.RUNS_TABLE +
                " WHERE " +DBTableConstants.DATE_ID +
                " = " + dayId;

        runNum = 1;
        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        /* if the cursor contains an int then add one to it for the new runNum */
        if(c.getCount() == 1){
            runNum = c.getInt(0) + 1;
        }

         /* Insert a new run with the new run */
        ContentValues values = new ContentValues();
        values.put(DBTableConstants.DATE_ID, dayId);
        values.put(DBTableConstants.RUN_NUMBER, runNum);
        values.put(DBTableConstants.RUN_DURATION, 0.0);

         /* Insert values into db */
        runId = (int) db.insert(DBTableConstants.RUNS_TABLE, null, values);
        db.close();
    }

    /* Make sure the user has location permissions enabled */
    private void requestPermission() {
        /* If permissions not allowed then request them */
        Log.i(TAG, "Reqeuest Permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.enable_location_service), Toast.LENGTH_LONG).show();
            Log.i(TAG, "Permission is requested");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_LOCATION_PERMISSIONS);

        } else {
            Log.i(TAG, "Location Permissions are enabled ");
            locationPermissionsEnabled = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "Permission Request Result");
        if (REQUEST_LOCATION_PERMISSIONS == requestCode) {
            locationPermissionsEnabled =true;
        }
    }

    /* Request that location services are enabled */
    private void requestLocationEnabled(){
        locationManager = (LocationManager) this
                .getSystemService(AppCompatActivity.LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            /* If both are disabled then request location be enabled */
        if (!isGPSEnabled && !isNetworkEnabled) {
            // location service disabled
            Log.i("GPS: ", "Disabled");
            Toast.makeText(this, getString(R.string.enable_location_service), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else{
            locationEnabled = true;
        }
    }

    private void startLocationListener() {
        Log.i(TAG, "Start Location Listener");
        isRecording = true;
        try {
            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            /* If both are disabled then request location be enabled */
            if (!isGPSEnabled && !isNetworkEnabled) {
                // location service disabled
                Log.i("GPS: ", "Disabled");
                Toast.makeText(this, "Enable Location Service", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {
                Log.i(TAG, "Location services are enabled");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }

                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                } else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                } else{
                    Toast.makeText(this, "Enable Location Services", Toast.LENGTH_LONG).show();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error : Location",
                    "Impossible to connect to LocationManager", e);
        }
    }

    private void startRecording(){
        Log.i(TAG, "Start Timer");
        if(locationEnabled && locationPermissionsEnabled){
            startLocationListener();
            /* Start the timer */
            stopWatch.start();
            /* Start the handler to request times from stopwatch */
            handler.post(updateTimer);
        } else{
            if(!locationEnabled){
                requestLocationEnabled();
            }
            if(!locationPermissionsEnabled){
                requestPermission();
            }
        }
    }

    private void stopRecording() {
        Log.i(TAG, "Stop Recording ");
        isRecording = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.enable_location_service), Toast.LENGTH_LONG).show();
            return;
        }
        resetViewsAndData();
        locationManager.removeUpdates(this);

        stopWatch.stop();
        handler.removeCallbacks(updateTimer);
    }

    private void pauseRecording(){
        isRecording = false;
        Log.i(TAG, "PAUSE");
        stopWatch.pause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.enable_location_service), Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.removeUpdates(this);
    }

    private void resetViewsAndData(){
        tvDuration.setText(getString(R.string.time_zero));
        tvCurrentSpeed.setText(getString(R.string.empty));
        tvMaxSpeed.setText(getString(R.string.empty));
        tvAvgSpeed.setText(getString(R.string.empty));
        tvMinElevation.setText(getString(R.string.empty));
        tvMaxElevation.setText(getString(R.string.empty));
        tvCurrentElevation.setText(getString(R.string.empty));

        speed.allSpeeds.clear();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Updated");
        if(location!= null){
            new StoreLocationInBackground(location,dayId, runId, databaseHelper).execute();

            double currentSpeed = location.getSpeed();
            double currentElevation = location.getAltitude();

            /* Add Speeds and Elevations */
            speed.allSpeeds.add(currentSpeed);
            elevation.allElevations.add(currentElevation);

            /* Convert speed and elevations to perferred units */
            double speedInUnits = currentSpeed * speedUnits.multiplier;
            double elivationInUnits = currentElevation *elevationUnits.multiplier;

            tvCurrentSpeed.setText(String.format(Locale.getDefault(), "%.2f", speedInUnits));
            tvAvgSpeed.setText(String.format(Locale.getDefault(), "%.2f", speed.getAverageWithoutOutliers()
                    * speedUnits.multiplier));

            tvMaxSpeed.setText(String.format(Locale.getDefault(), "%.2f", speed.getMaxSpeeds() * speedUnits.multiplier));


            elevation.createOutliers();
            /* Do nothing if elivation is 0 because it is not accurate */
            if (currentElevation != 0){
                tvCurrentElevation.setText(String.format(Locale.getDefault(), "%.1f", elivationInUnits));

                tvMaxElevation.setText(String.format(Locale.getDefault(), "%.1f",
                        elevation.getMaxElevation() *elevationUnits.multiplier));

                double minElevation =  elevation.getMinElevation();
                if(minElevation != 0){
                    tvMinElevation.setText(String.format(Locale.getDefault(), "%.1f",
                            minElevation *elevationUnits.multiplier));
                }

            }
        }
    }

    /* Find Views */
    private void findViews(){
        tvCurrentSpeed = (TextView) findViewById(R.id.tvCurrentSpeed);
        tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
        tvAvgSpeed = (TextView) findViewById(R.id.tvAvgSpeed);
        tvCurrentElevation = (TextView) findViewById(R.id.tvCurrentElevation);
        tvMaxElevation = (TextView) findViewById(R.id.tvMaxElevation);
        tvMinElevation = (TextView) findViewById(R.id.tvMinElevation);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvSpeedUnits = (TextView) findViewById(R.id.tvSpeedUnits);
        tvElevationUnits = (TextView) findViewById(R.id.tvElevationUnits);
        fabStartPauseRecording = (FloatingActionButton) findViewById(R.id.fabStartPauseRecording);
        fabStopRecording = (FloatingActionButton) findViewById(R.id.fabStopRecording);


    }

    /* Set views text and listeners */
    private void setupViews(){
        tvSpeedUnits.setText(speedUnits.label);
        tvElevationUnits.setText(elevationUnits.label);

        ImageView imgSettings = (ImageView) findViewById(R.id.imgSettings);

        fabStartPauseRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* If not recording then start recording */
                if(!isRecording){
                    fabStartPauseRecording.setImageResource(R.drawable.ic_pause_white_48dp);
                    fabStopRecording.setVisibility(View.GONE);
                    startRecording();
                    /* If it is recording the pause it */
                }else{
                    fabStartPauseRecording.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    fabStopRecording.setVisibility(View.VISIBLE);
                    pauseRecording();
                }
            }
        });

        fabStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* TODO: Store Data */
                fabStopRecording.setVisibility(View.GONE);
                stopRecording();
            }
        });

    }

    /* Update the non current values for their new units */
    private  void updateViewForUnits(){
        tvSpeedUnits.setText(speedUnits.label);
        tvElevationUnits.setText(elevationUnits.label);

        tvAvgSpeed.setText(String.format(Locale.getDefault(), "%.2f", speed.getAverageWithoutOutliers() * speedUnits.multiplier));
        tvMaxSpeed.setText(String.format(Locale.getDefault(), "%.2f", speed.getMaxSpeeds() * speedUnits.multiplier));
        tvMaxElevation.setText(String.format(Locale.getDefault(), "%.1f", elevation.getMaxElevation() * elevationUnits.multiplier));
        tvMinElevation.setText(String.format(Locale.getDefault(), "%.1f", elevation.getMinElevation() * elevationUnits.multiplier));
    }

    public Runnable updateTimer = new Runnable() {
        public void run() {
            /* Remove callbacks on stop */
            //handler.removeCallbacks(updateTimer);
            if(isRecording){
                int[] times = stopWatch.getHourMinSecs();

                /* Update the timer*/
                tvDuration.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", times[0],
                        times[1],times[2]));

                /* Run updateTimte again in 100ms */
                adsHelper.refreshAd();
                handler.postDelayed(this, 1000);
            }
        }};

    /* Unused Location Listener functions */
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private AdsHelper adsHelper;
    public void runAds(){
//        adsHelper =  new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_ad_id_main), this);
//
//        adsHelper.setUpAds();
//        int delay = 1000; // delay for 1 sec.
//        int period = getResources().getInteger(R.integer.ad_refresh_rate);
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                adsHelper.refreshAd();  // display the data
//            }
//        }, delay, period);
    }
}
