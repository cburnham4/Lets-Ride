package letshangllc.letsride.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.async.CalculateRunStatsAsync;
import letshangllc.letsride.async.RunCaclulationsListener;
import letshangllc.letsride.async.StoringDataComplete;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.helpers.AdsHelper;
import letshangllc.letsride.R;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.async.StoreRunInBackground;
import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.Speed;
import letshangllc.letsride.data_objects.RecordRunItem;
import letshangllc.letsride.helpers.StopWatch;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;


public class RecordRunActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private final static String TAG = RecordRunActivity.class.getSimpleName();

    /* Permission Variables */
    private final static int REQUEST_LOCATION_PERMISSIONS = 0;

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) 3;

    // The minimum time beetwen updates in milliseconds
    private long MIN_TIME_BW_UPDATES = 3000;

    /* Recording Variables
    *  UNITS ARE STORES AS METERS AND METERS PER SECOND*/
    private boolean isRecording = false;
    private Speed speed = new Speed();
    private Elevation elevation = new Elevation();
    private ArrayList<PastLocation> pastLocations = new ArrayList<>();
    private RecordRunItem recordRunItem;

    /* Timing Variables */
    private StopWatch stopWatch = new StopWatch();
    private Handler handler = new Handler();

    /* Location Variables */
    // private LocationManager locationManager;
    private boolean locationEnabled = false;
    private boolean locationPermissionsEnabled;

    /* Units */
    private SpeedUnits speedUnits;
    private LengthUnits elevationUnits;
    private LengthUnits distanceUnits;

    /* VIEWS */
    private TextView tvCurrentSpeed, tvMaxSpeed, tvAvgSpeed, tvCurrentElevation, tvMaxElevation,
            tvMinElevation, tvDuration, tvSpeedUnits, tvElevationUnits, tvDistance, tvDistanceUnits;
    private FloatingActionButton fabStartPauseRecording, fabStopRecording;

    /* Data */
    private LocationDatabaseHelper databaseHelper;
    private int dayId;

    /* Google Locations API */
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /* Async calculator */
    private CalculateRunStatsAsync calculateRunStatsAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_run);

        adsHelper = new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_ad_id_record), this);

        adsHelper.setUpAds();

        this.setupAPI();
        this.getUserData();
        this.findViews();
        this.setupToolbar();
        this.setupViews();
        this.requestPermission();
        //this.requestLocationEnabled();
    }

    private void setupAPI() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        locationEnabled = true;

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    RecordRunActivity.this,
                                    REQUEST_LOCATION_PERMISSIONS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    private void getUserData() {
        dayId = getIntent().getExtras().getInt(getString(R.string.day_id_extra), 0);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int speedUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_speed_unit_index), "0"));
        int elevationUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_elevation_index), "0"));
        int distanceUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_distance_index), "0"));
        MIN_TIME_BW_UPDATES = 1000 * (long) settings.getInt(getString(R.string.user_pref_min_time_loc_request), 3);

        speedUnits = SpeedUnits.getSpeedUnit(speedUnitIndex);
        elevationUnits = LengthUnits.getLengthUnits(elevationUnitIndex);
        distanceUnits = LengthUnits.getLengthUnits(distanceUnitIndex);

        databaseHelper = new LocationDatabaseHelper(this);

        recordRunItem = new RecordRunItem(dayId, 0, 0, 0, 0, 0, 0, 0);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDiscard();
                }
            });
        }
    }

    public void confirmDiscard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirm_discard));

        builder.setPositiveButton(getString(R.string.discard), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED);
                dialog.cancel();
                stopRecording();
                finish();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.i(TAG, "Permission Request Result");
//        if (REQUEST_LOCATION_PERMISSIONS == requestCode) {
//            locationPermissionsEnabled =true;
//        }
//    }

//    /* Request that location services are enabled */
//    private void requestLocationEnabled(){
//        locationManager = (LocationManager) this
//                .getSystemService(AppCompatActivity.LOCATION_SERVICE);
//
//        // getting GPS status
//        boolean isGPSEnabled = locationManager
//                .isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//        // getting network status
//        boolean isNetworkEnabled = locationManager
//                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            /* If both are disabled then request location be enabled */
//        if (!isGPSEnabled && !isNetworkEnabled) {
//            // location service disabled
//            Log.i("GPS: ", "Disabled");
//            Toast.makeText(this, getString(R.string.enable_location_service), Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//        }else if(!isGPSEnabled){
//            /* Network is enabled but not the gps */
//            showSettingsAlert("GPS Disabled");
//            locationEnabled = true;
//        } else{
//            locationEnabled = true;
//        }
//    }

    /* TODO: Add save function */

    /**
     * Function to show settings alert dialog
     * */
    public void showSettingsAlert(String title) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage("Would you like to change your settings?");
        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

