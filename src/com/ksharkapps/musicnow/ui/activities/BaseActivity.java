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

import static com.ksharkapps.musicnow.utils.MusicUtils.mService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ksharkapps.musicnow.IApolloService;
import com.ksharkapps.musicnow.R;
import com.ksharkapps.musicnow.MusicPlaybackService;
import com.ksharkapps.musicnow.MusicStateListener;
import com.ksharkapps.musicnow.misc.Utils;
import com.ksharkapps.musicnow.preferences.SettingsActivity;
import com.ksharkapps.musicnow.ui.widgets.ScrollableTabView;
import com.ksharkapps.musicnow.ui.widgets.SystemBarTintManager;
import com.ksharkapps.musicnow.utils.ApolloUtils;
import com.ksharkapps.musicnow.utils.Lists;
import com.ksharkapps.musicnow.utils.MusicUtils;
import com.ksharkapps.musicnow.utils.NavUtils;
import com.ksharkapps.musicnow.utils.MusicUtils.ServiceToken;
import com.ksharkapps.musicnow.widgets.PlayPauseButton;
import com.ksharkapps.musicnow.widgets.RepeatButton;
import com.ksharkapps.musicnow.widgets.ShuffleButton;
import com.ksharkapps.musicnow.widgets.theme.BottomActionBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A base {@link FragmentActivity} used to update the bottom bar and bind to
 * Apollo's service.
 * <p>
 * {@link HomeActivity} extends from this skeleton.
 * 
 * @author Kartik Sharma (kshark.apps@gmail.com)
 */
