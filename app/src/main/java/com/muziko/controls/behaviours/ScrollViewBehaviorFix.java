package com.muziko.controls.behaviours;


import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ScrollViewBehaviorFix extends AppBarLayout.ScrollingViewBehavior {

	public ScrollViewBehaviorFix() {
		super();
	}

	public ScrollViewBehaviorFix(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static int getContentHeight(View view) {
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;

			int contentHeight = 0;
			for (int index = 0; index < viewGroup.getChildCount(); ++index) {
				View child = viewGroup.getChildAt(index);
				contentHeight += child.getMeasuredHeight();
			}
			return contentHeight;
		} else {
			return view.getMeasuredHeight();
		}
	}

	private static AppBarLayout findFirstAppBarLayout(List<View> views) {
		int i = 0;

		for (int z = views.size(); i < z; ++i) {
			View view = views.get(i);
			if (view instanceof AppBarLayout) {
				return (AppBarLayout) view;
			}
		}

		throw new IllegalArgumentException("Missing AppBarLayout in CoordinatorLayout dependencies");
	}

	public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
	                              int heightUsed) {
		if (child.getLayoutParams().height == -1) {
			List<View> dependencies = parent.getDependencies(child);
			if (dependencies.isEmpty()) {
				return false;
			}

			final AppBarLayout appBar = findFirstAppBarLayout(dependencies);
			if (appBar != null && ViewCompat.isLaidOut(appBar)) {
				int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
				if (availableHeight == 0) {
					availableHeight = parent.getHeight();
				}

				final int height = availableHeight - appBar.getMeasuredHeight();
				int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);

				parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
				int childContentHeight = getContentHeight(child);

				if (childContentHeight <= height) {
					updateToolbar(parent, appBar, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed, false);

					heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
					parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

					return true;
				} else {
					updateToolbar(parent, appBar, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed, true);

					return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
				}
			}
		}

		return false;
	}

	private void updateToolbar(CoordinatorLayout parent, AppBarLayout appBar, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
	                           int heightUsed, boolean toggle) {
		toggleToolbarScroll(appBar, toggle);

		appBar.forceLayout();
		parent.onMeasureChild(appBar, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
	}

	private void toggleToolbarScroll(AppBarLayout appBar, boolean toggle) {
		for (int index = 0; index < appBar.getChildCount(); ++index) {
			View child = appBar.getChildAt(index);

			if (child instanceof Toolbar) {
				Toolbar toolbar = (Toolbar) child;
				AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
				int scrollFlags = params.getScrollFlags();

				if (toggle) {
					scrollFlags |= AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
				} else {
					scrollFlags &= ~AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
				}

				params.setScrollFlags(scrollFlags);
			}
		}
	}
}