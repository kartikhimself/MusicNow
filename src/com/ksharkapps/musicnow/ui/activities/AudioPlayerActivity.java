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
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.PorterDuff.*;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Artists;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.appcompat.R.integer;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ksharkapps.musicnow.IApolloService;
import com.ksharkapps.musicnow.R;
import com.ksharkapps.musicnow.MusicPlaybackService;
import com.ksharkapps.musicnow.adapters.PagerAdapter;
import com.ksharkapps.musicnow.cache.ImageCache;
import com.ksharkapps.musicnow.cache.ImageFetcher;
import com.ksharkapps.musicnow.colorart.ColorArt;
import com.ksharkapps.musicnow.colorart.Punto3D;
import com.ksharkapps.musicnow.menu.DeleteDialog;
import com.ksharkapps.musicnow.misc.Utils;
import com.ksharkapps.musicnow.preferences.SettingsActivity;
import com.ksharkapps.musicnow.ui.fragments.QueueFragment;
import com.ksharkapps.musicnow.ui.widgets.SystemBarTintManager;
import com.ksharkapps.musicnow.utils.ApolloUtils;
import com.ksharkapps.musicnow.utils.MusicUtils;
import com.ksharkapps.musicnow.utils.NavUtils;
import com.ksharkapps.musicnow.utils.MusicUtils.ServiceToken;
import com.ksharkapps.musicnow.widgets.PlayPauseButton;
import com.ksharkapps.musicnow.widgets.RepeatButton;
import com.ksharkapps.musicnow.widgets.RepeatingImageButton;
import com.ksharkapps.musicnow.widgets.ShuffleButton;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Apollo's "now playing" interface.
 * 
 * @author Kartik Sharma (kshark.apps@gmail.com)
 */
