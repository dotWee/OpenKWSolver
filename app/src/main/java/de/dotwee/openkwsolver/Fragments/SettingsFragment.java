/*
 * Copyright 2015 Lukas "dotwee" Wolfsteiner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dotwee.openkwsolver.Fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

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
    }
}