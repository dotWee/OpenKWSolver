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
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String preferenceKey = preference.getKey();

		switch (preferenceKey) {
			case "pref_layout_size":
				Log.i(LOG_TAG, "onPreferenceChange / KEY: " + preferenceKey);
				SolverFragment.imageViewCaptcha.getLayoutParams()
						.height = Integer.parseInt(newValue.toString());
				return false;

			case "pref_layout_captchaid":
				Log.i(LOG_TAG, "onPreferenceChange / KEY: " + preferenceKey);
				Log.i(LOG_TAG, "enableCaptchaID PreferenceListener / VALUE: " + newValue.toString());
				if (newValue.toString().equalsIgnoreCase("false")) {
					SolverFragment.textViewCaptchaDesc.setVisibility(View.GONE);
					SolverFragment.textViewCaptcha.setVisibility(View.GONE);
				} else {
					SolverFragment.textViewCaptchaDesc.setVisibility(View.VISIBLE);
					SolverFragment.textViewCaptcha.setVisibility(View.VISIBLE);
				}
				return false;

		}
		return false;
	}
}