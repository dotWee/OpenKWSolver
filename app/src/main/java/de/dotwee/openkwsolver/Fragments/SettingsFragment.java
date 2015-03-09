package de.dotwee.openkwsolver.Fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.dotwee.openkwsolver.R;

/**
 * Created by Lukas on 27.02.2015.
 */

public class SettingsFragment extends PreferenceFragment {
    public SettingsFragment() {

    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_api);
        addPreferencesFromResource(R.xml.pref_automation);
        addPreferencesFromResource(R.xml.pref_notification);
    }
}