package com.muziko.controls.behaviours;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v13.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import java.util.List;

/**
 * Created by Matteo on 08/08/2015.
 * <p>
 * Floating Action Menu Behavior for Clans.FloatingActionButton
 * https://github.com/Clans/FloatingActionButton/
 * <p>
 * Use this behavior as your app:layout_behavior attribute in your Floating Action Menu to use the
 * FabMenu in a Coordinator Layout.
 * <p>
 * Remember to use the correct namespace for the fab:
 * xmlns:fab="http://schemas.android.com/apk/res-auto"
 */
public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior {
	private float mTranslationY;

	public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
		super();
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
		if (child instanceof FloatingActionButton && dependency instanceof Snackbar.SnackbarLayout) {
			this.updateTranslation(parent, child, dependency);
		}

		return false;
	}

	private void updateTranslation(CoordinatorLayout parent, View child, View dependency) {
		float translationY = this.getTranslationY(parent, child);
		if (translationY != this.mTranslationY) {
			ViewCompat.animate(child)
					.cancel();
			if (Math.abs(translationY - this.mTranslationY) == (float) dependency.getHeight()) {
				ViewCompat.animate(child)
						.translationY(translationY)
						.setListener(null);
			} else {
				ViewCompat.setTranslationY(child, translationY);
			}

			this.mTranslationY = translationY;
		}

	}

	private float getTranslationY(CoordinatorLayout parent, View child) {
		float minOffset = 0.0F;
		List dependencies = parent.getDependencies(child);
		int i = 0;

		for (int z = dependencies.size(); i < z; ++i) {
			View view = (View) dependencies.get(i);
			if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
				minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float) view.getHeight());
			}
		}

		return minOffset;
	}

	/**
	 * onStartNestedScroll and onNestedScroll will hide/show the FabMenu when a scroll is detected.
	 */
	@Override
	public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child,
	                                   View directTargetChild, View target, int nestedScrollAxes) {
		return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
				super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
						nestedScrollAxes);
	}

	@Override
	public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target,
	                           int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
		super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
				dyUnconsumed);
		FloatingActionButton fab = (FloatingActionButton) child;
		if (dyConsumed > 0 && !fab.isHidden()) {
			fab.hide(true);
		} else if (dyConsumed < 0 && fab.isHidden()) {
			fab.show(true);
		}
	}
}