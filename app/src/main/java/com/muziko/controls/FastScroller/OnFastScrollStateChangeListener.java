package com.muziko.controls.FastScroller;

/**
 * Created by Bradley on 10/02/2017.
 */

public interface OnFastScrollStateChangeListener {

	/**
	 * Called when fast scrolling begins
	 */
	void onFastScrollStart();

	/**
	 * Called when fast scrolling ends
	 */
	void onFastScrollStop();
}