//    private void startLocationListener() {
//        Log.i(TAG, "Start Location Listener");
//        isRecording = true;
//        try {
//            // getting GPS status
//            boolean isGPSEnabled = locationManager
//                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//            // getting network status
//            boolean isNetworkEnabled = locationManager
//                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            /* If both are disabled then request location be enabled */
//            if (!isGPSEnabled && !isNetworkEnabled) {
//                // location service disabled
//                Log.i("GPS: ", "Disabled");
//                Toast.makeText(this, "Enable Location Service", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            } else {
//                Log.i(TAG, "Location services are enabled");
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                }
//
//                if (isGPSEnabled) {
//                    locationManager.requestLocationUpdates(
//                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                    onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
//
//                } else if (isNetworkEnabled) {
//                    locationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                    onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
//                } else{
//                    Toast.makeText(this, "Enable Location Services", Toast.LENGTH_LONG).show();
//                }
//
//
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("Error : Location",
//                    "Impossible to connect to LocationManager", e);
//        }
//    }

    private void startLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    private void startRecording(){
        Log.i(TAG, "Start Timer");
        if(locationEnabled && locationPermissionsEnabled){
            startLocationListener();
            /* Start the timer */
            stopWatch.start();
            isRecording = true;
            /* Start the handler to request times from stopwatch */

            handler.post(updateTimer);
            if(recordRunItem.startTime == 0){
                recordRunItem.startTime = System.currentTimeMillis();
            }
        } else{
            if(!locationEnabled){
                //requestLocationEnabled();
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
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);

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
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Updated");
        if(location!= null){

            double currentSpeed = location.getSpeed();
            final double currentElevation = location.getAltitude();

            /* Add Speeds and Elevations */
            speed.allSpeeds.add(currentSpeed);
            elevation.allElevations.add(currentElevation);

            /* Convert speed and elevations to perferred units */
            double speedInUnits = currentSpeed * speedUnits.multiplier;
            final double elivationInUnits = currentElevation * elevationUnits.multiplier;

            tvCurrentSpeed.setText(String.format(Locale.getDefault(), "%.2f", speedInUnits));

            pastLocations.add(new PastLocation(location.getLatitude(), location.getLongitude(),
                    currentSpeed, currentElevation));

            /* Async class to calculate distance */
            double currentDistance = recordRunItem.distance;
            new CalculateRunStatsAsync(pastLocations, currentDistance, speed, elevation,
                    new RunCaclulationsListener() {
                        @Override
                        public void onCalculationsComplete(double distanceInMeters , double speedAvg,
                                                           double maxSpeed, double maxElevation,
                                                           double minElevation) {
                            tvDistance.setText(String.format(Locale.getDefault(), "%.1f",
                                    distanceInMeters * distanceUnits.multiplier));
                            recordRunItem.distance = distanceInMeters;

                            tvAvgSpeed.setText(String.format(Locale.getDefault(), "%.2f", speedAvg
                                    * speedUnits.multiplier));

                            tvMaxSpeed.setText(String.format(Locale.getDefault(), "%.2f",
                                    maxSpeed * speedUnits.multiplier));

                            if (currentElevation != 0){
                                tvCurrentElevation.setText(String.format(Locale.getDefault(), "%.1f", elivationInUnits));

                                tvMaxElevation.setText(String.format(Locale.getDefault(), "%.1f",
                                        maxElevation * elevationUnits.multiplier));

                                if(minElevation != 0){
                                    tvMinElevation.setText(String.format(Locale.getDefault(), "%.1f",
                                            minElevation * elevationUnits.multiplier));
                                }

                            }
                        }
                    }).execute();
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
        tvDistance = (TextView) findViewById(R.id.tvCurrentDistance);
        tvDistanceUnits = (TextView) findViewById(R.id.tvDistanceUnits);
        fabStartPauseRecording = (FloatingActionButton) findViewById(R.id.fabStartPauseRecording);
        fabStopRecording = (FloatingActionButton) findViewById(R.id.fabStopRecording);
    }

    /* Set views text and listeners */
    private void setupViews(){
        tvSpeedUnits.setText(speedUnits.label);
        tvElevationUnits.setText(elevationUnits.label);
        tvDistanceUnits.setText(distanceUnits.label);

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
                fabStopRecording.setVisibility(View.GONE);
                stopRecording();
                recordRunItem.duration = stopWatch.getElapsedTime();
                recordRunItem.maxSpeed = speed.getMaxSpeeds();
                recordRunItem.avgSpeed = speed.getAverageWithoutOutliers();
                recordRunItem.maxElevation = elevation.getMaxElevation();
                recordRunItem.minElevation = elevation.getMinElevation();

                new StoreRunInBackground(pastLocations, recordRunItem, databaseHelper, RecordRunActivity.this,
                        new StoringDataComplete() {
                            @Override
                            public void onDataStored() {
                                Log.i(TAG, "Data Stored");
                                setResult(RESULT_OK);
                                finish();
                            }
                        }).execute();
            }
        });
    }


    private AdsHelper adsHelper;
    /* TODO Test out different  ways to make smoother */
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


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }
}
