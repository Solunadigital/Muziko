package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by dev on 13/07/2016.
 */
@Keep
public class PlayerPreparedEvent {

    public final int delay;

    public PlayerPreparedEvent(int delay) {
        this.delay = delay;
    }
}