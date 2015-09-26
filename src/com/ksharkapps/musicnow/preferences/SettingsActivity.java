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

package com.ksharkapps.musicnow.preferences;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.ksharkapps.musicnow.R;
import com.ksharkapps.musicnow.cache.ImageCache;
import com.ksharkapps.musicnow.utils.ApolloUtils;
import com.ksharkapps.musicnow.utils.MusicUtils;
import com.ksharkapps.musicnow.utils.PreferenceUtils;


/**
 * Settings.
 * 
 * @author Kartik Sharma (kshark.apps@gmail.com)
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    /**
     * Image cache
     */
    private ImageCache mImageCache;

    private PreferenceUtils mPreferences;
    
    public static final String KEY_ACTIONBAR_COLOR = "actionBarColor";
    
  	private int actionBarColor;
  	
    public static final String KEY_TRANSLUCENT_MODE = "translucentMode";

  	
	public boolean translucentMode = false;

	
	private ActionBar actionBar;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fade it in
      //  overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        

        // Get the preferences
        mPreferences = PreferenceUtils.getInstance(this);

        // Initialze the image cache
        mImageCache = ImageCache.getInstance(this);
        
        // Add the preferences

        int preferencesResId = R.xml.settings;
        addPreferencesFromResource(preferencesResId);
        
		
		actionBar = getActionBar();
	  
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
		
        //changeActionBarColor(0);
        
        // Interface settings
        initInterface();
        // Removes the cache entries
        deleteCache();
        // About
        showOpenSourceLicenses();
        // Update the version number
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            findPreference("version").setSummary(packageInfo.versionName);
        } catch (final NameNotFoundException e) {
            findPreference("version").setSummary("?");
        }
        
        
        actionBarColor = getActionBarColor(this);
        
        translucentMode = getTranslucentMode(this);
    

    }
    
    
    public static int getActionBarColor(Context context) {
     	int newColor = context.getResources().getColor(R.color.actionbar_color);
         return PreferenceManager.getDefaultSharedPreferences(context)
                 .getInt(KEY_ACTIONBAR_COLOR, newColor);
     }
  	 
  	 public static boolean getTranslucentMode(Context context) {
         return PreferenceManager.getDefaultSharedPreferences(context)
                 .getBoolean(KEY_TRANSLUCENT_MODE, false);
     }
  
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();
        MusicUtils.notifyForegroundStateChanged(this, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
        MusicUtils.notifyForegroundStateChanged(this, false);
    }

    /**
     * Initializes the preferences under the "Interface" category
     */
    private void initInterface() {
        // Color scheme picker
        // Open the theme chooser
     //   openThemeChooser();
    }

   
    /**
     * Opens the {@link ThemeFragment}.
     */
  /*  private void openThemeChooser() {
        final Preference themeChooser = findPreference("theme_chooser");
        themeChooser.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final Intent themeChooserIntent = new Intent(SettingsActivity.this,
                        ThemesActivity.class);
                startActivity(themeChooserIntent);
                return true;
            }
        });
    }*/

    /**
     * Removes all of the cache entries.
     */
    private void deleteCache() {
        final Preference deleteCache = findPreference("delete_cache");
        deleteCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this).setMessage(R.string.delete_warning)
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                mImageCache.clearCaches();
                            }
                        }).setNegativeButton(R.string.cancel, new OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return true;
            }
        });
    }

    /**
     * Show the open source licenses
     */
    private void showOpenSourceLicenses() {
        final Preference mOpenSourceLicenses = findPreference("open_source");
        mOpenSourceLicenses.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(final Preference preference) {
                ApolloUtils.createOpenSourceDialog(SettingsActivity.this).show();
                return true;
            }
        });
    }
    
    
    private final Handler handler = new Handler();
  	private Drawable oldBackground;
  	
  	
  	
    public void changeActionBarColor(int newColor) {

		int color = newColor != 0 ? newColor : getActionBarColor(this);
		Drawable colorDrawable = new ColorDrawable(color);
		Drawable bottomDrawable = getResources().getDrawable(R.drawable.transparent_overlay);
		LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });

		if (oldBackground == null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				ld.setCallback(drawableCallback);
			} else {
				getActionBar().setBackgroundDrawable(ld);
			}

		} else {
			TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });
			// workaround for broken ActionBarContainer drawable handling on
			// pre-API 17 builds
			// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				td.setCallback(drawableCallback);
			} else {
				getActionBar().setBackgroundDrawable(td);
			}
			td.startTransition(200);
		}

		oldBackground = ld;
		
		// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
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
	
	 @Override
	    protected void onResume() {
	    	super.onResume();
	    	if(getActionBarColor(this) != actionBarColor){
	    		changeActionBarColor(0);
	    	}
	    }
	    
	 
	 
	 @Override
	    protected void onSaveInstanceState(Bundle outState) {
	    	super.onSaveInstanceState(outState);
	    	outState.putBoolean("changed", translucentMode);

	    }
	    
	    @Override
	    protected void onRestoreInstanceState(Bundle state) {
	    	super.onRestoreInstanceState(state);
	    	translucentMode = state.getBoolean("changed");

	    }
	    
	    @Override
	    public void finish() {
	    	if((getTranslucentMode(this) != translucentMode)){
	    		setResult(RESULT_FIRST_USER);
	    	}
	    	super.finish();
	    }
	    
	   

	
}
