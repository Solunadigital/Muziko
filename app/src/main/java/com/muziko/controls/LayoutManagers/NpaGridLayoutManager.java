package com.muziko.controls.LayoutManagers;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

/**
 * No Predictive Animations GridLayoutManager
 */
public class NpaGridLayoutManager extends GridLayoutManager {
	public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public NpaGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
	}

	public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	/**
	 * Disable predictive animations. There is a bug in RecyclerView which causes views that
	 * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
	 * adapter size has decreased since the ViewHolder was recycled.
	 */
	@Override
	public boolean supportsPredictiveItemAnimations() {
		return false;
	}
}