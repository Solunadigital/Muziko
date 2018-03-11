package com.muziko.controls.behaviours;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.badoo.mobile.util.WeakHandler;
import com.github.clans.fab.FloatingActionButton;
import com.muziko.R;

/**
 * Created by Bradley on 3/02/2017.
 */


public class FabScrollingViewBehavior
        extends AppBarLayout.ScrollingViewBehavior {

    private Animation jumpToDown;
    private Animation jumpFromDown;
    private WeakHandler handler = new WeakHandler();
    private FloatingActionButton clipFab;
    private boolean fabDown = false;
    private final Runnable showFab = new Runnable() {
        @Override
        public void run() {
            fabDown = false;
            clipFab.startAnimation(jumpFromDown);
        }
    };

    public FabScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        jumpToDown = AnimationUtils.loadAnimation(context,
                R.anim.jump_to_down);
        jumpToDown.setFillAfter(true);

        jumpFromDown = AnimationUtils.loadAnimation(context,
                R.anim.jump_from_down);
        jumpFromDown.setFillAfter(true);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent,
                                   View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency) ||
                dependency instanceof FloatingActionButton;
    }

    @Override
    public boolean onStartNestedScroll(
            final CoordinatorLayout coordinatorLayout, final View child,
            final View directTargetChild, final View target,
            final int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child,
                directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(
            final CoordinatorLayout coordinatorLayout, final View child,
            final View target, final int dxConsumed, final int dyConsumed,
            final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target,
                dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0) {

            if (!fabDown) {
                fabDown = true;
//                clipFab = child.findViewById(R.id.clipFab);
                jumpFromDown.cancel();
                clipFab.startAnimation(jumpToDown);
                handler.removeCallbacksAndMessages(showFab);
                handler.postDelayed(showFab, 1500);
            }

        } else if (dyConsumed < 0) {

            if (!fabDown) {
                fabDown = true;
//                clipFab = child.findViewById(R.id.clipFab);
                jumpFromDown.cancel();
                clipFab.startAnimation(jumpToDown);
                handler.removeCallbacksAndMessages(showFab);
                handler.postDelayed(showFab, 1500);
            }
        }
    }
}