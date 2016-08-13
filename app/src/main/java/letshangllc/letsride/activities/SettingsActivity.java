package letshangllc.letsride.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import letshangllc.letsride.objects.AdsHelper;
import letshangllc.letsride.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.setupToolbar();
        this.runAds();
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(toolbar != null){getSupportActionBar().setDisplayHomeAsUpEnabled(true);}
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
