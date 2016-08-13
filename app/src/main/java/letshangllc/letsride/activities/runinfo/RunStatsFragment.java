package letshangllc.letsride.activities.runinfo;


import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import letshangllc.letsride.R;
import letshangllc.letsride.adapter.HistoryItemsAdapter;
import letshangllc.letsride.adapter.RunStatsAdapter;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.enums.LengthUnits;
import letshangllc.letsride.enums.SpeedUnits;
import letshangllc.letsride.objects.RunStatItem;
import letshangllc.letsride.objects.StopWatch;

/**
 * A simple {@link Fragment} subclass.
 */
public class RunStatsFragment extends Fragment {
    private PastRunItem pastRunItem;
    private ArrayList<RunStatItem> runStatItems;

    /* Units */
    private SpeedUnits speedUnits;
    private LengthUnits elevationUnits;
    private LengthUnits distanceUnits;

    private final static String TAG = RunStatsFragment.class.getSimpleName();

    public RunStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_run_stats, container, false);

        Bundle args = getArguments();
        pastRunItem = args.getParcelable(getString(R.string.past_run_item_extra));

        getUnits();
        createStatItems();
        setupRecycleView(rootView);

        return rootView;
    }

    private void createStatItems(){
        runStatItems = new ArrayList<>();

        addDuration();
        addDistance();
        addSpeeds();
        addElevations();
        addStartTime();

    }

    private void addDuration(){
        int[] times = StopWatch.milliToHourMinSecs(pastRunItem.durationInMilli);
        String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", times[0],
                times[1],times[2]);
        runStatItems.add(new RunStatItem(R.drawable.ic_alarm_black_36dp, "Duration", time));
    }

    private void addDistance(){
        String distance = String.format(Locale.getDefault(), "%.1f %s",
                pastRunItem.distance * distanceUnits.multiplier, distanceUnits.label);
        runStatItems.add(new RunStatItem(R.drawable.ic_distance_traveled, "Distance", distance));
    }

    private void addSpeeds(){
        String maxSpeed = String.format(Locale.getDefault(), "%.1s %s",
                pastRunItem.maxSpeed * speedUnits.multiplier, speedUnits.label);
        String avgSpeed = String.format(Locale.getDefault(), "%.1f %s",
                pastRunItem.avgSpeed * speedUnits.multiplier, speedUnits.label);

        runStatItems.add(new RunStatItem(R.drawable.ic_speedometer, "Max. Speed", maxSpeed));
        runStatItems.add(new RunStatItem(R.drawable.ic_speedometer, "Avg. Speed", avgSpeed));
    }

    private void addElevations(){
        String maxElevation = String.format(Locale.getDefault(), "%.1f %s",
                pastRunItem.maxElevation * elevationUnits.multiplier, elevationUnits.label);
        String minElevation = String.format(Locale.getDefault(), "%.1f %s",
                pastRunItem.minElevation * elevationUnits.multiplier, elevationUnits.label);
        double elevationChange = pastRunItem.minElevation - pastRunItem.maxElevation;
        String change = String.format(Locale.getDefault(), "%.1f %s",
                elevationChange * elevationUnits.multiplier, elevationUnits.label);

        runStatItems.add(new RunStatItem(R.drawable.ic_snowed_mountains, "Max. Elevation", maxElevation));
        runStatItems.add(new RunStatItem(R.drawable.ic_snowed_mountains, "Min. Elevation", minElevation));
        runStatItems.add(new RunStatItem(R.drawable.ic_snowed_mountains, "Elevation Change",change));
    }

    private void addStartTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(pastRunItem.startTime);
        Log.i(TAG, " time: " + pastRunItem.startTime);
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        runStatItems.add(new RunStatItem(R.drawable.ic_access_time_black_36dp, "Start Time", dateFormat.format(date)));
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

    private void setupRecycleView(View view) {

        RunStatsAdapter runStatsAdapter = new RunStatsAdapter(runStatItems, this.getContext());;

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvRunStats);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(runStatsAdapter);
    }



}
