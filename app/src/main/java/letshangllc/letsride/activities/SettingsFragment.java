package letshangllc.letsride.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import letshangllc.letsride.R;

/**
 * Created by Carl on 8/11/2016.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_settings);
    }

}