@SuppressLint("ResourceAsColor")
public abstract class BaseActivity extends ActionBarActivity implements
		ServiceConnection {

	/**
	 * Playstate and meta change listener
	 */
	private final ArrayList<MusicStateListener> mMusicStateListener = Lists
			.newArrayList();

	/**
	 * The service token
	 */
	private ServiceToken mToken;

	/**
	 * Play and pause button (BAB)
	 */
	private PlayPauseButton mPlayPauseButton;

	/**
	 * Repeat button (BAB)
	 */
	private RepeatButton mRepeatButton;

	/**
	 * Shuffle button (BAB)
	 */
	private ShuffleButton mShuffleButton;

	/**
	 * Track name (BAB)
	 */
	private TextView mTrackName;

	/**
	 * Artist name (BAB)
	 */
	private TextView mArtistName;

	/**
	 * Album art (BAB)
	 */
	private ImageView mAlbumArt;

	private boolean mActionMode;
	
	private LinearLayout bottomActionBar;

	/**
	 * Broadcast receiver
	 */
	private PlaybackStatus mPlaybackStatus;

	/**
	 * Keeps track of the back button being used
	 */
	private boolean mIsBackPressed = false;

	private ActionBar actBar;

	public static final String KEY_ACTIONBAR_COLOR = "actionBarColor";

	private int actionBarColor;

	/**
	 * Theme resources
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);

		// Fade it in
		overridePendingTransition(R.anim.swipeback_stack_right_in,
				R.anim.swipeback_stack_to_back);
		// Control the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Bind Apollo's service
		mToken = MusicUtils.bindToService(this, this);

		// Initialize the broadcast receiver
		mPlaybackStatus = new PlaybackStatus(this);

		// Set the layout
		setContentView(setContentView());

		actBar = getSupportActionBar();

		// Initialze the bottom action bar
		initBottomActionBar();
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onServiceConnected(final ComponentName name,
			final IBinder service) {
		mService = IApolloService.Stub.asInterface(service);
		// Set the playback drawables
		updatePlaybackControls();
		// Current info
		updateBottomActionBarInfo();
		// Update the favorites icon
		invalidateOptionsMenu();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onServiceDisconnected(final ComponentName name) {
		mService = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Search view
		getMenuInflater().inflate(R.menu.search, menu);
		// Settings
		getMenuInflater().inflate(R.menu.activity_base, menu);
		
		MenuItem searchItem = menu.findItem(R.id.menu_search);

		final SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
		// Add voice search
		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchableInfo searchableInfo = searchManager
				.getSearchableInfo(getComponentName());
		searchView.setSearchableInfo(searchableInfo);
		// Perform the search
		if(searchView!=null)
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(final String query) {
				// Open the search activity
				NavUtils.openSearch(BaseActivity.this, query);
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				// Nothing to do
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			// Settings
			NavUtils.openSettings(this);
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
	protected void onResume() {
		super.onResume();
		// Set the playback drawables
		updatePlaybackControls();
		// Current info
		updateBottomActionBarInfo();

		if (SettingsActivity.getActionBarColor(this) != actionBarColor) {
			changeActionBarColor(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter();
		// Play and pause changes
		filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
		// Shuffle and repeat changes
		filter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED);
		filter.addAction(MusicPlaybackService.REPEATMODE_CHANGED);
		// Track changes
		filter.addAction(MusicPlaybackService.META_CHANGED);
		// Update a list, probably the playlist fragment's
		filter.addAction(MusicPlaybackService.REFRESH);
		registerReceiver(mPlaybackStatus, filter);
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
	 * {@inheritDoc}
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unbind from the service
		if (mToken != null) {
			MusicUtils.unbindFromService(mToken);
			mToken = null;
		}

		// Unregister the receiver
		try {
			unregisterReceiver(mPlaybackStatus);
		} catch (final Throwable e) {
			//$FALL-THROUGH$
		}

		// Remove any music status listeners
		mMusicStateListener.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mIsBackPressed = true;
	}

	/**
	 * Initializes the items in the bottom action bar.
	 */
	private void initBottomActionBar() {
		// Play and pause button
		mPlayPauseButton = (PlayPauseButton) findViewById(R.id.action_button_play);
		mPlayPauseButton.setBackgroundResource(R.drawable.items_background);
		// Shuffle button
		mShuffleButton = (ShuffleButton) findViewById(R.id.action_button_shuffle);
		mShuffleButton.setBackgroundResource(R.drawable.items_background);

		// Repeat button
		mRepeatButton = (RepeatButton) findViewById(R.id.action_button_repeat);
		mRepeatButton.setBackgroundResource(R.drawable.items_background);

		// Track name
		mTrackName = (TextView) findViewById(R.id.bottom_action_bar_line_one);
		// Artist name
		mArtistName = (TextView) findViewById(R.id.bottom_action_bar_line_two);
		// Album art
		mAlbumArt = (ImageView) findViewById(R.id.bottom_action_bar_album_art);
		// Open to the currently playing album profile
		mAlbumArt.setOnClickListener(mOpenCurrentAlbumProfile);
		// Bottom action bar
		bottomActionBar = (LinearLayout) findViewById(R.id.bottom_action_bar);
		
		// Display the now playing screen or shuffle if this isn't anything
		// playing
		bottomActionBar.setOnClickListener(mOpenNowPlaying);
	}

	/**
	 * Sets the track name, album name, and album art.
	 */
	private void updateBottomActionBarInfo() {
		// Set the track name
		mTrackName.setText(MusicUtils.getTrackName());
		// Set the artist name
		mArtistName.setText(MusicUtils.getArtistName()+" - "+MusicUtils.getAlbumName());
		// Set the album art
		ApolloUtils.getImageFetcher(this).loadCurrentArtwork(mAlbumArt);
	}

	/**
	 * Sets the correct drawable states for the playback controls.
	 */
	private void updatePlaybackControls() {
		// Set the play and pause image
		mPlayPauseButton.updateState();
		// Set the shuffle image
		mShuffleButton.updateShuffleState();
		// Set the repeat image
		mRepeatButton.updateRepeatState();
	}

	/**
	 * Opens the album profile of the currently playing album
	 */
	private final View.OnClickListener mOpenCurrentAlbumProfile = new View.OnClickListener() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClick(final View v) {
			if (MusicUtils.getCurrentAudioId() != -1) {
				NavUtils.openAlbumProfile(BaseActivity.this,
						MusicUtils.getAlbumName(), MusicUtils.getArtistName(),
						MusicUtils.getCurrentAlbumId());
			} else {
				MusicUtils.shuffleAll(BaseActivity.this);
			}
			if (BaseActivity.this instanceof ProfileActivity) {
				finish();
			}
		}
	};

	/**
	 * Opens the now playing screen
	 */
	private final View.OnClickListener mOpenNowPlaying = new View.OnClickListener() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClick(final View v) {
			if (MusicUtils.getCurrentAudioId() != -1) {
				NavUtils.openAudioPlayer(BaseActivity.this);
			} else {
				MusicUtils.shuffleAll(BaseActivity.this);
			}
		}
	};

	/**
	 * Used to monitor the state of playback
	 */
	private final static class PlaybackStatus extends BroadcastReceiver {

		private final WeakReference<BaseActivity> mReference;

		/**
		 * Constructor of <code>PlaybackStatus</code>
		 */
		public PlaybackStatus(final BaseActivity activity) {
			mReference = new WeakReference<BaseActivity>(activity);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (action.equals(MusicPlaybackService.META_CHANGED)) {
				// Current info
				mReference.get().updateBottomActionBarInfo();
				// Update the favorites icon
				mReference.get().invalidateOptionsMenu();
				// Let the listener know to the meta chnaged
				for (final MusicStateListener listener : mReference.get().mMusicStateListener) {
					if (listener != null) {
						listener.onMetaChanged();
					}
				}
			} else if (action.equals(MusicPlaybackService.PLAYSTATE_CHANGED)) {
				// Set the play and pause image
				mReference.get().mPlayPauseButton.updateState();
			} else if (action.equals(MusicPlaybackService.REPEATMODE_CHANGED)
					|| action.equals(MusicPlaybackService.SHUFFLEMODE_CHANGED)) {
				// Set the repeat image
				mReference.get().mRepeatButton.updateRepeatState();
				// Set the shuffle image
				mReference.get().mShuffleButton.updateShuffleState();
			} else if (action.equals(MusicPlaybackService.REFRESH)) {
				// Let the listener know to update a list
				for (final MusicStateListener listener : mReference.get().mMusicStateListener) {
					if (listener != null) {
						listener.restartLoader();
					}
				}
			}
		}
	}

	/**
	 * @param status
	 *            The {@link MusicStateListener} to use
	 */
	public void setMusicStateListenerListener(final MusicStateListener status) {
		if (status != null) {
			mMusicStateListener.add(status);
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


		// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
		actBar.setDisplayShowTitleEnabled(false);
		actBar.setDisplayShowTitleEnabled(true);

		if (Utils.hasKitKat()) {
			if (SettingsActivity.getTranslucentMode(this)) {
				SystemBarTintManager tintManager = new SystemBarTintManager(
						this);
				tintManager.setStatusBarTintEnabled(true);
				tintManager.setStatusBarTintColor(getResources().getColor(R.color.contextual_actionbar_color));

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

	public boolean getActionMode() {
		return mActionMode;
	}

	public void setActionMode(boolean actionMode) {
		mActionMode = actionMode;
	}

	

	/**
	 * @return The resource ID to be inflated.
	 */
	public abstract int setContentView();
}
