package com.muziko.controls;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by dev on 15/07/2016.
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
	private final LinearLayoutManager mLinearLayoutManager;
	public String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();
	private int firstVisibleItem;
	private int visibleItemCount;
	private int totalItemCount;
	private int previousTotal = 0; // The total number of items in the dataset after the last load
	private boolean loading = true; // True if we are still waiting for the last set of data to load.
	private int current_page = 1;

	public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
		this.mLinearLayoutManager = linearLayoutManager;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

		visibleItemCount = recyclerView.getChildCount();
		totalItemCount = mLinearLayoutManager.getItemCount();
		firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}
		int visibleThreshold = 30;
		if (!loading && (totalItemCount - visibleItemCount)
				<= (firstVisibleItem + visibleThreshold)) {
			// End has been reached

			// Do something
			current_page++;

			onLoadMore(current_page);

			loading = true;
		}
	}

	public abstract void onLoadMore(int current_page);
}