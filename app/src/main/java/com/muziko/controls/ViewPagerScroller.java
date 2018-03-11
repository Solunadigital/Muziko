package com.muziko.controls;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by Bradley on 6/02/2017.
 */


public class ViewPagerScroller extends Scroller {

	private static final int MIN = 0;
	private static final int MAX = 3000;
	private int mDuration = 0;

	public ViewPagerScroller(Context context, Interpolator interpolator, int duration) {
		super(context, interpolator);
		mDuration = duration;
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		// Ignore received duration, use fixed one instead
		super.startScroll(startX, startY, dx, dy, mDuration);
	}

	public int getCustomDuration() {
		return mDuration;
	}

	public void setCustomDuration(int customDuration) {
		if (customDuration <= MIN) {
			mDuration = MIN;
		} else if (customDuration >= MAX) {
			mDuration = MAX;
		} else {
			mDuration = customDuration;
		}
	}
}