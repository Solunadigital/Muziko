package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 10/02/2017.
 */
@Keep
public class FirebaseCloudEvent {
    private String title;
    private String message;

    public FirebaseCloudEvent(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

}
