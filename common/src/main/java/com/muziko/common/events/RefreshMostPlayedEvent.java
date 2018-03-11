package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by dev on 13/07/2016.
 */
@Keep
public class RefreshMostPlayedEvent {

    public final int delay;

    public RefreshMostPlayedEvent(int delay) {
        this.delay = delay;
    }
}