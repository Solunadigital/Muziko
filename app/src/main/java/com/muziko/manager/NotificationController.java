package com.muziko.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.receivers.NotificationBroadcast;
import com.muziko.service.SongService;

import java.util.ArrayList;
import java.util.Locale;

import hugo.weaving.DebugLog;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;
import static com.muziko.service.SongService.NOTIFY_DELETE;
import static com.muziko.service.SongService.NOTIFY_NEXT;
import static com.muziko.service.SongService.NOTIFY_PAUSE;
import static com.muziko.service.SongService.NOTIFY_PLAY;
import static com.muziko.service.SongService.NOTIFY_PREVIOUS;
import static com.muziko.service.SongService.NOTIFY_REPEAT;
import static com.muziko.service.SongService.NOTIFY_SHUFFLE;
import static com.muziko.service.SongService.currentVersionSupportBigNotification;
import static com.muziko.service.SongService.notiBuilder;

/**
 * Created by dev on 28/09/2016.
 */
public class NotificationController {

    private static NotificationController instance;
    private Context mContext;
    private QueueItem queueItem;
    private Notification notification;
    private RemoteViews expandedView;
    private RemoteViews simpleContentView;
    private QueueItem nextItem;
    private PendingIntent pDelete;
    private PendingIntent pPrevious;
    private PendingIntent pNext;
    private PendingIntent pPause;
    private PendingIntent pPlay;
    private PendingIntent prepeat;
    private PendingIntent pshuffle;
    private ArrayList<QueueItem> queueItems;

    //no outer class can initialize this class's object
    private NotificationController() {
    }

