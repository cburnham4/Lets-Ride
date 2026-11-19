package letshangllc.letsride.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.async.CalculateRunStatsAsync;
import letshangllc.letsride.async.RunCaclulationsListener;
import letshangllc.letsride.async.StoreRunInBackground;
import letshangllc.letsride.async.StoringDataComplete;
import letshangllc.letsride.data.LocationDatabaseHelper;
import letshangllc.letsride.data_objects.Elevation;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.RecordRunItem;
import letshangllc.letsride.data_objects.Speed;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;
import letshangllc.letsride.helpers.AdsHelper;
import letshangllc.letsride.helpers.EdgeToEdgeHelper;
import letshangllc.letsride.helpers.StopWatch;

public class RecordRunActivity extends AppCompatActivity {
    private final static String TAG = RecordRunActivity.class.getSimpleName();

    private final static int REQUEST_LOCATION_PERMISSIONS = 0;
    private final static int REQUEST_CHECK_SETTINGS = 1;

    private long MIN_TIME_BW_UPDATES = 3000;

    /* Recording Variables — units stored as meters and meters per second */
    private boolean isRecording = false;
    private Speed speed = new Speed();
    private Elevation elevation = new Elevation();
    private ArrayList<PastLocation> pastLocations = new ArrayList<>();
    private RecordRunItem recordRunItem;

    private StopWatch stopWatch = new StopWatch();
    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean locationEnabled = false;
    private boolean locationPermissionsEnabled;

    private SpeedUnits speedUnits;
    private LengthUnits elevationUnits;
    private LengthUnits distanceUnits;

    private TextView tvCurrentSpeed, tvMaxSpeed, tvAvgSpeed, tvCurrentElevation, tvMaxElevation,
            tvMinElevation, tvDuration, tvSpeedUnits, tvElevationUnits, tvDistance, tvDistanceUnits;
    private FloatingActionButton fabStartPauseRecording, fabStopRecording;

    private LocationDatabaseHelper databaseHelper;
    private int dayId;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;

