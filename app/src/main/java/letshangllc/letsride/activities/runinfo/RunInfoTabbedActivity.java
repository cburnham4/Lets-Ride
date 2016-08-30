package letshangllc.letsride.activities.runinfo;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.helpers.AdsHelper;

public class RunInfoTabbedActivity extends AppCompatActivity {
    private Bundle args;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_info_tabbed);

        PastRunItem pastRunItem = (PastRunItem) getIntent().getParcelableExtra(getString(R.string.past_run_item_extra));
        args = new Bundle();
        args.putParcelable(getString(R.string.past_run_item_extra), pastRunItem);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(toolbar != null){getSupportActionBar().setDisplayHomeAsUpEnabled(true);}
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewPager viewPager = (ViewPager) findViewById(R.id.vp_run_info_activity);

        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tl_run_info_activity);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_map_location);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_assignment_white_36dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_assessment_white_36dp);

        //this.runAds();
    }



    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Fragment mapFragment = new MapFragment();
        mapFragment.setArguments(args);
        adapter.addFragment(mapFragment, "Map");

        Fragment runStatsFragment = new RunStatsFragment();
        runStatsFragment.setArguments(args);
        adapter.addFragment(runStatsFragment, "Statistics");

        Fragment runGraphFragment = new RunGraphFragment();
        runGraphFragment.setArguments(args);
        adapter.addFragment(runGraphFragment, "Graphs");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    private AdsHelper adsHelper;
    public void runAds(){
        adsHelper =  new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_id_history), this);

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
