package com.muziko.helpers;

import android.view.animation.Interpolator;

/**
 * Created by Bradley on 28/02/2017.
 */

public class ItemTouchHelpers {

    public static final Interpolator sDragScrollInterpolator = t -> {
//            return t * t * t * t * t; // default return value, but it's too late for me
        return (int) Math.pow(2, (double) t); // optional whatever you like
    };
    // default
    public static final Interpolator sDragViewScrollCapInterpolator = t -> {
        t -= 1.0f;
        return t * t * t * t * t + 1.0f;
    };
}
