package com.muziko.controls;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Bradley on 13/02/2017.
 */

public abstract class ReyclerViewFastScrollListener extends RecyclerView.OnScrollListener {
	private LinearLayoutManager linearLayoutManager;
	private GridLayoutManager gridLayoutManager;
	private int firstVisible = -1;
	private int itemCount = -1;
	private int buffer = 10;

	public ReyclerViewFastScrollListener(LinearLayoutManager linearLayoutManager) {
		this.linearLayoutManager = linearLayoutManager;
	}

	public ReyclerViewFastScrollListener(GridLayoutManager gridLayoutManager) {
		this.gridLayoutManager = gridLayoutManager;
	}


	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

		if (linearLayoutManager != null) {
			firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
			itemCount = linearLayoutManager.getItemCount();
		} else {
			firstVisible = gridLayoutManager.findFirstVisibleItemPosition();
			itemCount = gridLayoutManager.getItemCount();
		}


		if (firstVisible < (itemCount / 2)) {
			//top
			if (firstVisible < buffer) {
				onShowFirst();
			}
		} else {
			if (itemCount - firstVisible < buffer) {
				onShowLast();
			}
		}
	}

	public abstract void onShowLast();

	public abstract void onShowFirst();
}