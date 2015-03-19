/*
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *                     Version 2, December 2004
 *
 *  Copyright (C) 2015 Lukas "dotwee" Wolfsteiner <lukas@wolfsteiner.de>
 *
 *  Everyone is permitted to copy and distribute verbatim or modified
 *  copies of this license document, and changing it is allowed as long
 *  as the name is changed.
 *
 *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *   0. You just DO WHAT THE FUCK YOU WANT TO.
 *
 */

package de.dotwee.openkwsolver.Fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import de.dotwee.openkwsolver.R;

public class SettingsFragment extends PreferenceFragment {
	public final static String LOG_TAG = "PreferenceFragment";

	public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    Log.i(LOG_TAG, "onCreate");
	    addPreferencesFromResource(R.xml.pref_api);
	    addPreferencesFromResource(R.xml.pref_automation);
        addPreferencesFromResource(R.xml.pref_notification);
        addPreferencesFromResource(R.xml.pref_style);

	    Preference enableConfirm = findPreference("pref_layout_confirm");
	    enableConfirm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
			    Toast.makeText(getActivity(), "Restart to reload viewPager.",
					    Toast.LENGTH_SHORT).show();

			    return false;
		    }
	    });
    }
}