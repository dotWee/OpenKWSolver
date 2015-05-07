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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.dotwee.openkwsolver.Fragments.SettingsFragment;
import de.dotwee.openkwsolver.Fragments.SolverFragment;
import de.dotwee.openkwsolver.Tools.StaticHelpers;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(StaticHelpers.getDefaultStyle(this));
		setContentView(R.layout.activity_main);

		if (!getResources().getBoolean(R.bool.isTablet)) {
			setupActionBar();
			setupViewPager();
		}

	}

	private void setupActionBar() {
		if (getSupportActionBar() != null)
			getSupportActionBar().setLogo(StaticHelpers.getDefaultIcon(this));
	}

	private void setupViewPager() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(new FragmentAdapter(getFragmentManager()));
		viewPager.setOffscreenPageLimit(2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.menu_global, menu);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_exit:
				finish();
				break;

			case R.id.menu_github:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dotWee/OpenKWSolver")));
				finish();
				break;
		}

		return false;
	}

	public static class FragmentAdapter extends FragmentPagerAdapter {
		public FragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return new SolverFragment();
				case 1:
					return new SettingsFragment();
				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "Solver";
				case 1:
					return "Settings";
				default:
					return null;
			}
		}
	}
}