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
import android.view.View;

import de.dotwee.openkwsolver.R;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
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

	    findPreference("pref_layout_captchaid").setOnPreferenceChangeListener(this);
	    findPreference("pref_layout_size").setOnPreferenceChangeListener(this);
	    findPreference("pref_api_key").setOnPreferenceChangeListener(this);
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.i(LOG_TAG, "onPreferenceChange / VALUE: " + newValue.toString());
		String preferenceKey = preference.getKey();
		Log.i(LOG_TAG, "onPreferenceChange / KEY: " + preferenceKey);
		if (preferenceKey.equalsIgnoreCase("pref_layout_size")) {
			SolverFragment.imageViewCaptcha.getLayoutParams().height =
					Integer.parseInt(newValue.toString());

		} else if (preferenceKey.equalsIgnoreCase("pref_layout_captchaid")) {
			if (newValue.toString().equalsIgnoreCase("false")) {
				SolverFragment.textViewCaptchaDesc.setVisibility(View.GONE);
				SolverFragment.textViewCaptcha.setVisibility(View.GONE);
			} else {
				SolverFragment.textViewCaptchaDesc.setVisibility(View.VISIBLE);
				SolverFragment.textViewCaptcha.setVisibility(View.VISIBLE);
			}
		} else if (preferenceKey.equalsIgnoreCase("pref_api_key")) {
			SolverFragment.buttonPull.setEnabled(newValue.toString() != null);
			SolverFragment.buttonSkip.setEnabled(newValue.toString() != null);
			SolverFragment.buttonSend.setEnabled(newValue.toString() != null);
		}
		return true;
	}
}