package de.dotwee.openkwsolver;

import android.os.Bundle;


public class PreferenceActivity extends android.preference.PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
