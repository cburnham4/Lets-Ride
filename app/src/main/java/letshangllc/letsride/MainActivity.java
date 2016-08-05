package letshangllc.letsride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int REQUEST_LOCATION_PERMISSIONS = 0;

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) 0.5;

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 2000;

    /* Recording Variables */
    private boolean isRecording = false;
    private double maxSpeed = -1;
    private double minElevation = Double.MAX_VALUE;
    private double maxElevation = -1;
    private Speed speed = new Speed();

    /* Timing Variables */
    private StopWatch stopWatch = new StopWatch();
    private Handler handler = new Handler();

    /* Location Variables */
    private LocationManager locationManager;

    /* VIEWS */
    private TextView tvCurrentSpeed, tvMaxSpeed, tvAvgSpeed, tvCurrentElevation, tvMaxElevation,
            tvMinElevation, tvDuration;
    private FloatingActionButton fabStartPauseRecording, fabStopRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
    }

    private void requestPermissionAndStart() {
        /* If permissions not allowed then request them */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_LOCATION_PERMISSIONS);

        } else {
            startLocationListener();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_LOCATION_PERMISSIONS == requestCode) {
            startLocationListener();
        }
    }

    /* TODO: Create Enable location method */
    private void startLocationListener() {
        isRecording = true;
        try {
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
                Toast.makeText(this, "Enable Location Service", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }

                if (isNetworkEnabled && isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    startTimer();
                } else if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    startTimer();
                } else if(isGPSEnabled){
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    startTimer();
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

    private void startTimer(){
        /* Start the timer */
        stopWatch.start();
        /* Start the handler to request times from stopwatch */
        handler.post(updateTimer);
    }

    private void stopRecording() {
        isRecording = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Enbable Location Services", Toast.LENGTH_LONG).show();
            return;
        }
        tvDuration.setText(getString(R.string.time_zero));
        locationManager.removeUpdates(this);
        stopWatch.stop();
        speed.speeds.clear();
        handler.removeCallbacks(updateTimer);
    }

    private void pauseRecording(){
        isRecording = false;
        Log.i(TAG, "PAUSE");
        stopWatch.pause();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Updated");
        if(location!= null){
            double currentSpeed = location.getSpeed();
            double elivation = location.getAltitude();
            tvCurrentSpeed.setText(String.format(Locale.getDefault(), "%.2f", currentSpeed));

            speed.speeds.add(currentSpeed);

            tvAvgSpeed.setText(String.format(Locale.getDefault(), "%.2f", speed.getAverageSpeed()));

            if(currentSpeed>maxSpeed){
                maxSpeed = currentSpeed;
                tvMaxSpeed.setText(String.format(Locale.getDefault(), "%.2f", currentSpeed));
            }

            /* Do nothing if elivation is 0 because it is not accurate */
            if (elivation != 0){
                tvCurrentElevation.setText(String.format(Locale.getDefault(), "%.1f", elivation));

                if(elivation  > maxElevation){
                    tvMaxElevation.setText(String.format(Locale.getDefault(), "%.1f", elivation));
                    maxElevation = elivation;
                }
                if(elivation < minElevation && minElevation!= 0){
                    tvMinElevation.setText(String.format(Locale.getDefault(), "%.1f", elivation));
                    minElevation = elivation;
                }
            }



        }
    }

    private void findViews(){
        tvCurrentSpeed = (TextView) findViewById(R.id.tvCurrentSpeed);
        tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
        tvAvgSpeed = (TextView) findViewById(R.id.tvAvgSpeed);
        tvCurrentElevation = (TextView) findViewById(R.id.tvCurrentElevation);
        tvMaxElevation = (TextView) findViewById(R.id.tvMaxElevation);
        tvMinElevation = (TextView) findViewById(R.id.tvMinElevation);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        fabStartPauseRecording = (FloatingActionButton) findViewById(R.id.fabStartPauseRecording);
        fabStopRecording = (FloatingActionButton) findViewById(R.id.fabStopRecording);

        fabStartPauseRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* If not recording then start recording */
                if(!isRecording){
                    fabStartPauseRecording.setImageResource(R.drawable.ic_pause_white_48dp);
                    fabStopRecording.setVisibility(View.GONE);
                    requestPermissionAndStart();
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
                handler.postDelayed(this, 300);
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
}
