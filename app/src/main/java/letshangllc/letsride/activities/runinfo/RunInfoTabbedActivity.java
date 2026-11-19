package letshangllc.letsride.activities.runinfo;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import letshangllc.letsride.R;
import letshangllc.letsride.data_objects.PastRunItem;
import letshangllc.letsride.helpers.EdgeToEdgeHelper;

public class RunInfoTabbedActivity extends AppCompatActivity {
    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_info_tabbed);
        EdgeToEdgeHelper.apply(this);

        PastRunItem pastRunItem = getIntent().getParcelableExtra(getString(R.string.past_run_item_extra));
        args = new Bundle();
        args.putParcelable(getString(R.string.past_run_item_extra), pastRunItem);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        ViewPager viewPager = findViewById(R.id.vp_run_info_activity);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tl_run_info_activity);
        tabLayout.setupWithViewPager(viewPager);
        if (tabLayout.getTabAt(0) != null) {
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_map_location);
        }
        if (tabLayout.getTabAt(1) != null) {
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_assignment_white_36dp);
        }
        if (tabLayout.getTabAt(2) != null) {
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_assessment_white_36dp);
        }
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

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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
}
