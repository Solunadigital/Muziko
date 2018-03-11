package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 10/02/2017.
 */
@Keep
public class ProgressEvent {
    private int position;
    private int progress;
    private int duration;

    public ProgressEvent(int position, int progress, int duration) {
        this.position = position;
        this.progress = progress;
        this.duration = duration;
    }

    public int getProgress() {
        return progress;
    }

    public int getDuration() {
        return duration;
    }

    public int getPosition() {
        return position;
    }
}
