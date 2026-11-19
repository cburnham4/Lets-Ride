package letshangllc.letsride.activities.runinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;

/**
 * Ride path map using the current Google Maps SDK MapView.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = MapFragment.class.getSimpleName();

    private MapView mMapView;
    private GoogleMap mMap;
    private PastRunItem pastRunItem;

    public MapFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        Log.i(TAG, "On Create");
        Bundle args = getArguments();
        if (args != null) {
            pastRunItem = args.getParcelable(getString(R.string.past_run_item_extra));
        }
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        try {
            MapsInitializer.initialize(requireActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (pastRunItem != null && pastRunItem.pastLocations != null && pastRunItem.pastLocations.size() >= 2) {
            PastLocation startLocation = pastRunItem.pastLocations.get(0);
            PastLocation endLocation = pastRunItem.pastLocations.get(pastRunItem.pastLocations.size() - 1);
            LatLng start = new LatLng(startLocation.lat, startLocation.lon);
            LatLng end = new LatLng(endLocation.lat, endLocation.lon);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 15));

            mMap.addMarker(new MarkerOptions()
                    .position(start)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );

            mMap.addMarker(new MarkerOptions()
                    .position(end)
                    .title("Finish")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
            mMap.addPolyline(getPath());
        }
    }

    private PolylineOptions getPath() {
        PolylineOptions rectOptions = new PolylineOptions();
        int i = 0;
        for (PastLocation pastLocation : pastRunItem.pastLocations) {
            if (i % 2 == 0) {
                rectOptions.add(new LatLng(pastLocation.lat, pastLocation.lon)).color(Color.BLUE);
            }
            i++;
        }
        return rectOptions;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }
}
