package letshangllc.letsride.activities.runinfo;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    MapView mMapView;
    private GoogleMap mMap;
    private PastRunItem pastRunItem;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView  = inflater.inflate(R.layout.fragment_map, container, false);
        Bundle args = getArguments();
        pastRunItem = args.getParcelable(getString(R.string.past_run_item_extra));
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(pastRunItem.pastLocations.size()>=2){
            PastLocation startLocation = pastRunItem.pastLocations.get(0);
            PastLocation endLocation = pastRunItem.pastLocations.get(pastRunItem.pastLocations.size()-1);
            LatLng start = new LatLng(startLocation.lat, startLocation.lon);
            LatLng end = new LatLng(endLocation.lat, endLocation.lon);

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
        int i = 0;
        for(PastLocation pastLocation: pastRunItem.pastLocations){
            if(i%2 ==0){
                rectOptions.add(new LatLng(pastLocation.lat, pastLocation.lon)).color(Color.BLUE);
            }i++;

        }

        return rectOptions;
    }

}
