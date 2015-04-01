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

package de.dotwee.openkwsolver;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import de.dotwee.openkwsolver.Fragments.SettingsFragment;
import de.dotwee.openkwsolver.Fragments.SolverFragment;

public class MainActivity extends ActionBarActivity {
	private static final String LOG_TAG = "MainActivity";
	public Toolbar toolbar;
	ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	    toolbar = (Toolbar) findViewById(R.id.toolbar_main);
	    toolbar.setLogo(R.mipmap.ic_launcher);
	    toolbar.setTitle(getResources().getString(R.string.app_name));

	    toolbar.inflateMenu(R.menu.menu_global);
	    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
		    @Override
		    public boolean onMenuItemClick(MenuItem menuItem) {
			    switch (menuItem.getItemId()) {
				    case R.id.menu_exit:
					    System.exit(0);
			    }

			    return false;
		    }
	    });

	    if (getResources().getBoolean(R.bool.isTablet) != true) {
		    viewPager = (ViewPager) findViewById(R.id.viewPager);
		    viewPager.setAdapter(new FragmentAdapter(getFragmentManager()));
		    viewPager.setOffscreenPageLimit(2);
	    }

    }

	public class FragmentAdapter extends FragmentPagerAdapter {
		private static final String LOG_TAG = "FragmentAdapter";

		public FragmentAdapter(FragmentManager fm) {
			super(fm);
        }

        @Override
        public Fragment getItem(int position) {
			Log.i(LOG_TAG, "getItem / " + position);

	        switch (position) {
		        case 0: return new SolverFragment();
		        case 1: return new SettingsFragment();
		        default: return null;
            }

        }

        @Override
        public int getCount() {
	        return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
	        switch (position) {
		        case 0: return "Solver";
		        case 1: return "Settings";
		        default: return null;
	        }
        }
    }
}