package com.ksharkapps.musicnow.ui.widgets;
import org.apache.http.cookie.SetCookie;

import com.ksharkapps.musicnow.preferences.SettingsActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;


/**
 * Used as a thin strip placed just above the bottom action bar or just below
 * the top action bar.
 * 
 * @author Kartik Sharma (kshark.apps@gmail.com)
 */
public class Colorstrip extends View {

	/**
	 * Resource name used to theme the colorstrip
	 */
	private static final String COLORSTRIP = "colorstrip";

	/**
	 * @param context
	 *            The {@link Context} to use
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the view.
	 */
	public Colorstrip(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		// Initialze the theme resources
		// Theme the colorstrip
		int color = SettingsActivity.getActionBarColor(context);
		setBackgroundColor(color);

	}
}
