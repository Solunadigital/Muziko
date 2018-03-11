package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by dev on 13/07/2016.
 */
@Keep
public class FirebaseShareCountEvent {

    public final int count;

    public FirebaseShareCountEvent(int count) {
        this.count = count;
    }
}