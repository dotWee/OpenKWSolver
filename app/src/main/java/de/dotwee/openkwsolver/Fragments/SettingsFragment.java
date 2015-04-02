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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;

import de.dotwee.openkwsolver.R;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
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

	    addPreferencesFromResource(R.xml.pref_about);

	    if (!getResources().getBoolean(R.bool.isTablet)) {
		    addPreferencesFromResource(R.xml.pref_layout);
		    findPreference("pref_layout_size").setOnPreferenceChangeListener(this);
		    findPreference("pref_layout_captchaid").setOnPreferenceChangeListener(this);
	    } else if (!getResources().getBoolean(R.bool.isLandscape)) {
		    addPreferencesFromResource(R.xml.pref_layout);
		    findPreference("pref_layout_size").setOnPreferenceChangeListener(this);
		    findPreference("pref_layout_captchaid").setOnPreferenceChangeListener(this);
	    }

	    findPreference("pref_api_key").setOnPreferenceChangeListener(this);
	    findPreference("pref_about_issue").setOnPreferenceClickListener(this);
	    findPreference("pref_about_github").setOnPreferenceClickListener(this);
	    findPreference("pref_about_mail").setOnPreferenceClickListener(this);
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

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Log.i(LOG_TAG, "onPreferenceClick / ITEM: " + preference.getKey());
		String intentUri;

		if (preference == getPreferenceScreen().findPreference("pref_about_issue")) {
			intentUri = "https://github.com/dotWee/OpenKWSolver/issues/new";
		} else if (preference == getPreferenceScreen().findPreference("pref_about_github")) {
			intentUri = "https://github.com/dotWee/OpenKWSolver";
		} else if (preference == getPreferenceScreen().findPreference("pref_about_mail")) {
			intentUri = "mailto:coding@dotwee.de";
		} else {
			intentUri = null;
		}

		Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentUri));
		startActivity(linkIntent);
		getActivity().finish();
		return false;
	}
}