    private AdsHelper adsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_run);
        EdgeToEdgeHelper.apply(this);

        adsHelper = new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_ad_id_record), this);
        adsHelper.setUpAds();

        this.setupLocationApi();
        this.getUserData();
        this.findViews();
        this.setupToolbar();
        this.setupViews();
        this.requestPermission();
    }

    private void setupLocationApi() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(MIN_TIME_BW_UPDATES)
                .setMinUpdateDistanceMeters(3f)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    onLocationChanged(location);
                }
            }
        };

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationEnabled = true;
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ((ResolvableApiException) e).startResolutionForResult(
                                RecordRunActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                }
            }
        });
    }

    private void getUserData() {
        Bundle extras = getIntent().getExtras();
        dayId = extras != null ? extras.getInt(getString(R.string.day_id_extra), 0) : 0;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int speedUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_speed_unit_index), "0"));
        int elevationUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_elevation_index), "0"));
        int distanceUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_distance_index), "0"));
        MIN_TIME_BW_UPDATES = 1000L * settings.getInt(getString(R.string.user_pref_min_time_loc_request), 3);

        speedUnits = SpeedUnits.getSpeedUnit(speedUnitIndex);
        elevationUnits = LengthUnits.getLengthUnits(elevationUnitIndex);
        distanceUnits = LengthUnits.getLengthUnits(distanceUnitIndex);

        databaseHelper = new LocationDatabaseHelper(this);
        recordRunItem = new RecordRunItem(dayId, 0, 0, 0, 0, 0, 0, 0);

        // Rebuild location request with user interval preference
        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, MIN_TIME_BW_UPDATES)
                .setMinUpdateIntervalMillis(Math.max(1000, MIN_TIME_BW_UPDATES / 2))
                .setMinUpdateDistanceMeters(3f)
                .build();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null && getSupportActionBar() != null) {
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
        builder.create().show();
    }

    private void requestPermission() {
        Log.i(TAG, "Request Permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.enable_location_service), Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_LOCATION_PERMISSIONS);
        } else {
            locationPermissionsEnabled = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            locationPermissionsEnabled = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void showSettingsAlert(String title) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage("Would you like to change your settings?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void startLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.getMainLooper());
    }

    private void startRecording() {
        Log.i(TAG, "Start Timer");
        if (locationEnabled && locationPermissionsEnabled) {
            startLocationListener();
            stopWatch.start();
            isRecording = true;
            handler.post(updateTimer);
            if (recordRunItem.startTime == 0) {
                recordRunItem.startTime = System.currentTimeMillis();
            }
        } else {
            if (!locationPermissionsEnabled) {
                requestPermission();
            }
            if (!locationEnabled) {
                showSettingsAlert("Location Disabled");
            }
        }
    }

    private void stopRecording() {
        Log.i(TAG, "Stop Recording ");
        isRecording = false;
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        stopWatch.stop();
        handler.removeCallbacks(updateTimer);
    }

    private void pauseRecording() {
        isRecording = false;
        Log.i(TAG, "PAUSE");
        stopWatch.pause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void onLocationChanged(Location location) {
        Log.i(TAG, "Location Updated");
        double currentSpeed = location.getSpeed();
        final double currentElevation = location.getAltitude();

        speed.allSpeeds.add(currentSpeed);
        elevation.allElevations.add(currentElevation);

        double speedInUnits = currentSpeed * speedUnits.multiplier;
        final double elivationInUnits = currentElevation * elevationUnits.multiplier;

        tvCurrentSpeed.setText(String.format(Locale.getDefault(), "%.2f", speedInUnits));

        pastLocations.add(new PastLocation(location.getLatitude(), location.getLongitude(),
                currentSpeed, currentElevation));

        double currentDistance = recordRunItem.distance;
        new CalculateRunStatsAsync(pastLocations, currentDistance, speed, elevation,
                new RunCaclulationsListener() {
                    @Override
                    public void onCalculationsComplete(double distanceInMeters, double speedAvg,
                                                       double maxSpeed, double maxElevation,
                                                       double minElevation) {
                        tvDistance.setText(String.format(Locale.getDefault(), "%.1f",
                                distanceInMeters * distanceUnits.multiplier));
                        recordRunItem.distance = distanceInMeters;

                        tvAvgSpeed.setText(String.format(Locale.getDefault(), "%.2f", speedAvg
                                * speedUnits.multiplier));

                        tvMaxSpeed.setText(String.format(Locale.getDefault(), "%.2f",
                                maxSpeed * speedUnits.multiplier));

                        if (currentElevation != 0) {
                            tvCurrentElevation.setText(String.format(Locale.getDefault(), "%.1f", elivationInUnits));

                            tvMaxElevation.setText(String.format(Locale.getDefault(), "%.1f",
                                    maxElevation * elevationUnits.multiplier));

                            if (minElevation != 0) {
                                tvMinElevation.setText(String.format(Locale.getDefault(), "%.1f",
                                        minElevation * elevationUnits.multiplier));
                            }
                        }
                    }
                }).execute();
    }

    private void findViews() {
        tvCurrentSpeed = findViewById(R.id.tvCurrentSpeed);
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed);
        tvAvgSpeed = findViewById(R.id.tvAvgSpeed);
        tvCurrentElevation = findViewById(R.id.tvCurrentElevation);
        tvMaxElevation = findViewById(R.id.tvMaxElevation);
        tvMinElevation = findViewById(R.id.tvMinElevation);
        tvDuration = findViewById(R.id.tvDuration);
        tvSpeedUnits = findViewById(R.id.tvSpeedUnits);
        tvElevationUnits = findViewById(R.id.tvElevationUnits);
        tvDistance = findViewById(R.id.tvCurrentDistance);
        tvDistanceUnits = findViewById(R.id.tvDistanceUnits);
        fabStartPauseRecording = findViewById(R.id.fabStartPauseRecording);
        fabStopRecording = findViewById(R.id.fabStopRecording);
    }

    private void setupViews() {
        tvSpeedUnits.setText(speedUnits.label);
        tvElevationUnits.setText(elevationUnits.label);
        tvDistanceUnits.setText(distanceUnits.label);

        fabStartPauseRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    fabStartPauseRecording.setImageResource(R.drawable.ic_pause_white_48dp);
                    fabStopRecording.setVisibility(View.GONE);
                    startRecording();
                } else {
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

    public Runnable updateTimer = new Runnable() {
        public void run() {
            if (isRecording) {
                int[] times = stopWatch.getHourMinSecs();
                tvDuration.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", times[0],
                        times[1], times[2]));
                adsHelper.refreshAd();
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            locationEnabled = resultCode == RESULT_OK;
        }
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
        stopRecording();
        if (adsHelper != null) {
            adsHelper.onDestroy();
        }
        super.onDestroy();
    }
}
