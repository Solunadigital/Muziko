package com.muziko.events;

/**
 * Created by Bradley on 16/03/2017.
 */

public class BufferingEvent {
    private String message;
    private boolean close;

    public BufferingEvent(String message, boolean close) {
        this.message = message;
        this.close = close;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
