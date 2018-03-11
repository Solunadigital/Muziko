package com.muziko.controls;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.animation.Interpolator;

/**
 * Created by lgaji on 2015/06/09.
 */
public abstract class ExtendItemTouchHelperCallback extends ItemTouchHelper.Callback {

	private static final long DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS = 2000; // default
	// default
    private static final Interpolator sDragScrollInterpolator = t -> {
//            return t * t * t * t * t; // default return value, but it's too late for me
        return (int) Math.pow(2, (double) t); // optional whatever you like
    };
    // default
    private static final Interpolator sDragViewScrollCapInterpolator = t -> {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    };
    private int mCachedMaxScrollSpeed = -1; // default

	@Override
	public abstract int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

	@Override
	public abstract boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1);

	@Override
	public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int i);

	@Override
	public abstract void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

	@Override
	public abstract void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState);

	@Override
	public abstract void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive);

	@Override
	public abstract void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive);

	// default
	@Override
	public int interpolateOutOfBoundsScroll(RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
//        return super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);

		final int maxScroll = getMaxDragScroll(recyclerView);
		final int absOutOfBounds = Math.abs(viewSizeOutOfBounds);
		final int direction = (int) Math.signum(viewSizeOutOfBounds);
		// might be negative if other direction
		float outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize);
		final int cappedScroll = (int) (direction * maxScroll *
				sDragViewScrollCapInterpolator.getInterpolation(outOfBoundsRatio));
		final float timeRatio;
		if (msSinceStartScroll > DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS) {
			timeRatio = 1f;

//            timeRatio = 3f;
		} else {
			timeRatio = (float) msSinceStartScroll / DRAG_SCROLL_ACCELERATION_LIMIT_TIME_MS;
		}
		final int value = (int) (cappedScroll * sDragScrollInterpolator
				.getInterpolation(timeRatio));
		if (value == 0) {
			return viewSizeOutOfBounds > 0 ? 1 : -1;
		}
		return value;
	}

	// default
	private int getMaxDragScroll(RecyclerView recyclerView) {
		if (mCachedMaxScrollSpeed == -1) {
			mCachedMaxScrollSpeed = recyclerView.getResources().getDimensionPixelSize(
					android.support.v7.recyclerview.R.dimen.item_touch_helper_max_drag_scroll_per_frame);
		}
		return mCachedMaxScrollSpeed;
	}
}