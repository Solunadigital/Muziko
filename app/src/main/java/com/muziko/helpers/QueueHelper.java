package com.muziko.helpers;

import android.content.Context;

import com.bluelinelabs.logansquare.LoganSquare;
import com.crashlytics.android.Crashlytics;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.Queuelist;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dev on 27/08/2016.
 */
public class QueueHelper {


    public static void saveQueue(Context context) {

        ArrayList<QueueItem> queueItems = new ArrayList<>();

        for (QueueItem queueItem : PlayerConstants.QUEUE_LIST) {
            queueItems.add(queueItem);
        }

        Queuelist queuelist = new Queuelist();
        queuelist.setQueueItems(queueItems);
        String json = null;
        try {
            json = LoganSquare.serialize(queuelist);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
        PrefsManager.Instance().setQueueList(json);
    }

    public static void loadQueue(Context context) {

        Queuelist queuelist = new Queuelist();
        String json = PrefsManager.Instance().getQueueList();
        try {
            queuelist = LoganSquare.parse(json, Queuelist.class);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        if (queuelist != null) {
            PlayerConstants.QUEUE_LIST.addAll(queuelist.getQueueItems());
        }
    }
}
