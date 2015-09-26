/*
 * Copyright (C) 2014 Kartik Sharma Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.ksharkapps.musicnow.ui.activities;

import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bluemor.reddotface.view.DragLayout;
import com.bluemor.reddotface.view.DragLayout.DragListener;
import com.ksharkapps.musicnow.R;
import com.ksharkapps.musicnow.adapters.NavigationAdapter;
import com.ksharkapps.musicnow.misc.Utils;
import com.ksharkapps.musicnow.preferences.SettingsActivity;
import com.ksharkapps.musicnow.ui.fragments.phone.MusicBrowserPhoneFragment;
import com.ksharkapps.musicnow.ui.widgets.ScrollableTabView;
import com.ksharkapps.musicnow.ui.widgets.SystemBarTintManager;
import com.nineoldandroids.view.ViewHelper;

/**
 * This class is used to display the {@link ViewPager} used to swipe between the
 * main {@link Fragment}s used to browse the user's music.
 * 
 * @author Kartik Sharma (kshark.apps@gmail.com)
 */
public class HomeActivity extends BaseActivity {

	private ActionBar actBar;

	public static final String KEY_ACTIONBAR_COLOR = "actionBarColor";

	private int actionBarColor;

	private DrawerLayout mLayoutDrawer;
	
	private DragLayout dl;


	private ListView mListDrawer;

	private NavigationAdapter mNavigationAdapter;

	private ActionBarDrawerToggleCompat mDrawerToggle;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (SettingsActivity.getTranslucentMode(this) && Utils.hasKitKat()) {
			setTheme(R.style.Apollo_Theme_Dark_Translucent);
			setTranslucentStatus(true);
		}
		
		//initDragLayout();

		
		/*mListDrawer = (ListView) findViewById(R.id.listDrawer);
		mLayoutDrawer = (DrawerLayout) findViewById(R.id.layoutDrawer);
		
		mListDrawer.setAdapter(null);

		mDrawerToggle = new ActionBarDrawerToggleCompat(this, mLayoutDrawer);		
		mLayoutDrawer.setDrawerListener(mDrawerToggle);*/
		

		// Load the music browser fragment
		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.activity_base_content,
							new MusicBrowserPhoneFragment()).commit();
		}

		initActionBar();

		

	}
	
	/*
	private void initDragLayout() {
		dl = (DragLayout) findViewById(R.id.drawerLayout);
		dl.setDragListener(new DragListener() {
			@Override
			public void onOpen() {
				//lv.smoothScrollToPosition(new Random().nextInt(30));
			}

			@Override
			public void onClose() {
				//shake();
			}

			@Override
			public void onDrag(float percent) {
				//ViewHelper.setAlpha(iv_icon, 1 - percent);
			}
		});
	}
*/

	private void initActionBar() {
		actBar = getSupportActionBar();

		actBar.setDisplayUseLogoEnabled(false);
		actBar.setIcon(R.drawable.ic_navigation_drawer);
		actBar.setTitle("Library");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int setContentView() {
		return R.layout.activity_base;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (SettingsActivity.getActionBarColor(this) != actionBarColor) {
			changeActionBarColor(0);
		}
	}

	public static int getActionBarColor(Context context) {
		int newColor = context.getResources().getColor(R.color.actionbar_color);
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(
				KEY_ACTIONBAR_COLOR, newColor);
	}

	private final Handler handler = new Handler();
	private Drawable oldBackground;

	public void changeActionBarColor(int newColor) {

		int color = newColor != 0 ? newColor : SettingsActivity
				.getActionBarColor(this);
		Drawable colorDrawable = new ColorDrawable(color);
		Drawable bottomDrawable = getResources().getDrawable(
				R.drawable.transparent_overlay);
		LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable,
				bottomDrawable });

		if (oldBackground == null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				ld.setCallback(drawableCallback);
			} else {
				actBar.setBackgroundDrawable(colorDrawable);
			}

		} else {
			TransitionDrawable td = new TransitionDrawable(new Drawable[] {
					oldBackground, ld });
			// workaround for broken ActionBarContainer drawable handling on
			// pre-API 17 builds
			// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				td.setCallback(drawableCallback);
			} else {
				actBar.setBackgroundDrawable(td);
			}
			td.startTransition(200);
		}

		oldBackground = ld;

		ScrollableTabView mScrollingTabs = (ScrollableTabView) findViewById(R.id.fragment_home_phone_pager_titles);
		mScrollingTabs.setBackgroundColor(color);

		// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
		actBar.setDisplayShowTitleEnabled(false);
		actBar.setDisplayShowTitleEnabled(true);

		if (Utils.hasKitKat()) {
			if (SettingsActivity.getTranslucentMode(this)) {
				SystemBarTintManager tintManager = new SystemBarTintManager(
						this);
				tintManager.setStatusBarTintEnabled(true);
				tintManager.setStatusBarTintColor(color);

			}

		}

	}

	private Drawable.Callback drawableCallback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable who) {
			getActionBar().setBackgroundDrawable(who);
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {
			handler.postAtTime(what, when);
		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {
			handler.removeCallbacks(what);
		}
	};

	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	private class ActionBarDrawerToggleCompat extends ActionBarDrawerToggle {

		public ActionBarDrawerToggleCompat(Activity mActivity,
				DrawerLayout mDrawerLayout) {
			super(mActivity, mDrawerLayout, R.drawable.ic_action_navigation_drawer,
					R.string.drawer_open, R.string.drawer_close);
		}

		@Override
		public void onDrawerClosed(View view) {
			supportInvalidateOptionsMenu();
		}

		@Override
		public void onDrawerOpened(View drawerView) {
		//	mNavigationAdapter.notifyDataSetChanged();
			supportInvalidateOptionsMenu();
		}
	}

}
