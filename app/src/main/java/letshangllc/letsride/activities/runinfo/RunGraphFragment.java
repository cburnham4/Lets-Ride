package letshangllc.letsride.activities.runinfo;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastLocation;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;
import letshangllc.letsride.helpers.DataHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class RunGraphFragment extends Fragment {
    private PastRunItem pastRunItem;

    /* Units */
    private SpeedUnits speedUnits;
    private LengthUnits elevationUnits;
    private LengthUnits distanceUnits;

    /* Views */
    private GraphView graphSpeed;
    private GraphView graphElevation;

    /* Datapoints */
    private LineGraphSeries<DataPoint> speedPoints;
    private LineGraphSeries<DataPoint> elevationPoints;



    public RunGraphFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_run_graph, container, false);

        Bundle args = getArguments();
        pastRunItem = args.getParcelable(getString(R.string.past_run_item_extra));
        this.getUnits();
        this.getDatapoints();
        this.setupGraphs(rootView);





        return rootView;
    }

    private void getDatapoints(){
        elevationPoints = new LineGraphSeries<>();
        speedPoints = new LineGraphSeries<>();

        /* TODO: Smooth data set and remove outliers */
        ArrayList<Double> elevations = new ArrayList<>();
        ArrayList<Double> speeds = new ArrayList<>();
        for(PastLocation pastLocation : pastRunItem.pastLocations){
            elevations.add(pastLocation.elevation);
            speeds.add(pastLocation.speed);
        }

        ArrayList<Double> normalSpeeds = DataHelper.getNormalDataSet(speeds);
        ArrayList<Double> normalElevations = DataHelper.getNormalDataSet(elevations);

        int i = 0;
        for (; i <normalElevations.size() && i <normalSpeeds.size(); i++){
            elevationPoints.appendData(new DataPoint(i+1,
                    normalElevations.get(i) * elevationUnits.multiplier), true, pastRunItem.pastLocations.size());
            speedPoints.appendData(new DataPoint(i+1,
                    normalSpeeds.get(i) * speedUnits.multiplier), true, pastRunItem.pastLocations.size());
        }

//        int x =1;
//        for(PastLocation pastLocation : pastRunItem.pastLocations){
//            elevationPoints.appendData(new DataPoint(x,
//                    pastLocation.elevation * elevationUnits.multiplier), true, pastRunItem.pastLocations.size());
//            speedPoints.appendData(new DataPoint(x,
//                    pastLocation.speed * speedUnits.multiplier), true, pastRunItem.pastLocations.size());
//            x++;
//        }
    }

    private void setupGraphs(View rootView){
        graphSpeed = (GraphView) rootView.findViewById(R.id.graphSpeed);
        graphElevation = (GraphView) rootView.findViewById(R.id.graphElevation);

        graphElevation.setTitle(String.format(Locale.getDefault(), "Elevation (%s)", elevationUnits.label));
        graphSpeed.setTitle(String.format(Locale.getDefault(),"Speed (%s)", speedUnits.label));

        graphSpeed.getGridLabelRenderer().setPadding(8);
        graphElevation.getGridLabelRenderer().setPadding(8);

        graphSpeed.getGridLabelRenderer().setNumHorizontalLabels(3);
        graphElevation.getGridLabelRenderer().setNumHorizontalLabels(3);

        graphSpeed.addSeries(speedPoints);
        graphElevation.addSeries(elevationPoints);

        graphSpeed.getViewport().setXAxisBoundsManual(true);
        graphElevation.getViewport().setXAxisBoundsManual(true);

        graphSpeed.getViewport().setMinX(0);
        graphSpeed.getViewport().setMaxX(pastRunItem.pastLocations.size());

        graphElevation.getViewport().setMinX(0);
        graphElevation.getViewport().setMaxX(pastRunItem.pastLocations.size());

    }

    private void getUnits(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        int speedUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_speed_unit_index), "0"));
        int elevationUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_elevation_index), "0"));
        int distanceUnitIndex = Integer.parseInt(settings.getString(getString(R.string.user_pref_distance_index), "0"));

        speedUnits = SpeedUnits.getSpeedUnit(speedUnitIndex);
        elevationUnits = LengthUnits.getLengthUnits(elevationUnitIndex);
        distanceUnits =LengthUnits.getLengthUnits(distanceUnitIndex);
    }



}
