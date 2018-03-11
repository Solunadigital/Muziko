package com.muziko.controls.LayoutManagers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by dev on 19/10/2016.
 */

public class WrapContentLinearLayoutManager extends LinearLayoutManager {
	public WrapContentLinearLayoutManager(Context context) {
		super(context);
	}

	public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
	}

	public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	//... constructor
	@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
		try {
			super.onLayoutChildren(recycler, state);
		} catch (IndexOutOfBoundsException e) {
			Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens");
		}
	}
}
