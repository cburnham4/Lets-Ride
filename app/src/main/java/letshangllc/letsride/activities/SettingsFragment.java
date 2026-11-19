package letshangllc.letsride.activities;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import letshangllc.letsride.R;

/**
 * Settings preferences (AndroidX PreferenceFragmentCompat).
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey);
    }
}
