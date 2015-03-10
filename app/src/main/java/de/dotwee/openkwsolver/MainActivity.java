package de.dotwee.openkwsolver;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.ExecutionException;

import de.dotwee.openkwsolver.Fragments.SettingsFragment;
import de.dotwee.openkwsolver.Fragments.SolverFragment;
import de.dotwee.openkwsolver.Tools.DownloadContentTask;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_NOCAPTCHA = "&nocaptcha=1";
    public static final String URL_PARAMETER_CAPTCHA_NEW = "?action=usercaptchanew";
    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    public static final String URL_PARAMETER_CAPTCHA_SKIP = "?action=usercaptchaskip";
    private final static String LOG_TAG = "MainActivity";
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new cFragmentAdapter(getFragmentManager()));

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setText("Solver").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Settings").setTabListener(this));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
                        finish();
                        return true;
                    }
                });
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
    
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public class cFragmentAdapter extends FragmentPagerAdapter {
        private String LOG_TAG = "FragmentAdapter";

        public cFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) fragment = new SolverFragment();
            if (position == 1) fragment = new SettingsFragment();

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static String requestCaptchaID(String API_KEY, String EXTERNAL_PARAMETER, Boolean LOOP) {
        String CAPTCHA_URL = (
                URL_9WK + URL_PARAMETER_CAPTCHA_NEW + API_KEY +
                        URL_PARAMETER_SOURCE + EXTERNAL_PARAMETER + URL_PARAMETER_NOCAPTCHA);
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

        Log.i(LOG_TAG, "ID Request RETURN: " +
                CAPTCHA_ID);

        return CAPTCHA_ID;
    }

    public static void skipCaptchaByID(String CAPTCHA_ID, String API_KEY) {
        String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_SKIP + "&id=" + CAPTCHA_ID +
                API_KEY + URL_PARAMETER_SOURCE);
        Log.i(LOG_TAG, "SKIP Request URL: " + CAPTCHA_URL);
        new DownloadContentTask().execute(CAPTCHA_URL);
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
}