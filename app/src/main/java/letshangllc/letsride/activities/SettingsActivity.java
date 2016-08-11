package letshangllc.letsride.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import letshangllc.letsride.objects.AdsHelper;
import letshangllc.letsride.R;

public class SettingsActivity extends AppCompatActivity {

    /* Views */
    private TextView tvMPHSelection, tvMPSSelection, tvKMHSelection,
        tvFeetSelection, tvMileSelection, tvMeterSelection, tvKiloMeterSelection;
    private TextView[] tvSpeedSelections, tvElevationSelections;

    private int speedUnitIndex, elevationUnitIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getValues();
        findViews();
        this.runAds();
    }

    private void getValues(){
        SharedPreferences settings = getSharedPreferences(getString(R.string.user_preferences), 0);
        speedUnitIndex = settings.getInt(getString(R.string.user_pref_speed_unit_index), 0 );
        elevationUnitIndex = settings.getInt(getString(R.string.user_pref_elevation_index),0 );


    }

    private  void findViews(){
        tvMPHSelection = (TextView) findViewById(R.id.tvMPHSelection);
        tvMPSSelection = (TextView) findViewById(R.id.tvMPSSelection);
        tvKMHSelection = (TextView) findViewById(R.id.tvKMHSelection);
        tvFeetSelection = (TextView) findViewById(R.id.tvFeetSelection);
        tvMileSelection = (TextView) findViewById(R.id.tvMileSelection);
        tvMeterSelection = (TextView) findViewById(R.id.tvMeterSelection);
        tvKiloMeterSelection = (TextView) findViewById(R.id.tvKiloMeterSelection);

        tvSpeedSelections = new TextView[]{tvMPHSelection, tvMPSSelection,tvKMHSelection};
        tvMPHSelection.setOnClickListener(new OnClickSpeedSelection(0));
        tvMPSSelection.setOnClickListener(new OnClickSpeedSelection(1));
        tvKMHSelection.setOnClickListener(new OnClickSpeedSelection(2));

        tvElevationSelections = new TextView[]{tvFeetSelection, tvMileSelection, tvMeterSelection,
        tvKiloMeterSelection};
        tvFeetSelection.setOnClickListener(new OnClickElevationSelection(0));
        tvMileSelection.setOnClickListener(new OnClickElevationSelection(1));
        tvMeterSelection.setOnClickListener(new OnClickElevationSelection(2));
        tvKiloMeterSelection.setOnClickListener(new OnClickElevationSelection(3));

        setElevationSelection(elevationUnitIndex);
        setSpeedSelection(speedUnitIndex);

        Button btnConfirmSetting = (Button) findViewById(R.id.btnConfirmSettings);
        btnConfirmSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settings = getSharedPreferences(getString(R.string.user_preferences), 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(getString(R.string.user_pref_speed_unit_index), speedUnitIndex);
                editor.putInt(getString(R.string.user_pref_elevation_index), elevationUnitIndex);

                editor.commit();
                SettingsActivity.this.finish();
            }
        });
    }

    private void setSpeedSelection(int index){
        for(TextView textView: tvSpeedSelections){
            textView.setBackgroundColor(getResources().getColor(R.color.primary_light));
            textView.setTextColor(getResources().getColor(R.color.black));
        }
        tvSpeedSelections[index].setBackgroundColor(getResources().getColor(R.color.primary_dark));
        tvSpeedSelections[index].setTextColor(getResources().getColor(R.color.white));
    }

    private class OnClickSpeedSelection implements View.OnClickListener{
        private int index;

        public OnClickSpeedSelection(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            setSpeedSelection(index);
            speedUnitIndex = index;
        }
    }

    private void setElevationSelection(int index){
        for(TextView textView: tvElevationSelections){
            textView.setBackgroundColor(getResources().getColor(R.color.primary_light));
            textView.setTextColor(getResources().getColor(R.color.black));
        }
        tvElevationSelections[index].setBackgroundColor(getResources().getColor(R.color.primary_dark));
        tvElevationSelections[index].setTextColor(getResources().getColor(R.color.white));
    }

    private class OnClickElevationSelection implements View.OnClickListener{
        private int index;

        public OnClickElevationSelection(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View view) {
            setElevationSelection(index);
            elevationUnitIndex = index;
        }
    }

    private AdsHelper adsHelper;
    public void runAds(){
        adsHelper =  new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_ad_id_settings), this);

        adsHelper.setUpAds();
        int delay = 1000; // delay for 1 sec.
        int period = getResources().getInteger(R.integer.ad_refresh_rate);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                adsHelper.refreshAd();  // display the data
            }
        }, delay, period);
    }
}
