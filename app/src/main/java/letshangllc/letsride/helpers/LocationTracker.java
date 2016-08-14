package letshangllc.letsride.helpers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Carl on 8/3/2016.
 */
public class LocationTracker implements LocationListener {

    /* Calling Activity */
    private final AppCompatActivity appCompatActivity;

    // flag for GPS Status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    android.location.Location location;
    double latitude;
    double longitude;

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) 0.5;

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 2000;

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public LocationTracker(AppCompatActivity activity) {
        this.appCompatActivity = activity;
        getLocation();
    }

    public android.location.Location getLocation() {
        try {
            locationManager = (LocationManager) appCompatActivity
                    .getSystemService(AppCompatActivity.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // location service disabled
                Log.i("GPS: ", "Disabled");
                Toast.makeText(appCompatActivity, "Enable Location Service", Toast.LENGTH_LONG).show();
            } else {
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services

                /* Request permissions */
                if (ActivityCompat.checkSelfPermission(appCompatActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(appCompatActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(appCompatActivity, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
                    }, 0);

                }

                if (isGPSEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("GPS Enabled", "GPS Enabled");

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        //updateGPSCoordinates();
                    }
                }

                // Next get location from Network Provider if no previous location
                if (isNetworkEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("Network", "Network");

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            //updateGPSCoordinates();
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            Log.e("Error : Location",
                    "Impossible to connect to LocationManager", e);
        }

        return location;
    }

    public void updateGPSCoordinates(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void onLocationChanged(Location location) {
        //Toast.makeText(getApplicationContext(), : " + location.toString(), Toast.LENGTH_LONG).show();
        updateGPSCoordinates(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}