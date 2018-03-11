package com.muziko.controls;

import android.widget.AbsListView;

/**
 * Created by dev on 9/08/2016.
 */
public abstract class InifniteScrollListener implements AbsListView.OnScrollListener {
	private int mCurrentPage = 0;
	private int mPreviousTotalItemCount = 0;
	private boolean mLoading = true;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
	                     int visibleItemCount, int totalItemCount) {
		if (totalItemCount < mPreviousTotalItemCount) {
			int mStartingPageIndex = 0;
			mCurrentPage = mStartingPageIndex;
			mPreviousTotalItemCount = totalItemCount;
			if (totalItemCount == 0) {
				mLoading = true;
			}
		}

		if (mLoading && (totalItemCount > mPreviousTotalItemCount)) {
			mLoading = false;
			mPreviousTotalItemCount = totalItemCount;
			mCurrentPage++;
		}

		int mVisibleThreshold = 5;
		if (!mLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold)) {
			onLoadMore(mCurrentPage + 1, totalItemCount);
			mLoading = true;
		}
	}

	public abstract void onLoadMore(int page, int totalItemsCount);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}
}
