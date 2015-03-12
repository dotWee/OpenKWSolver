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

package de.dotwee.openkwsolver;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.dotwee.openkwsolver.Fragments.ConfirmFragment;
import de.dotwee.openkwsolver.Fragments.SettingsFragment;
import de.dotwee.openkwsolver.Fragments.SolverFragment;
import de.dotwee.openkwsolver.Tools.DownloadContentTask;

public class MainActivity extends ActionBarActivity {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_NOCAPTCHA = "&nocaptcha=1";
    public static final String URL_PARAMETER_CAPTCHA_NEW = "?action=usercaptchanew";
    public static final String URL_PARAMETER_SERVER_BALANCE = "?action=usercaptchaguthaben";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    public static final String URL_PARAMETER_CAPTCHA_SKIP = "?action=usercaptchaskip";
    private final static String LOG_TAG = "MainActivity";
    ViewPager viewPager;

    public static String requestCaptchaID(Context context, Boolean LOOP, int TYPE) {
        String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_NEW + getApiKey(context) +
                URL_PARAMETER_SOURCE + getExternalParameter(context, TYPE) + URL_PARAMETER_NOCAPTCHA);

        Log.i(LOG_TAG, "ID Request URL: " + CAPTCHA_URL);
        String CAPTCHA_ID = "";
        if (LOOP)
            try {
                CAPTCHA_ID = new DownloadContentTask().execute(CAPTCHA_URL, "captchaid").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        else try {
            CAPTCHA_ID = new DownloadContentTask().execute(CAPTCHA_URL, "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "ID Request RETURN: " + CAPTCHA_ID);

        return CAPTCHA_ID;
    }

    public static void skipCaptchaByID(Context context, String CAPTCHA_ID) {
        String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_SKIP + "&id=" + CAPTCHA_ID +
                getApiKey(context) + URL_PARAMETER_SOURCE);
        Log.i(LOG_TAG, "SKIP Request URL: " + CAPTCHA_URL);

        try {
            String res = new DownloadContentTask().execute(CAPTCHA_URL, "").get(5000, TimeUnit.MILLISECONDS);
            Toast.makeText(context, "Return: " + res, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static String getApiKey(Context context) {
        SharedPreferences pref_apikey = PreferenceManager
                .getDefaultSharedPreferences(context);
        String apikey = pref_apikey.getString("pref_api_key", null);
        Log.i(LOG_TAG, "API-Key: " + apikey);
        if (apikey != null)
            return "&apikey=" + apikey;
        else return "";
    }

    public static String getExternalParameter(Context context, int type) {
        String s = "", d = "", t = "";
        String CONFIRM = "&confirm=";
        String CLICK = "&mouse=";

        switch (type) {
            case 0: // confirm
                t = CONFIRM + "1" + CLICK + "0";
                break;

            case 1: // Todo click
                t = CONFIRM + "0" + CLICK + "1";
                break;

            case 2: // none
                t = CONFIRM + "0" + CLICK + "0";
                break;
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        Boolean prefSelfonly = prefs.getBoolean("pref_api_selfonly", false);
        Boolean prefDebug = prefs.getBoolean("pref_api_debug", false);
        if (prefSelfonly) s = "&selfonly=1";
        if (prefDebug) d = "&debug=1";
        return s + d + t;
    }

    public static String getBalance(Context context) {
        String URL_BALANCE = (URL_9WK + URL_PARAMETER_SERVER_BALANCE +
                URL_PARAMETER_SOURCE + getApiKey(context));

        try {
            return new DownloadContentTask().execute(URL_BALANCE, "").get();
        } catch (InterruptedException | ExecutionException e) {
            return "";
        }
    }

    public static boolean networkAvailable(Context context) {
        Log.i("isNetworkAvailable", "Called");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isConfirmEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getBoolean("pref_layout_confirm", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new cFragmentAdapter(getFragmentManager()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem linkItem = menu.add("Source");
        linkItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        linkItem.setIcon(R.drawable.ic_bookmark_outline_white_36dp);
        linkItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent linkIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/dotWee/OpenKWSolver"));
                startActivity(linkIntent);
                finish();
                return false;
            }
        });

        MenuItem exitItem = menu.add("Exit");
        exitItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        exitItem.setIcon(R.drawable.ic_close_circle_outline_white_36dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        System.exit(0);
                        return true;
                    }
                });
        return true;
    }

    public class cFragmentAdapter extends FragmentPagerAdapter {
        private String LOG_TAG = "FragmentAdapter";

        public cFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (isConfirmEnabled(getApplicationContext())) {
                if (position == 0) return new SolverFragment();
                if (position == 1) return new ConfirmFragment();
                if (position == 2) return new SettingsFragment();
            } else {
                if (position == 0) return new SolverFragment();
                if (position == 1) return new SettingsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            if (isConfirmEnabled(getApplicationContext())) return 3;
            else return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (isConfirmEnabled(getApplicationContext())) {
                if (position == 0) return "Solver";
                if (position == 1) return "Confirm";
                if (position == 2) return "Settings";
            } else {
                if (position == 0) return "Solver";
                if (position == 1) return "Settings";
            }
            return null;
        }
    }
}