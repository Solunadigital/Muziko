package com.muziko.controls;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

public class NonFocusableNestedScrollView extends NestedScrollView {

	public NonFocusableNestedScrollView(Context context) {
		super(context);
	}

	public NonFocusableNestedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonFocusableNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
		return true;
	}
}
