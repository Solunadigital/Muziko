package com.muziko.events;

/**
 * Created by Bradley on 16/03/2017.
 */

public class ClipboardEvent {
    private String message;

    public ClipboardEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