    public static NotificationController Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new NotificationController();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public int getUploadNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_cloud_upload_white_24dp;
        } else {
            return R.drawable.ic_cloud_upload_black_24dp;
        }
    }

    public int getDownloadNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_cloud_download_white_24dp;
        } else {
            return R.drawable.ic_cloud_download_black_24dp;
        }
    }

    public int getSyncNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_sync_white_24dp;
        } else {
            return R.drawable.ic_sync_black_24dp;
        }
    }

    public int getSharedNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.share_notification_white;
        } else {
            return R.drawable.share_notification_white;
        }
    }

    public int getUpdateNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_system_update_white_24dp;
        } else {
            return R.drawable.ic_system_update_black_24dp;
        }
    }

    public int getCancelNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_cancel_white_24dp;
        } else {
            return R.drawable.ic_cancel_black_24dp;
        }
    }

    public int getDeleteNotificationIcon() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return R.drawable.ic_delete_forever_white_24dp;
        } else {
            return R.drawable.ic_delete_forever_black_24dp;
        }
    }

    private QueueItem nextSong(int position, QueueItem queueItem) {
        QueueItem item = null;

        if (PlayerConstants.QUEUE_LIST.size() > 0) {
            int repeat = PrefsManager.Instance().getPlayRepeat();
            if (repeat == PlayerConstants.REPEAT_ONE) {
                item = queueItem;
            } else {
                if (PrefsManager.Instance().getPlayShuffle(mContext)) {

                } else {
                    int index = position + 1;
                    if (index >= PlayerConstants.QUEUE_LIST.size()) {
                        if (repeat == PlayerConstants.REPEAT_OFF) {
                            item = PlayerConstants.QUEUE_LIST.get(0);
                        } else //repeat all
                        {
                            if (index > PlayerConstants.QUEUE_LIST.size() - 1) {
                                item =
                                        PlayerConstants.QUEUE_LIST.get(
                                                PlayerConstants.QUEUE_LIST.size() - 1);
                            } else {
                                item = PlayerConstants.QUEUE_LIST.get(index);
                            }
                        }
                    } else {
                        item = PlayerConstants.QUEUE_LIST.get(index);
                    }
                }
            }
        }

        return item;
    }

    private RemoteViews createExpandedView(int position, QueueItem queueItem) {

        String songName = queueItem.title;
        String albumName = queueItem.album_name;

        String url = "content://media/external/audio/albumart/" + queueItem.album;

        nextItem = nextSong(position, queueItem);
        String songInfo = "";
        if (PlayerConstants.QUEUE_LIST.size() > 0) {
            songInfo = String.format(Locale.ENGLISH, "%d/%d %s", PlayerConstants.QUEUE_INDEX + 1, PlayerConstants.QUEUE_LIST.size(), (nextItem == null ? "" : String.format("Next: %s - %s ", nextItem.title, nextItem.artist_name)));
        }

        expandedView = new RemoteViews(mContext.getPackageName(), R.layout.notification_player_large);

        switch ((int) queueItem.album) {
            case CloudManager.GOOGLEDRIVE:
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.drive_large);
                break;

            case CloudManager.DROPBOX:
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.dropbox_large);
                break;

            case CloudManager.BOX:
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.box_large);
                break;

            case CloudManager.ONEDRIVE:
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.onedrive_large);
                break;

            case CloudManager.AMAZON:
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.amazon_large);
                break;

            case CloudManager.FIREBASE:
                expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.firebase_large);
                break;

            default:
                if (Utils.contentURIExists(mContext, url)) {
                    expandedView.setImageViewUri(R.id.imageViewAlbumArt, Uri.parse(url));
                } else {
                    expandedView.setImageViewResource(R.id.imageViewAlbumArt, R.mipmap.placeholder);
                }
        }

        expandedView.setTextViewText(R.id.textSongName, songName);
        expandedView.setTextViewText(R.id.textAlbumName, albumName);
        expandedView.setTextViewText(R.id.textInfo, songInfo);
        expandedView.setViewVisibility(R.id.textInfo, songInfo.length() > 0 ? View.VISIBLE : View.GONE);
        expandedView.setProgressBar(R.id.progressBar, 0, 0, true);

        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_PLAYING) {
            expandedView.setViewVisibility(R.id.btnPause, View.GONE);
            expandedView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
            expandedView.setChronometer(R.id.timetext, SystemClock.elapsedRealtime() - PlayerConstants.QUEUE_TIME, null, false);
        } else {
            expandedView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            expandedView.setViewVisibility(R.id.btnPlay, View.GONE);
            expandedView.setChronometer(R.id.timetext, SystemClock.elapsedRealtime() - PlayerConstants.QUEUE_TIME, null, true);
        }

        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                expandedView.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_gray_48dp);
                break;

            case PlayerConstants.REPEAT_ALL:
                expandedView.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_black_48dp);
                break;

            case PlayerConstants.REPEAT_ONE:
                expandedView.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_one_black_48dp);
                break;
        }

        if (PrefsManager.Instance().getPlayShuffle(mContext)) {
            expandedView.setImageViewResource(R.id.btnShuffle, R.drawable.ic_shuffle_black_48dp);
        } else {
            expandedView.setImageViewResource(R.id.btnShuffle, R.drawable.ic_shuffle_gray_48dp);
        }

        return expandedView;
    }

    private RemoteViews createStandardView(int position, QueueItem queueItem) {
        String songName = queueItem.title;
        String albumName = queueItem.album_name;
        String url = "content://media/external/audio/albumart/" + queueItem.album;

        nextItem = nextSong(position, queueItem);
        String songInfo = "";
        if (PlayerConstants.QUEUE_LIST.size() > 0) {
            songInfo = String.format(Locale.ENGLISH, "%d/%d %s", PlayerConstants.QUEUE_INDEX + 1, PlayerConstants.QUEUE_LIST.size(), (nextItem == null ? "" : String.format("Next: %s - %s ", nextItem.title, nextItem.artist_name)));
        }

        simpleContentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_player_small);

        switch ((int) queueItem.album) {
            case CloudManager.GOOGLEDRIVE:
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.drive_large);
                break;

            case CloudManager.DROPBOX:
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.dropbox_large);
                break;

            case CloudManager.BOX:
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.box_large);
                break;

            case CloudManager.ONEDRIVE:
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.onedrive_large);
                break;

            case CloudManager.AMAZON:
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.amazon_large);
                break;

            case CloudManager.FIREBASE:
                simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.firebase_large);
                break;

            default:
                if (Utils.contentURIExists(mContext, url)) {
                    simpleContentView.setImageViewUri(R.id.imageViewAlbumArt, Uri.parse(url));
                } else {
                    simpleContentView.setImageViewResource(R.id.imageViewAlbumArt, R.mipmap.placeholder);
                }
        }

        simpleContentView.setTextViewText(R.id.textSongName, songName);
        simpleContentView.setTextViewText(R.id.textAlbumName, albumName);
        simpleContentView.setTextViewText(R.id.textInfo, songInfo);
        simpleContentView.setViewVisibility(R.id.textInfo, songInfo.length() > 0 ? View.VISIBLE : View.INVISIBLE);

        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_PLAYING) {
            simpleContentView.setViewVisibility(R.id.btnPause, View.GONE);
            simpleContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

        } else {
            simpleContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            simpleContentView.setViewVisibility(R.id.btnPlay, View.GONE);
        }

        return simpleContentView;
    }

    private RemoteViews updateExpandedView(
            RemoteViews expandedView, int position, QueueItem queueItem) {

        nextItem = nextSong(position, queueItem);
        String songInfo = "";
        if (PlayerConstants.QUEUE_LIST.size() > 0) {
            songInfo =
                    String.format(
                            Locale.ENGLISH,
                            "%d/%d %s",
                            PlayerConstants.QUEUE_INDEX + 1,
                            PlayerConstants.QUEUE_LIST.size(),
                            (nextItem == null
                                    ? ""
                                    : String.format(
                                    "Next: %s - %s ",
                                    nextItem.title, nextItem.artist_name)));
        }

        expandedView.setTextViewText(R.id.textInfo, songInfo);
        expandedView.setViewVisibility(
                R.id.textInfo, songInfo.length() > 0 ? View.VISIBLE : View.GONE);

        expandedView.setProgressBar(R.id.progressBar, 0, 0, true);

        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_PLAYING) {
            expandedView.setViewVisibility(R.id.btnPause, View.GONE);
            expandedView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
            expandedView.setChronometer(
                    R.id.timetext,
                    SystemClock.elapsedRealtime() - PlayerConstants.QUEUE_TIME,
                    null,
                    false);
            if (AppController.isBuffering) {
                expandedView.setViewVisibility(R.id.btnPause, View.GONE);
                expandedView.setViewVisibility(R.id.btnPlay, View.GONE);
                expandedView.setViewVisibility(R.id.progressBar, View.VISIBLE);
            } else {
                expandedView.setViewVisibility(R.id.btnPause, View.GONE);
                expandedView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
                expandedView.setViewVisibility(R.id.progressBar, View.GONE);
            }
        } else {
            expandedView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            expandedView.setViewVisibility(R.id.btnPlay, View.GONE);
            expandedView.setChronometer(
                    R.id.timetext,
                    SystemClock.elapsedRealtime() - PlayerConstants.QUEUE_TIME,
                    null,
                    true);
            if (AppController.isBuffering) {
                expandedView.setViewVisibility(R.id.btnPause, View.GONE);
                expandedView.setViewVisibility(R.id.btnPlay, View.GONE);
                expandedView.setViewVisibility(R.id.progressBar, View.VISIBLE);
            } else {
                expandedView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                expandedView.setViewVisibility(R.id.btnPlay, View.GONE);
                expandedView.setViewVisibility(R.id.progressBar, View.GONE);
            }
        }

        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                expandedView.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_gray_48dp);
                break;

            case PlayerConstants.REPEAT_ALL:
                expandedView.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_black_48dp);
                break;

            case PlayerConstants.REPEAT_ONE:
                expandedView.setImageViewResource(
                        R.id.btnRepeat, R.drawable.ic_repeat_one_black_48dp);
                break;
        }

        if (PrefsManager.Instance().getPlayShuffle(mContext)) {
            expandedView.setImageViewResource(R.id.btnShuffle, R.drawable.ic_shuffle_black_48dp);
        } else {
            expandedView.setImageViewResource(R.id.btnShuffle, R.drawable.ic_shuffle_gray_48dp);
        }

        return expandedView;
    }

    private RemoteViews updateStandardView(
            RemoteViews simpleContentView, int position, QueueItem queueItem) {

        nextItem = nextSong(position, queueItem);
        String songInfo = "";
        if (PlayerConstants.QUEUE_LIST.size() > 0) {
            songInfo =
                    String.format(
                            Locale.ENGLISH,
                            "%d/%d %s",
                            PlayerConstants.QUEUE_INDEX + 1,
                            PlayerConstants.QUEUE_LIST.size(),
                            (nextItem == null
                                    ? ""
                                    : String.format(
                                    "Next: %s - %s ",
                                    nextItem.title, nextItem.artist_name)));
        }
        simpleContentView.setTextViewText(R.id.textInfo, songInfo);
        simpleContentView.setViewVisibility(R.id.textInfo, songInfo.length() > 0 ? View.VISIBLE : View.INVISIBLE);

        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_PLAYING) {
            simpleContentView.setViewVisibility(R.id.btnPause, View.GONE);
            simpleContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

        } else {
            simpleContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            simpleContentView.setViewVisibility(R.id.btnPlay, View.GONE);
        }

        return simpleContentView;
    }

    private Notification createNotification(int position, QueueItem queueItem) {

        String songName = queueItem.title;

        Intent myIntent = new Intent(mContext, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        myIntent.setAction(Intent.ACTION_MAIN);
        myIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notiBuilder.setSmallIcon(R.drawable.ic_music);
        notiBuilder.setContentTitle(songName);
        notiBuilder.setAutoCancel(false);
        notiBuilder.setContentIntent(pendingIntent);
        notiBuilder.setOngoing(true);
        notiBuilder.setOnlyAlertOnce(true);
        notiBuilder.setSound(null);
        notiBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(songName));

        Intent delete = new Intent(NOTIFY_DELETE);
        PendingIntent pDelete = PendingIntent.getBroadcast(mContext, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        notiBuilder.setDeleteIntent(pDelete);

        simpleContentView = createStandardView(position, queueItem);
        expandedView = createExpandedView(position, queueItem);

        notiBuilder.setCustomContentView(simpleContentView);
        if (currentVersionSupportBigNotification) {
            notiBuilder.setCustomBigContentView(expandedView);
        }

        notification = notiBuilder.build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        setListeners(simpleContentView);
        setListeners(expandedView);

        return notification;
    }

    private void setListeners(RemoteViews view) {
//        Intent previous = new Intent(mContext, NotificationBroadcast.class);
//        previous.setAction(NOTIFY_PREVIOUS);
//        Intent delete = new Intent(mContext, NotificationBroadcast.class);
//        delete.setAction(NOTIFY_DELETE);
//        Intent pause = new Intent(mContext, NotificationBroadcast.class);
//        pause.setAction(NOTIFY_PAUSE);
//        Intent next = new Intent(mContext, NotificationBroadcast.class);
//        next.setAction(NOTIFY_NEXT);
//        Intent play = new Intent(mContext, NotificationBroadcast.class);
//        play.setAction(NOTIFY_PLAY);
//        Intent repeat = new Intent(mContext, NotificationBroadcast.class);
//        repeat.setAction(NOTIFY_REPEAT);
//        Intent shuffle = new Intent(mContext, NotificationBroadcast.class);
//        shuffle.setAction(NOTIFY_SHUFFLE);


        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);
        Intent repeat = new Intent(NOTIFY_REPEAT);
        Intent shuffle = new Intent(NOTIFY_SHUFFLE);

        pDelete = PendingIntent.getBroadcast(mContext, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        pPrevious = PendingIntent.getBroadcast(mContext, 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        pNext = PendingIntent.getBroadcast(mContext, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        pPause = PendingIntent.getBroadcast(mContext, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        pPlay = PendingIntent.getBroadcast(mContext, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

        prepeat = PendingIntent.getBroadcast(mContext, 0, repeat, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnRepeat, prepeat);

        pshuffle = PendingIntent.getBroadcast(mContext, 0, shuffle, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnShuffle, pshuffle);
    }

    public Notification updateNotification(int position, QueueItem queueItem, Notification notification) {

        try {
            RemoteViews updatedSimpleContentView = updateStandardView(notification.contentView, position, queueItem);
            RemoteViews updatedExpandedView = updateExpandedView(notification.bigContentView, position, queueItem);

            notiBuilder.setCustomContentView(updatedSimpleContentView);
            if (currentVersionSupportBigNotification) {
                notiBuilder.setCustomBigContentView(updatedExpandedView);
            }

            notification = notiBuilder.build();

            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            setListeners(notification.contentView);
            setListeners(notification.bigContentView);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        return notification;
    }

    public Notification getNotificationNotInCache(int position, QueueItem queueItem) {
        notification = null;
        try {
            notification = createNotification(position, queueItem);
            SongService.notificationCache.put(queueItem.data, notification);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return notification;
    }

    public void updateNotificationCacheforTracks(
            ArrayList<QueueItem> queueItems,
            onNoticationCacheListener noticationCacheListener) {

        try {
            for (int i = 0; i < queueItems.size(); i++) {
                queueItem = queueItems.get(i);
                notification = createNotification(i, queueItem);
                SongService.notificationCache.remove(queueItem.data);
                SongService.notificationCache.put(queueItem.data, notification);
            }
            noticationCacheListener.onComplete();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void createNotificationCache(
            onNoticationCacheListener noticationCacheListener) {

        try {
            queueItems = new ArrayList<>();
            queueItems.addAll(TrackRealmHelper.getTracks().values());
            for (int i = 0; i < queueItems.size(); i++) {
                queueItem = queueItems.get(i);
                notification = createNotification(i, queueItem);
                SongService.notificationCache.put(queueItem.data, notification);
                //                if (i == PlayerConstants.QUEUE_INDEX) {
                //                    noticationCacheListener.onfirstItemReady();
                //                }
            }
            queueItem = null;
            nextItem = null;
            notification = null;
            expandedView = null;
            simpleContentView = null;
            pDelete = null;
            pPrevious = null;
            pNext = null;
            pPause = null;
            pPlay = null;
            prepeat = null;
            pshuffle = null;
            queueItems.clear();
            System.gc();
            noticationCacheListener.onComplete();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public interface onNoticationCacheListener {

        void onfirstItemReady();

        void onComplete();
    }
}
