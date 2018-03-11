package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by dev on 13/07/2016.
 */
@Keep
public class TrackAddedEvent {

    public final String trackData;

    public TrackAddedEvent(String trackData) {

        this.trackData = trackData;
    }
}