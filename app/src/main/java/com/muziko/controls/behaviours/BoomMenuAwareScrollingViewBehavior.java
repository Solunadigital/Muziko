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
import com.muziko.R;
import com.nightonke.boommenu.BoomMenuButton;

/**
 * Created by Bradley on 3/02/2017.
 */


public class BoomMenuAwareScrollingViewBehavior
        extends AppBarLayout.ScrollingViewBehavior {

    private Animation jumpToDown;
    private Animation jumpFromDown;
    private WeakHandler handler = new WeakHandler();
    private BoomMenuButton boomMenuButton;
    private boolean boomDown = false;
    private final Runnable showBoomMenu = new Runnable() {
        @Override
        public void run() {
            boomDown = false;
            boomMenuButton.startAnimation(jumpFromDown);
        }
    };

    public BoomMenuAwareScrollingViewBehavior(Context context, AttributeSet attrs) {
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
                dependency instanceof BoomMenuButton;
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
            // User scrolled down -> hide the BoomMenuButton

            if (!boomDown) {
                boomDown = true;
//                boomMenuButton = child.findViewById(R.id.bmb);
                jumpFromDown.cancel();
                boomMenuButton.startAnimation(jumpToDown);
                handler.removeCallbacksAndMessages(showBoomMenu);
                handler.postDelayed(showBoomMenu, 1500);
            }

        } else if (dyConsumed < 0) {
            // User scrolled up -> show the BoomMenuButton

            if (!boomDown) {
                boomDown = true;
//                boomMenuButton = child.findViewById(R.id.bmb);
                jumpFromDown.cancel();
                boomMenuButton.startAnimation(jumpToDown);
                handler.removeCallbacksAndMessages(showBoomMenu);
                handler.postDelayed(showBoomMenu, 1500);
            }
        }
    }
}