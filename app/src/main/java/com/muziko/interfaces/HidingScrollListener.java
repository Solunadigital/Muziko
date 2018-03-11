package com.muziko.interfaces;

import android.support.v7.widget.RecyclerView;

import com.muziko.MyApplication;

/**
 * Created by dev on 20/09/2016.
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
	private final int HIDE_THRESHOLD = 20;
	private int scrolledDistance = 0;
	private boolean controlsVisible = true;

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

		if (MyApplication.playerListLastScrollChange == 0) {
			MyApplication.playerListLastScrollChange = System.currentTimeMillis();
		}

		if (scrolledDistance > HIDE_THRESHOLD && controlsVisible && MyApplication.playerListLastScrollChange + 200 < System.currentTimeMillis()) {
			onHide();
			MyApplication.playerListLastScrollChange = System.currentTimeMillis();
			controlsVisible = false;
			scrolledDistance = 0;
		} else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible && MyApplication.playerListLastScrollChange + 200 < System.currentTimeMillis()) {
			onShow();
			MyApplication.playerListLastScrollChange = System.currentTimeMillis();
			controlsVisible = true;
			scrolledDistance = 0;
		}

		if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
			scrolledDistance += dy;
		}
	}

	public abstract void onHide();

	public abstract void onShow();

}