public class AudioPlayerActivity extends ActionBarActivity implements
		ServiceConnection, OnSeekBarChangeListener,
		DeleteDialog.DeleteDialogCallback {

	// Message to refresh the time
	private static final int REFRESH_TIME = 1;

	// The service token
	private ServiceToken mToken;

	// Play and pause button
	private PlayPauseButton mPlayPauseButton;

	// Repeat button
	private RepeatButton mRepeatButton;

	// Shuffle button
	private ShuffleButton mShuffleButton;

	// Previous button
	private RepeatingImageButton mPreviousButton;

	// Next button
	private RepeatingImageButton mNextButton;

	// Track name
	private TextView mTrackName;

	// Artist name
	private TextView mArtistName;

	// Album art
	private ImageView mAlbumArt;

	// Tiny artwork
	private ImageView mAlbumArtSmall;

	// Current time
	private TextView mCurrentTime;

	// Seek time
	private TextView mSeekTime;

	// Total time
	private TextView mTotalTime;

	// Queue switch
	private ImageView mQueueSwitch;

	// Progess
	private SeekBar mProgress;

	// Broadcast receiver
	private PlaybackStatus mPlaybackStatus;

	// Handler used to update the current time
	private TimeHandler mTimeHandler;

	// View pager
	private ViewPager mViewPager;

	// Pager adpater
	private PagerAdapter mPagerAdapter;

	// ViewPager container
	private FrameLayout mPageContainer;

	// Header
	private LinearLayout mAudioPlayerHeader;
	
	private RelativeLayout mControlsLayout;

	// Image cache
	private ImageFetcher mImageFetcher;
	
    protected ImageCache mImageCache;
    
    private static String mLastPlayingSong;


	private long mPosOverride = -1;

	private long mStartSeekPos = 0;

	private long mLastSeekEventTime;

	private long mLastShortSeekEventTime;

	private boolean mIsPaused = false;

	private boolean mFromTouch = false;

	public static final String KEY_ACTIONBAR_COLOR = "actionBarColor";

	private int actionBarColor;

	private ActionBar actionBar;

	private RelativeLayout player_bg;
	
	private boolean mArtShown=true;

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

		// Set up the action bar
		actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.now_playing);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);

		

		// Sets fonts for all
		Typeface font = Typeface.createFromAsset(getAssets(), "RobotoThin.ttf");
		ViewGroup root = (ViewGroup) findViewById(R.id.player_bg);
		// setFont(root, font);

		// Fade it in
		overridePendingTransition(R.anim.swipeup_in,
				R.anim.swipeup_out);

		// Control the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Bind Apollo's service
		mToken = MusicUtils.bindToService(this, this);

		// Initialize the image fetcher/cache
		mImageFetcher = ApolloUtils.getImageFetcher(this);

		// Initialize the handler used to update the current time
		mTimeHandler = new TimeHandler(this);

		// Initialize the broadcast receiver
		mPlaybackStatus = new PlaybackStatus(this);


		// Theme the action bar
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Set the layout
		setContentView(R.layout.activity_player_base);

		// Cache all the items
		initPlaybackControls();
	}
	
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		startPlayback();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onServiceConnected(final ComponentName name,
			final IBinder service) {
		mService = IApolloService.Stub.asInterface(service);
		// Check whether we were asked to start any playback
		startPlayback();
		// Set the playback drawables
		updatePlaybackControls();
		// Current info
		updateNowPlayingInfo();
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
	public void onProgressChanged(final SeekBar bar, final int progress,
			final boolean fromuser) {
		if (!fromuser || mService == null) {
			return;
		}
		final long now = SystemClock.elapsedRealtime();
		if (now - mLastSeekEventTime > 250) {
			mLastSeekEventTime = now;
			mLastShortSeekEventTime = now;
			mPosOverride = MusicUtils.duration() * progress / 1000;
			MusicUtils.seek(mPosOverride);
			if (!mFromTouch) {
				// refreshCurrentTime();
				mPosOverride = -1;
			}
		} else if (now - mLastShortSeekEventTime > 5) {
			mLastShortSeekEventTime = now;
			mPosOverride = MusicUtils.duration() * progress / 1000;
			refreshCurrentTimeText(mPosOverride);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStartTrackingTouch(final SeekBar bar) {
		mLastSeekEventTime = 0;
		mFromTouch = true;
		mCurrentTime.setVisibility(View.VISIBLE);
		mSeekTime.setVisibility(View.VISIBLE);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStopTrackingTouch(final SeekBar bar) {
		if (mPosOverride != -1) {
			MusicUtils.seek(mPosOverride);
		}
		mPosOverride = -1;
		mFromTouch = false;
		mSeekTime.setVisibility(View.INVISIBLE);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		// Hide the EQ option if it can't be opened
		MenuItem fav = menu.findItem(R.id.menu_favorite);

		final Intent intent = new Intent(
				AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
		if (getPackageManager().resolveActivity(intent, 0) == null) {
			final MenuItem effects = menu
					.findItem(R.id.menu_audio_player_equalizer);
			effects.setVisible(false);
		}
		if (MusicUtils.mService != null && MusicUtils.getCurrentAudioId() != -1) {
			if (MusicUtils.isFavorite()) {
				fav.setIcon(R.drawable.ic_favorited);
			} else {
				fav.setIcon(R.drawable.ic_unfavorited);
				// Theme chooser
			}
		}
		return true;
	}

	private void processImage() {

		Bitmap image = ((BitmapDrawable) mAlbumArt.getDrawable()).getBitmap();

		ColorArt colorArt = new ColorArt(image);

		// get the colors
		colorArt.getBackgroundColor();
		colorArt.getPrimaryColor();
		colorArt.getSecondaryColor();
		colorArt.getDetailColor();

		Bitmap imagen = ((BitmapDrawable) mAlbumArt.getDrawable()).getBitmap();
		long reds = 0L;
		long greens = 0L;
		long blues = 0L;
		int[] pixeles = new int[imagen.getWidth() * imagen.getHeight()];
		imagen.getPixels(pixeles, 0, imagen.getWidth(), 0, 0,
				imagen.getWidth(), imagen.getHeight());
		for (int cursor = 0; cursor < pixeles.length; cursor++) {
			reds += Color.red(pixeles[cursor]);
			greens += Color.green(pixeles[cursor]);
			blues += Color.blue(pixeles[cursor]);
		}
		long numPixels = imagen.getWidth() * imagen.getHeight();
		reds /= numPixels;
		greens /= numPixels;
		blues /= numPixels;

		Punto3D[] pixelesRef = { new Punto3D(255, 0, 0),
				new Punto3D(0, 255, 0), new Punto3D(0, 0, 255),
				new Punto3D(255, 255, 0), new Punto3D(0, 255, 255),
				new Punto3D(255, 0, 255), new Punto3D(0, 0, 0),
				new Punto3D(255, 255, 255) };
		Punto3D pixelActual = new Punto3D(reds, greens,blues);

		double[] distancias = { pixelActual.distancia(pixelesRef[0]),
				pixelActual.distancia(pixelesRef[1]),
				pixelActual.distancia(pixelesRef[2]),
				pixelActual.distancia(pixelesRef[3]),
				pixelActual.distancia(pixelesRef[4]),
				pixelActual.distancia(pixelesRef[5]),
				pixelActual.distancia(pixelesRef[6]),
				pixelActual.distancia(pixelesRef[7]) };
		String[] colors = { "Red" , "Green" , "Blue" , "Yellow" , "Cyan "
				,"Magenta", " Black " , "White" };

		double dist_minima = 255;
		int indice_minima = 0;
		for (int index = 0; index < distancias.length; index++) {
			if (distancias[index] <= dist_minima) {
				indice_minima = index;
				dist_minima = distancias[index];
			}
			Log.i("Distancias", "Distancias en pos " + index + ": "
					+ distancias[index]);
		}

		GradientDrawable gd2 = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, new int[] {

						Color.rgb((int) pixelesRef[indice_minima].getX(),
								(int) pixelesRef[indice_minima].getY(),
								(int) pixelesRef[indice_minima].getZ()),
						colorArt.getBackgroundColor(),
						colorArt.getDetailColor() });

		gd2.setCornerRadius(0f);

		player_bg.setBackgroundDrawable(gd2);

	}
	
	private int getBackgroundColor(Bitmap imagen){
		long reds = 0L;
		long greens = 0L;
		long blues = 0L;
		int[] pixeles = new int[imagen.getWidth() * imagen.getHeight()];
		imagen.getPixels(pixeles, 0, imagen.getWidth(), 0, 0,
				imagen.getWidth(), imagen.getHeight());
		for (int cursor = 0; cursor < pixeles.length; cursor++) {
			reds += Color.red(pixeles[cursor]);
			greens += Color.green(pixeles[cursor]);
			blues += Color.blue(pixeles[cursor]);
		}
		long numPixels = imagen.getWidth() * imagen.getHeight();
		reds /= numPixels;
		greens /= numPixels;
		blues /= numPixels;

		Punto3D[] pixelesRef = { new Punto3D(255, 0, 0),
				new Punto3D(0, 255, 0), new Punto3D(0, 0, 255),
				new Punto3D(255, 255, 0), new Punto3D(0, 255, 255),
				new Punto3D(255, 0, 255), new Punto3D(0, 0, 0),
				new Punto3D(255, 255, 255) };
		Punto3D pixelActual = new Punto3D(reds, greens,blues);

		double[] distancias = { pixelActual.distancia(pixelesRef[0]),
				pixelActual.distancia(pixelesRef[1]),
				pixelActual.distancia(pixelesRef[2]),
				pixelActual.distancia(pixelesRef[3]),
				pixelActual.distancia(pixelesRef[4]),
				pixelActual.distancia(pixelesRef[5]),
				pixelActual.distancia(pixelesRef[6]),
				pixelActual.distancia(pixelesRef[7]) };
		String[] colors = { "Red" , "Green" , "Blue" , "Yellow" , "Cyan "
				,"Magenta", " Black " , "White" };

		double dist_minima = 255;
		int indice_minima = 0;
		for (int index = 0; index < distancias.length; index++) {
			if (distancias[index] <= dist_minima) {
				indice_minima = index;
				dist_minima = distancias[index];
			}
			Log.i("Distancias", "Distancias en pos " + index + ": "
					+ distancias[index]);
		}
		
		return Color.rgb((int)pixelesRef[indice_minima].getX(),(int) pixelesRef[indice_minima].getY(),(int) pixelesRef[indice_minima].getZ());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Search view
		getMenuInflater().inflate(R.menu.search, menu);

		MenuItem searchItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) MenuItemCompat
				.getActionView(searchItem);

		// Add voice search
		final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchableInfo searchableInfo = searchManager
				.getSearchableInfo(getComponentName());
		searchView.setSearchableInfo(searchableInfo);
		// Perform the search
		if (searchView != null)
			searchView.setOnQueryTextListener(new OnQueryTextListener() {

				@Override
				public boolean onQueryTextSubmit(final String query) {
					// Open the search activity
					NavUtils.openSearch(AudioPlayerActivity.this, query);
					return true;
				}

				@Override
				public boolean onQueryTextChange(final String newText) {
					// Nothing to do
					return false;
				}
			});

		// Favorite action
		getMenuInflater().inflate(R.menu.favorite, menu);
		// Shuffle all
		getMenuInflater().inflate(R.menu.shuffle, menu);
		// Share, ringtone, and equalizer
		getMenuInflater().inflate(R.menu.audio_player, menu);
		// Settings
		getMenuInflater().inflate(R.menu.activity_base, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Go back to the home activity
			NavUtils.goHome(this);
			return true;
		case R.id.menu_shuffle:
			// Shuffle all the songs
			MusicUtils.shuffleAll(this);
			// Refresh the queue
			((QueueFragment) mPagerAdapter.getFragment(0)).refreshQueue();
			return true;
		case R.id.menu_favorite:
			// Toggle the current track as a favorite and update the menu
			// item
			MusicUtils.toggleFavorite();
			invalidateOptionsMenu();
			return true;
		case R.id.menu_audio_player_ringtone:
			// Set the current track as a ringtone
			MusicUtils.setRingtone(this, MusicUtils.getCurrentAudioId());
			return true;
		case R.id.menu_audio_player_share:
			// Share the current meta data
			shareCurrentTrack();
			return true;
		case R.id.menu_audio_player_equalizer:
			// Sound effects
			NavUtils.openEffectsPanel(this);
			return true;
		case R.id.menu_settings:
			// Settings
			NavUtils.openSettings(this);
			return true;
		case R.id.menu_audio_player_delete:
			// Delete current song
			DeleteDialog.newInstance(MusicUtils.getTrackName(),
					new long[] { MusicUtils.getCurrentAudioId() }, null).show(
					getSupportFragmentManager(), "DeleteDialog");
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDelete(long[] ids) {
		((QueueFragment) mPagerAdapter.getFragment(0)).refreshQueue();
		if (MusicUtils.getQueue().length == 0) {
			NavUtils.goHome(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBackPressed() {
		
		if(!mArtShown){
			showAlbumArt();
			mArtShown=true;
		}else{
		   super.onBackPressed();
		   NavUtils.goHome(this);
		}
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
		updateNowPlayingInfo();
		// Refresh the queue

		refreshCurrentTime();
		((QueueFragment) mPagerAdapter.getFragment(0)).refreshQueue();

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
		// Refresh the current time
		final long next = refreshCurrentTime();
		queueNextRefresh(next);
		MusicUtils.notifyForegroundStateChanged(this, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStop() {
		super.onStop();
		MusicUtils.notifyForegroundStateChanged(this, false);
		mImageFetcher.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mIsPaused = false;
		mTimeHandler.removeMessages(REFRESH_TIME);
		// Unbind from the service
		if (mService != null) {
			MusicUtils.unbindFromService(mToken);
			mToken = null;
		}

		// Unregister the receiver
		try {
			unregisterReceiver(mPlaybackStatus);
		} catch (final Throwable e) {
			//$FALL-THROUGH$
		}
	}

	/**
	 * Sets the font on all TextViews in the ViewGroup. Searches recursively for
	 * all inner ViewGroups as well. Just add a check for any other views you
	 * want to set as well (EditText, etc.)
	 */
	private void setFont(ViewGroup group, Typeface font) {
		int count = group.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = group.getChildAt(i);
			if (v instanceof TextView || v instanceof EditText
					|| v instanceof Button) {
				((TextView) v).setTypeface(font);
			} else if (v instanceof ViewGroup)
				setFont((ViewGroup) v, font);
		}
	}

	/**
	 * Initializes the items in the now playing screen
	 */
	@SuppressWarnings("deprecation")
	private void initPlaybackControls() {
		// ViewPager container
		mPageContainer = (FrameLayout) findViewById(R.id.audio_player_pager_container);
		// Theme the pager container background
		mPageContainer
				.setBackgroundResource(R.drawable.audio_player_pager_container);

		// Now playing header
		mAudioPlayerHeader = (LinearLayout) findViewById(R.id.audio_player_header);
		// Opens the currently playing album profile
		mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
		
		mControlsLayout = (RelativeLayout)findViewById(R.id.controls_layout);

		// Used to hide the artwork and show the queue
		final FrameLayout mSwitch = (FrameLayout) findViewById(R.id.audio_player_switch);
		mSwitch.setOnClickListener(mToggleHiddenPanel);

		// Initialize the pager adapter
		mPagerAdapter = new PagerAdapter(this);
		// Queue
		mPagerAdapter.add(QueueFragment.class, null);

		// Initialize the ViewPager
		mViewPager = (ViewPager) findViewById(R.id.audio_player_pager);
		// Attch the adapter
		mViewPager.setAdapter(mPagerAdapter);
		// Offscreen pager loading limit
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
		// Play and pause button
		mPlayPauseButton = (PlayPauseButton) findViewById(R.id.action_button_play);
		// Shuffle button
		mShuffleButton = (ShuffleButton) findViewById(R.id.action_button_shuffle);
		// Repeat button
		mRepeatButton = (RepeatButton) findViewById(R.id.action_button_repeat);
		// Previous button
		mPreviousButton = (RepeatingImageButton) findViewById(R.id.action_button_previous);
		// Next button
		mNextButton = (RepeatingImageButton) findViewById(R.id.action_button_next);
		// Track name
		mTrackName = (TextView) findViewById(R.id.audio_player_track_name);
		// Artist name
		mArtistName = (TextView) findViewById(R.id.audio_player_artist_name);
		// Album art
		mAlbumArt = (ImageView) findViewById(R.id.audio_player_album_art);
		// Small album art
		mAlbumArtSmall = (ImageView) findViewById(R.id.audio_player_switch_album_art);
		// Current time
		mCurrentTime = (TextView) findViewById(R.id.audio_player_current_time);
		// Seek time
		mSeekTime = (TextView) findViewById(R.id.seek_time);
		// Total time
		mTotalTime = (TextView) findViewById(R.id.audio_player_total_time);
		// Used to show and hide the queue fragment
		mQueueSwitch = (ImageView) findViewById(R.id.audio_player_switch_queue);
		// Theme the queue switch icon
		mQueueSwitch.setImageResource(R.drawable.btn_switch_queue);
		// Progress
		mProgress = (SeekBar) findViewById(android.R.id.progress);

		// Set the repeat listner for the previous button
		mPreviousButton.setRepeatListener(mRewindListener);
		// Set the repeat listner for the next button
		mNextButton.setRepeatListener(mFastForwardListener);
		// Update the progress
		mProgress.setOnSeekBarChangeListener(this);
	}

	/**
	 * Sets the track name, album name, and album art.
	 */
	private void updateNowPlayingInfo() {
		// Set the track name
		mTrackName.setText(MusicUtils.getTrackName());
		// Set the artist name
		mArtistName.setText(MusicUtils.getArtistName()+" - "+MusicUtils.getAlbumName());
		// Set the total time
		mTotalTime.setText(MusicUtils.makeTimeString(this,
				MusicUtils.duration() / 1000));
		// Set the album art
		mImageFetcher.loadCurrentArtwork(mAlbumArt);
		// Set the small artwork
		mImageFetcher.loadCurrentArtwork(mAlbumArtSmall);
		
		if(!mTrackName.getText().toString().equals(mLastPlayingSong))
			
		mControlsLayout.setBackgroundColor(Color.parseColor(Utils.generateRandomColorNew(Math.abs((int)MusicUtils.getCurrentAlbumId()))));
		
		mControlsLayout.setFitsSystemWindows(true);
		mControlsLayout.setClipToPadding(true);
		//changeControlsBackground();

		// processImage();
		// Update the current time
		queueNextRefresh(1);
		
		mLastPlayingSong = mTrackName.getText().toString();

	}

	
	private void changeControlsBackground(){
		
		 
	       new AsyncTask<String[],Integer, Integer>(){
	 			
				  @Override
					protected Integer doInBackground(String[]... params) {
					   int color =0;
		        	   Bitmap bitmap = mImageFetcher.getArtwork(MusicUtils.getAlbumName(), MusicUtils.getCurrentAlbumId(), MusicUtils.getArtistName());
			           
		        	   ColorArt colorArt = new ColorArt(bitmap);

		       		
						
					  return colorArt.getDetailColor();
					}
					protected void onPreExecute() {
			    		
			    		 mControlsLayout.setBackgroundColor(getResources().getColor(R.color.transparent_black));
					}
					
					
					@Override
					protected void onPostExecute(Integer result){
						
						mControlsLayout.setBackgroundColor(result);
						
					}

					
					
					
	     	}.execute();
		
	}
	
	private long parseIdFromIntent(Intent intent, String longKey,
			String stringKey, long defaultId) {
		long id = intent.getLongExtra(longKey, -1);
		if (id < 0) {
			String idString = intent.getStringExtra(stringKey);
			if (idString != null) {
				try {
					id = Long.parseLong(idString);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		return id;
	}

	/**
	 * Checks whether the passed intent contains a playback request, and starts
	 * playback if that's the case
	 */
	private void startPlayback() {
		Intent intent = getIntent();

		if (intent == null || mService == null) {
			return;
		}

		Uri uri = intent.getData();
		String mimeType = intent.getType();
		boolean handled = false;

		if (uri != null && uri.toString().length() > 0) {
			MusicUtils.playFile(this, uri);
			handled = true;
		} else if (Playlists.CONTENT_TYPE.equals(mimeType)) {
			long id = parseIdFromIntent(intent, "playlistId", "playlist", -1);
			if (id >= 0) {
				MusicUtils.playPlaylist(this, id);
				handled = true;
			}
		} else if (Albums.CONTENT_TYPE.equals(mimeType)) {
			long id = parseIdFromIntent(intent, "albumId", "album", -1);
			if (id >= 0) {
				int position = intent.getIntExtra("position", 0);
				MusicUtils.playAlbum(this, id, position);
				handled = true;
			}
		} else if (Artists.CONTENT_TYPE.equals(mimeType)) {
			long id = parseIdFromIntent(intent, "artistId", "artist", -1);
			if (id >= 0) {
				int position = intent.getIntExtra("position", 0);
				MusicUtils.playArtist(this, id, position);
				handled = true;
			}
		}

		if (handled) {
			// Make sure to process intent only once
			setIntent(new Intent());
			// Refresh the queue
			((QueueFragment) mPagerAdapter.getFragment(0)).refreshQueue();
		}
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
	 * @param delay
	 *            When to update
	 */
	private void queueNextRefresh(final long delay) {
		if (!mIsPaused) {
			final Message message = mTimeHandler.obtainMessage(REFRESH_TIME);
			mTimeHandler.removeMessages(REFRESH_TIME);
			mTimeHandler.sendMessageDelayed(message, delay);
		}
	}

	/**
	 * Used to scan backwards in time through the curren track
	 * 
	 * @param repcnt
	 *            The repeat count
	 * @param delta
	 *            The long press duration
	 */
	private void scanBackward(final int repcnt, long delta) {
		if (mService == null) {
			return;
		}
		if (repcnt == 0) {
			mStartSeekPos = MusicUtils.position();
			mLastSeekEventTime = 0;
		} else {
			if (delta < 5000) {
				// seek at 10x speed for the first 5 seconds
				delta = delta * 10;
			} else {
				// seek at 40x after that
				delta = 50000 + (delta - 5000) * 40;
			}
			long newpos = mStartSeekPos - delta;
			if (newpos < 0) {
				// move to previous track
				MusicUtils.previous(this);
				final long duration = MusicUtils.duration();
				mStartSeekPos += duration;
				newpos += duration;
			}
			if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
				MusicUtils.seek(newpos);
				mLastSeekEventTime = delta;
			}
			if (repcnt >= 0) {
				mPosOverride = newpos;
			} else {
				mPosOverride = -1;
			}
			refreshCurrentTime();
		}
	}

	/**
	 * Used to scan forwards in time through the curren track
	 * 
	 * @param repcnt
	 *            The repeat count
	 * @param delta
	 *            The long press duration
	 */
	private void scanForward(final int repcnt, long delta) {
		if (mService == null) {
			return;
		}
		if (repcnt == 0) {
			mStartSeekPos = MusicUtils.position();
			mLastSeekEventTime = 0;
		} else {
			if (delta < 5000) {
				// seek at 10x speed for the first 5 seconds
				delta = delta * 10;
			} else {
				// seek at 40x after that
				delta = 50000 + (delta - 5000) * 40;
			}
			long newpos = mStartSeekPos + delta;
			final long duration = MusicUtils.duration();
			if (newpos >= duration) {
				// move to next track
				MusicUtils.next();
				mStartSeekPos -= duration; // is OK to go negative
				newpos -= duration;
			}
			if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
				MusicUtils.seek(newpos);
				mLastSeekEventTime = delta;
			}
			if (repcnt >= 0) {
				mPosOverride = newpos;
			} else {
				mPosOverride = -1;
			}
			refreshCurrentTime();
		}
	}

	private void refreshCurrentTimeText(final long pos) {
		mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
		mSeekTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
	}

	/* Used to update the current time string */
	private long refreshCurrentTime() {
		if (mService == null) {
			return 500;
		}
		try {
			final long pos = mPosOverride < 0 ? MusicUtils.position()
					: mPosOverride;
			if (pos >= 0 && MusicUtils.duration() > 0) {
				refreshCurrentTimeText(pos);
				final int progress = (int) (1000 * pos / MusicUtils.duration());
				mProgress.setProgress(progress);

				if (mFromTouch) {
					return 500;
				} else if (MusicUtils.isPlaying()) {
					mCurrentTime.setVisibility(View.VISIBLE);
				} else {
					// blink the counter
					final int vis = mCurrentTime.getVisibility();
					mCurrentTime
							.setVisibility(vis == View.INVISIBLE ? View.VISIBLE
									: View.INVISIBLE);
					return 500;
				}
			} else {
				mCurrentTime.setText("--:--");
				mSeekTime.setText("--:--");

				mProgress.setProgress(1000);
			}
			// calculate the number of milliseconds until the next full second,
			// so
			// the counter can be updated at just the right time
			final long remaining = 1000 - pos % 1000;
			// approximate how often we would need to refresh the slider to
			// move it smoothly
			int width = mProgress.getWidth();
			if (width == 0) {
				width = 320;
			}
			final long smoothrefreshtime = MusicUtils.duration() / width;
			if (smoothrefreshtime > remaining) {
				return remaining;
			}
			if (smoothrefreshtime < 20) {
				return 20;
			}
			return smoothrefreshtime;
		} catch (final Exception ignored) {

		}
		return 500;
	}

	/**
	 * @param v
	 *            The view to animate
	 * @param alpha
	 *            The alpha to apply
	 */
	private void fade(final View v, final float alpha) {
		final ObjectAnimator fade = ObjectAnimator.ofFloat(v, "alpha", alpha);
		fade.setInterpolator(AnimationUtils.loadInterpolator(this,
				android.R.anim.accelerate_decelerate_interpolator));
		fade.setDuration(400);
		fade.start();
	}

	/**
	 * Called to show the album art and hide the queue
	 */
	private void showAlbumArt() {
		mPageContainer.setVisibility(View.INVISIBLE);
		mAlbumArtSmall.setVisibility(View.GONE);
		mQueueSwitch.setVisibility(View.VISIBLE);
		// Fade out the pager container
		fade(mPageContainer, 0f);
		// Fade in the album art
		fade(mAlbumArt, 1f);
	}

	/**
	 * Called to hide the album art and show the queue
	 */
	public void hideAlbumArt() {
		mPageContainer.setVisibility(View.VISIBLE);
		mQueueSwitch.setVisibility(View.GONE);
		mAlbumArtSmall.setVisibility(View.VISIBLE);
		// Fade out the artwork
		fade(mAlbumArt, 0f);
		// Fade in the pager container
		fade(mPageContainer, 1f);
	}

	/**
	 * /** Used to shared what the user is currently listening to
	 */
	private void shareCurrentTrack() {
		if (MusicUtils.getTrackName() == null
				|| MusicUtils.getArtistName() == null) {
			return;
		}
		final Intent shareIntent = new Intent();
		final String shareMessage = getString(R.string.now_listening_to,
				MusicUtils.getTrackName(), MusicUtils.getArtistName());

		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
		startActivity(Intent.createChooser(shareIntent,
				getString(R.string.share_track_using)));
	}

	/**
	 * Used to scan backwards through the track
	 */
	private final RepeatingImageButton.RepeatListener mRewindListener = new RepeatingImageButton.RepeatListener() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onRepeat(final View v, final long howlong, final int repcnt) {
			scanBackward(repcnt, howlong);
		}
	};

	/**
	 * Used to scan ahead through the track
	 */
	private final RepeatingImageButton.RepeatListener mFastForwardListener = new RepeatingImageButton.RepeatListener() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onRepeat(final View v, final long howlong, final int repcnt) {
			scanForward(repcnt, howlong);
		}
	};

	/**
	 * Switches from the large album art screen to show the queue and lyric
	 * fragments, then back again
	 */
	private final OnClickListener mToggleHiddenPanel = new OnClickListener() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClick(final View v) {
			if (mPageContainer.getVisibility() == View.VISIBLE) {
				// Open the current album profile
				mAudioPlayerHeader.setOnClickListener(mOpenAlbumProfile);
				// Show the artwork, hide the queue
				mArtShown = true;
				showAlbumArt();
			} else {
				// Scroll to the current track
				mAudioPlayerHeader.setOnClickListener(mScrollToCurrentSong);
				// Show the queue, hide the artwork
				mArtShown = false;
				hideAlbumArt();
			}
		}
	};

	/**
	 * Opens to the current album profile
	 */
	private final OnClickListener mOpenAlbumProfile = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			NavUtils.openAlbumProfile(AudioPlayerActivity.this,
					MusicUtils.getAlbumName(), MusicUtils.getArtistName(),
					MusicUtils.getCurrentAlbumId());
		}
	};

	/**
	 * Scrolls the queue to the currently playing song
	 */
	private final OnClickListener mScrollToCurrentSong = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			((QueueFragment) mPagerAdapter.getFragment(0))
					.scrollToCurrentSong();
		}
	};

	/**
	 * Used to update the current time string
	 */
	private static final class TimeHandler extends Handler {

		private final WeakReference<AudioPlayerActivity> mAudioPlayer;

		/**
		 * Constructor of <code>TimeHandler</code>
		 */
		public TimeHandler(final AudioPlayerActivity player) {
			mAudioPlayer = new WeakReference<AudioPlayerActivity>(player);
		}

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case REFRESH_TIME:
				final long next = mAudioPlayer.get().refreshCurrentTime();
				mAudioPlayer.get().queueNextRefresh(next);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * Used to monitor the state of playback
	 */
	private static final class PlaybackStatus extends BroadcastReceiver {

		private final WeakReference<AudioPlayerActivity> mReference;

		/**
		 * Constructor of <code>PlaybackStatus</code>
		 */
		public PlaybackStatus(final AudioPlayerActivity activity) {
			mReference = new WeakReference<AudioPlayerActivity>(activity);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (action.equals(MusicPlaybackService.META_CHANGED)) {
				// Current info
				mReference.get().updateNowPlayingInfo();
				// Update the favorites icon
				mReference.get().invalidateOptionsMenu();
			} else if (action.equals(MusicPlaybackService.PLAYSTATE_CHANGED)) {
				// Set the play and pause image
				mReference.get().mPlayPauseButton.updateState();
			} else if (action.equals(MusicPlaybackService.REPEATMODE_CHANGED)
					|| action.equals(MusicPlaybackService.SHUFFLEMODE_CHANGED)) {
				// Set the repeat image
				mReference.get().mRepeatButton.updateRepeatState();
				// Set the shuffle image
				mReference.get().mShuffleButton.updateShuffleState();
			}
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
		if (SettingsActivity.getTranslucentMode(this) && Utils.hasKitKat()) {

			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayUseLogoEnabled(false);

			actionBar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#000000ff")));
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#000000ff")));

		} else {

			/*Drawable colorDrawable = new ColorDrawable(color);
			Drawable bottomDrawable = getResources().getDrawable(
					R.drawable.transparent_overlay);
			LayerDrawable ld = new LayerDrawable(new Drawable[] {
					colorDrawable, bottomDrawable });

			if (oldBackground == null) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					ld.setCallback(drawableCallback);
				} else {
					actionBar.setBackgroundDrawable(colorDrawable);
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
					actionBar.setBackgroundDrawable(td);
				}
				td.startTransition(200);
			}

			oldBackground = ld;

			// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);

			if (Utils.hasKitKat()) {
				if (SettingsActivity.getTranslucentMode(this)) {
					SystemBarTintManager tintManager = new SystemBarTintManager(
							this);
					tintManager.setStatusBarTintEnabled(true);
					tintManager.setStatusBarTintColor(color);

				}

			}*/
			
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayUseLogoEnabled(false);

			actionBar.setBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#000000ff")));
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color
					.parseColor("#000000ff")));
		}
		updateSeekbartheme(color);

	}

	private void updateSeekbartheme(int color) {

		/*
		 * ShapeDrawable pgDrawable = new ShapeDrawable(new RectShape());
		 * 
		 * // Sets the progressBar color pgDrawable.getPaint().setColor(color);
		 * 
		 * // Adds the drawable to your progressBar ClipDrawable progress = new
		 * ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		 * mProgress.setProgressDrawable(progress);
		 * mProgress.setBackgroundDrawable
		 * (getResources().getDrawable(R.drawable.progress_shape));
		 */

		ShapeDrawable shape = new ShapeDrawable();
		shape.getPaint().setStyle(Style.FILL);
		shape.getPaint().setColor(
				getResources().getColor(R.color.transparent_black));

		ShapeDrawable shapeD = new ShapeDrawable();
		shapeD.getPaint().setStyle(Style.FILL);
		shapeD.getPaint().setColor(getResources().getColor(R.color.white));
		ClipDrawable clipDrawable = new ClipDrawable(shapeD, Gravity.LEFT,
				ClipDrawable.HORIZONTAL);

		LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] {
				shape,clipDrawable });
		mProgress.setProgressDrawable(layerDrawable);

	}

	public static Drawable createDrawable(Context context, int color) {

		ShapeDrawable shape = new ShapeDrawable();
		shape.getPaint().setStyle(Style.FILL);
		shape.setIntrinsicHeight(1);
		shape.getPaint().setColor(
				context.getResources().getColor(R.color.transparent));

		shape.getPaint().setStyle(Style.STROKE);
		shape.getPaint().setStrokeWidth(4);
		shape.getPaint().setColor(color);

		ShapeDrawable shapeD = new ShapeDrawable();
		shapeD.getPaint().setStyle(Style.FILL);
		shapeD.getPaint().setColor(color);
		ClipDrawable clipDrawable = new ClipDrawable(shapeD, Gravity.LEFT,
				ClipDrawable.HORIZONTAL);

		LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] {
				clipDrawable, shape });
		return layerDrawable;
	}

	protected Drawable convertToGrayscale(Drawable drawable) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0);

		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

		drawable.setColorFilter(filter);

		return drawable;
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

}
