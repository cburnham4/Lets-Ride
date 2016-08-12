package letshangllc.letsride.activities.runinfo;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private PastRunItem pastRunItem;
    //private ArrayList<PastLocation> pastLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        pastRunItem = (PastRunItem) getIntent().getParcelableExtra(getString(R.string.past_run_item_extra));


        if(pastRunItem.pastLocations.size()>=2){
            PastLocation startLocation = pastRunItem.pastLocations.get(0);
            PastLocation endLocation = pastRunItem.pastLocations.get(pastRunItem.pastLocations.size()-1);
            LatLng start = new LatLng(startLocation.lat, startLocation.lon);
            LatLng end = new LatLng(endLocation.lat, startLocation.lon);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 15));

            /* Add Start Marker */
            mMap.addMarker(new MarkerOptions()
                    .position(start)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );

            /* Add Finish Marker */
            mMap.addMarker(new MarkerOptions()
                    .position(end)
                    .title("Finish")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );

        }

        mMap.addPolyline(getPath());
    }

    private PolylineOptions getPath(){
        // Instantiates a new Polyline object and adds points to define a rectangle
        PolylineOptions rectOptions = new PolylineOptions();

        //Log.i(TAG, "LOCATIONS: " + pastRunItem.pastLocations.size());
        for(PastLocation pastLocation: pastRunItem.pastLocations){

            rectOptions.add(new LatLng(pastLocation.lat, pastLocation.lon)).color(Color.BLUE);
        }

        return rectOptions;
    }
}
