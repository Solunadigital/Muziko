package com.muziko.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bluelinelabs.logansquare.LoganSquare;
import com.muziko.PlayerConstants;
import com.muziko.common.CommonConstants;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.events.buswear.QueueAddEvent;
import com.muziko.common.events.buswear.QueueFullUpdateEvent;
import com.muziko.common.events.buswear.QueueProgressEvent;
import com.muziko.common.events.buswear.QueueRemoveEvent;
import com.muziko.common.events.buswear.QueueUpdateEvent;
import com.muziko.common.events.buswear.WearActionEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.Queuelist;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import pl.tajchert.buswear.EventBus;


/**
 * Created by dev on 7/11/2016.
 */
public class MuzikoWearService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault(this).register(this);
    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {


            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        EventBus.getDefault(this).unregister(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWearActionEvent(WearActionEvent wearActionEvent) {

        if (wearActionEvent.getName().equals(CommonConstants.ACTION_HANDHELD_CLEAR)) {
            PlayerConstants.QUEUE_LIST.clear();
            EventBus.getDefault(this).postLocal(new RefreshEvent(0));
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueProgressEvent(QueueProgressEvent queueProgressEvent) {

        PlayerConstants.QUEUE_TIME = queueProgressEvent.getPosition();
        PlayerConstants.QUEUE_DURATION = queueProgressEvent.getDuration();
        EventBus.getDefault(this).postLocal(new ProgressEvent(PlayerConstants.QUEUE_INDEX, PlayerConstants.QUEUE_TIME, PlayerConstants.QUEUE_DURATION));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueUpdateEvent(QueueUpdateEvent queueUpdateEvent) {

        PlayerConstants.QUEUE_INDEX = queueUpdateEvent.getQueueindex();
        PlayerConstants.QUEUE_STATE = queueUpdateEvent.getState();
        PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(PlayerConstants.QUEUE_INDEX);
        EventBus.getDefault(this).postLocal(new RefreshEvent(0));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueRemoveEvent(QueueRemoveEvent queueRemoveEvent) {

        PlayerConstants.QUEUE_LIST.remove((int) queueRemoveEvent.getPosition());
        EventBus.getDefault(this).postLocal(new RefreshEvent(0));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueAddEvent(QueueAddEvent queueAddEvent) {

        String json = queueAddEvent.getName();
        Queuelist queuelist = new Queuelist();
        try {
            queuelist = LoganSquare.parse(json, Queuelist.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (queuelist != null) {
            for (int i = 0; i < queuelist.size(); i++) {
                QueueItem queueItem = queuelist.get(i);
                if (PlayerConstants.QUEUE_LIST.size() > queueAddEvent.getPosition()) {
                    PlayerConstants.QUEUE_LIST.set(queueAddEvent.getPosition(), queueItem);
                } else {
                    PlayerConstants.QUEUE_LIST.add(queueItem);
                }
            }
            EventBus.getDefault(this).postLocal(new RefreshEvent(0));
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onQueueFullUpdateEvent(QueueFullUpdateEvent queueFullUpdateEvent) {

        PlayerConstants.QUEUE_INDEX = queueFullUpdateEvent.getPosition();
        PlayerConstants.QUEUE_STATE = queueFullUpdateEvent.getState();
        if (PlayerConstants.QUEUE_LIST.size() > queueFullUpdateEvent.getPosition()) {
            PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(PlayerConstants.QUEUE_INDEX);
        }

        String json = queueFullUpdateEvent.getName();
        Queuelist queuelist = new Queuelist();
        try {
            queuelist = LoganSquare.parse(json, Queuelist.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (queuelist != null) {
            if (queueFullUpdateEvent.getFresh() == 0) {
                PlayerConstants.QUEUE_LIST.clear();
            }
            for (int i = 0; i < queuelist.size(); i++) {
                QueueItem queueItem = queuelist.get(i);
                PlayerConstants.QUEUE_LIST.add(queueItem);
            }
            EventBus.getDefault(this).postLocal(new RefreshEvent(0));
        }
    }
}
