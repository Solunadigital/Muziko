package com.muziko.common.models;

import android.support.annotation.Keep;

@Keep
public class ReverbItem {
    public final short id;
    public final String title;

    public ReverbItem(short id, String title) {
        this.id = id;
        this.title = title;
    }

}

