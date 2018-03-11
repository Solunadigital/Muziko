package com.muziko.interfaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Picasso;

/**
 * Created by dev on 6/09/2016.
 */
public final class PicassoScrollListener extends RecyclerView.OnScrollListener {

	private final int SETTLING_DELAY = 1500;
	private final Object mTag;
	private Context mContext = null;
	private Runnable mSettlingResumeRunnable = null;

	public PicassoScrollListener(Context context, Object tag) {
		mContext = context;
		mTag = tag;
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
		if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
			recyclerView.removeCallbacks(mSettlingResumeRunnable);
			Picasso.with(mContext).resumeTag(mTag);

		} else if (scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
			mSettlingResumeRunnable = () -> Picasso.with(mContext).resumeTag(mTag);

			recyclerView.postDelayed(mSettlingResumeRunnable, SETTLING_DELAY);

		} else {
			Picasso.with(mContext).pauseTag(mTag);
		}
	}

}
