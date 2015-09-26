
package com.ksharkapps.musicnow.preferences;

import static com.ksharkapps.musicnow.preferences.SettingsActivity.KEY_ACTIONBAR_COLOR;
import static com.ksharkapps.musicnow.preferences.SettingsActivity.KEY_TRANSLUCENT_MODE;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.ksharkapps.musicnow.R;
import com.ksharkapps.musicnow.misc.Utils;


public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {


    public SettingsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Load settings XML
        int preferencesResId = R.xml.settings;
        addPreferencesFromResource(preferencesResId);
        
        Preference preferenceActionBar = findPreference(KEY_ACTIONBAR_COLOR);
		preferenceActionBar.setOnPreferenceChangeListener(this);
		
		  if(!Utils.hasKitKat()){
				PreferenceCategory pref = (PreferenceCategory) getPreferenceManager().findPreference("theme");
				Preference preference = findPreference(KEY_TRANSLUCENT_MODE);
				if(null != preference)
					pref.removePreference(preference);				
				
			}
		
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ((SettingsActivity)getActivity()).changeActionBarColor(0);
		return true;
	}

	
    
}
