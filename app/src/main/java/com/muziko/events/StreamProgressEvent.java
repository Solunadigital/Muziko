package com.muziko.events;

/**
 * Created by Bradley on 16/03/2017.
 */

public class StreamProgressEvent {
    private int action;

    public StreamProgressEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
