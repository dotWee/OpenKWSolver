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

import java.util.ArrayList;

import de.dotwee.openkwsolver.R;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
	public final static String LOG_TAG = "PreferenceFragment";
	private ArrayList<Preference> preferencesClickList;

	public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    Log.i(LOG_TAG, "onCreate");
		setPreferencesView();
	    for (final Preference preference : preferencesClickList()) {
		    preference.setOnPreferenceClickListener(this);
	    }
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.i(LOG_TAG, "onPreferenceChange / VALUE: " + newValue.toString());
		String preferenceKey = preference.getKey();
		Log.i(LOG_TAG, "onPreferenceChange / KEY: " + preferenceKey);

		switch (preferenceKey) {
			case "pref_layout_size":
				SolverFragment.imageViewCaptcha.getLayoutParams().height = Integer.parseInt(newValue.toString());

			case "pref_layout_captchaid":
				if (newValue.toString().equalsIgnoreCase("false")) {
					SolverFragment.textViewCaptchaDesc.setVisibility(View.GONE);
					SolverFragment.textViewCaptcha.setVisibility(View.GONE);
				} else {
					SolverFragment.textViewCaptchaDesc.setVisibility(View.VISIBLE);
					SolverFragment.textViewCaptcha.setVisibility(View.VISIBLE);
				}

			case "pref_api_key":
				SolverFragment.buttonPull.setEnabled(newValue.toString() != null);
				SolverFragment.buttonSkip.setEnabled(newValue.toString() != null);
				SolverFragment.buttonSend.setEnabled(newValue.toString() != null);
		}

		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Log.i(LOG_TAG, "onPreferenceClick / ITEM: " + preference.getKey());

		switch (preference.getKey()) {
			case "pref_about_issue": startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dotWee/OpenKWSolver/issues/new")));
			case "pref_about_github": startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dotWee/OpenKWSolver")));
			case "pref_about_mail": startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:coding@dotwee.de")));
		} getActivity().finish();

		return false;
	}

	private ArrayList<Preference> preferencesClickList() {
		preferencesClickList = new ArrayList<>();
		preferencesClickList.add(findPreference("pref_api_key"));
		preferencesClickList.add(findPreference("pref_about_issue"));
		preferencesClickList.add(findPreference("pref_about_github"));
		preferencesClickList.add(findPreference("pref_about_mail"));
		return preferencesClickList;
	}

	private void setPreferencesView() {
		addPreferencesFromResource(R.xml.pref_api);
		addPreferencesFromResource(R.xml.pref_automation);
		addPreferencesFromResource(R.xml.pref_notification);
		addPreferencesFromResource(R.xml.pref_about);

		// check if tablet or landscape
		if (!getResources().getBoolean(R.bool.isTablet) || !getResources().getBoolean(R.bool.isLandscape)) {
			addPreferencesFromResource(R.xml.pref_layout);
			findPreference("pref_layout_size").setOnPreferenceChangeListener(this);
			findPreference("pref_layout_captchaid").setOnPreferenceChangeListener(this);
		}
	}
}