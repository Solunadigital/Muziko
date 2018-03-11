package com.muziko.common.models;

import android.support.annotation.Keep;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;

/**
 * Created by Bradley on 1/03/2017.
 */
@Keep
@JsonObject
public class Queuelist {

    @JsonField
    private ArrayList<QueueItem> queueItems;

    public ArrayList<QueueItem> getQueueItems() {
        return queueItems;
    }

    public void setQueueItems(ArrayList<QueueItem> queueItems) {
        this.queueItems = queueItems;
    }

    public int size() {
        return queueItems.size();
    }

    public QueueItem get(int i) {
        return queueItems.get(i);
    }
}
