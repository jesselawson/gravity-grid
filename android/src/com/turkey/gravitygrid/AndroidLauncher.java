/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.turkey.gravitygrid.GravityGrid;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		// Remove the android menu from some android devices
		config.useImmersiveMode = true;

		// Sort of following https://developers.google.com/android/reference/com/google/android/gms/ads/AdView

		RelativeLayout layout = new RelativeLayout(this);

		// Create layout parameters for our ad
		RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		// Create the game view
		View gameView = initializeForView(new GravityGrid(), config);

		// Create a banner ad
		AdView theAdView = new AdView(this);
		theAdView.setAdSize(AdSize.SMART_BANNER);


		theAdView.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id)); // Get the banner_ad_unit_id which we set as a string in strings.xml

		// Create an ad request
		AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

		// Add the game view
		layout.addView(gameView);

		// Add the AdView to the view hierarchy
		//layout.addView(theAdView, adParams);

		// Start loading the add
		theAdView.loadAd(adRequestBuilder.build());

		setContentView(layout);



		//initialize(new GravityGrid(), config);
	}
}
