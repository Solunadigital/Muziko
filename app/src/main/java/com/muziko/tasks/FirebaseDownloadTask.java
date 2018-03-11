package com.muziko.tasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.FirebaseCloudEvent;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.events.TrackAddedEvent;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.controls.FileTransferNotification;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.NotificationController;
import com.muziko.manager.SettingsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.receivers.NotificationBroadcast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Created by dev on 27/10/2016.
 */

public class FirebaseDownloadTask extends AsyncTask<Void, Double, Void> {

    private final WeakReference<Context> mContext;
    private final CloudTrack cloudTrack;
    private final MuzikoConstants.FirebaseFileMode firebaseFileMode;
    private String playlistHash;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int mProgress = 0;
    private int nID;
    private String downloadPath;
    private FileDownloadTask downloadTask;
    private File downloadFile;
    private Notification notification;

    public FirebaseDownloadTask(Context ctx, CloudTrack cloudTrack, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = new WeakReference<>(ctx);
        this.cloudTrack = cloudTrack;
        this.firebaseFileMode = firebaseFileMode;
    }

    public FirebaseDownloadTask(Context ctx, CloudTrack cloudTrack, MuzikoConstants.FirebaseFileMode firebaseFileMode, String downloadPath) {
        mContext = new WeakReference<>(ctx);
        this.cloudTrack = cloudTrack;
        this.firebaseFileMode = firebaseFileMode;
        this.downloadPath = downloadPath;
    }

    public FirebaseDownloadTask(Context ctx, CloudTrack cloudTrack, String playlistHash, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = new WeakReference<>(ctx);
        this.cloudTrack = cloudTrack;
        this.playlistHash = playlistHash;
        this.firebaseFileMode = firebaseFileMode;
    }

    public FirebaseDownloadTask(Context ctx, CloudTrack cloudTrack, String playlistHash, MuzikoConstants.FirebaseFileMode firebaseFileMode, String downloadPath) {
        mContext = new WeakReference<>(ctx);
        this.cloudTrack = cloudTrack;
        this.playlistHash = playlistHash;
        this.firebaseFileMode = firebaseFileMode;
        this.downloadPath = downloadPath;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (downloadPath == null) {
            downloadFile = FileHelper.getDownloadFolder(cloudTrack.getFileName());
            if (downloadFile == null) {
                new MaterialDialog.Builder(mContext.get())
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .negativeColorRes(R.color.dialog_negetive_button)
                        .positiveColorRes(R.color.normal_blue)
                        .title(R.string.default_sync_location)
                        .content(R.string.default_sync_location_desc)
                        .positiveText(R.string.ok)
                        .onPositive(
                                (dialog, which) -> {
                                    SettingsManager.Instance().setPrefSyncLocation(0);
                                    downloadFile = FileHelper.getDownloadFolder(mContext.get(), downloadPath, cloudTrack.getFileName());
                                })
                        .negativeText(R.string.cancel)
                        .onNegative(
                                (dialog, which) -> {
                                    cancelDownload();
                                })
                        .show();
            }
        } else {
            downloadFile = FileHelper.getDownloadFolder(mContext.get(), downloadPath, cloudTrack.getFileName());
        }

        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        final StorageReference httpsReference = storageRef.getReferenceFromUrl(cloudTrack.getUrl());

        downloadTask = httpsReference.getFile(downloadFile);

        downloadTask.addOnSuccessListener(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), taskSnapshot -> {
            FirebaseManager.Instance().setDownloadRunning(false);
            FirebaseManager.Instance().removeFirebaseDownloadTask(cloudTrack.getUrl());

            mBuilder.setContentText(mContext.get().getString(R.string.download_complete));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);

            MediaScannerConnection.scanFile(mContext.get(),
                    new String[]{downloadFile.getAbsolutePath()}, null,
                    (path, uri) -> {

                        MediaHelper.Instance().loadMusicFromTrack(downloadFile.getAbsolutePath(), false);

                        QueueItem queueItem = TrackRealmHelper.getTrack(downloadFile.getAbsolutePath());

                        if (queueItem != null) {
                            if (queueItem.md5 == null) {
                                queueItem.md5 = cloudTrack.getMd5();
                                TrackRealmHelper.updateMD5Hash(queueItem);
                            }
                            switch (firebaseFileMode) {
                                case LIBRARY:
                                    TrackRealmHelper.toggleLibrary(queueItem, true);
                                    TrackRealmHelper.deleteFirebaseTrackByMD5(cloudTrack.getMd5());
                                    break;
                                case FAVS:
                                    TrackRealmHelper.toggleFavorite(queueItem);
                                    TrackRealmHelper.toggleSync(queueItem, true);
                                    TrackRealmHelper.deleteFirebaseTrackByMD5(cloudTrack.getMd5());
                                    break;
                                case PLAYLISTS:
                                    PlaylistQueueItem playlistQueueItem = new PlaylistQueueItem();
                                    playlistQueueItem.copyQueue(queueItem);
                                    PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(playlistHash);
                                    playlistQueueItem.playlist = playlistItem.id;
                                    PlaylistSongRealmHelper.insert(playlistQueueItem, true);
                                    PlaylistRealmHelper.toggleSync(playlistItem.id, true);
                                    PlaylistSongRealmHelper.deleteByData(cloudTrack.getUrl());
                                    TrackRealmHelper.deleteFirebaseTrackByMD5(cloudTrack.getMd5());
                                    break;
                            }

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
                            boolean prefArtworkDownload = prefs.getBoolean("prefArtworkDownload", false);
                            if (queueItem != null && prefArtworkDownload) {
                                ArtworkHelper artworkHelper = new ArtworkHelper();
                                artworkHelper.autoPickAlbumArt(mContext.get(), queueItem, true);
                            }
                        }

                        EventBus.getDefault().post(new TrackAddedEvent(queueItem.data));

                        AsyncJob.doInBackground(() -> {
                            try {
                                String cloudResult = CloudManager.Instance().getMuzikoCloud().complete(cloudTrack.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();
                            } catch (IOException e) {
                                Crashlytics.logException(e);
                            }
                        });

                        pl.tajchert.buswear.EventBus.getDefault(mContext.get()).postLocal(new FirebaseRefreshEvent(1000));

                        Intent shareintent = new Intent(AppController.INTENT_SHARE_DOWNLOADED);
                        shareintent.putExtra("data", queueItem.data);
                        MyApplication.getInstance().sendBroadcast(shareintent);
                    });
        });

        downloadTask.addOnProgressListener(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            if (!isCancelled() && (progress > 1 && (mProgress + 3 < progress || (mProgress > 90 & mProgress + 1 < progress)))) {
                mProgress = (int) progress;

                String subTitle = "";
                switch (firebaseFileMode) {
                    case LIBRARY:
                        subTitle = mContext.get().getString(R.string.download_library_track);
                        break;
                    case FAVS:
                        subTitle = mContext.get().getString(R.string.download_fav_track);
                        break;
                    case PLAYLISTS:
                        subTitle = mContext.get().getString(R.string.download_playlist_track);
                        break;
                }

                RemoteViews simpleContentView = FileTransferNotification.createStandardViewFromCloudTrack(mContext.get(), cloudTrack, subTitle, mProgress, false);
                mBuilder.setCustomContentView(simpleContentView);
                mBuilder.setContentTitle(mContext.get().getString(R.string.downloading_files) + " " + cloudTrack.getTitle())
                        .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());
                mBuilder.setDeleteIntent(getDeleteIntent(cloudTrack.getUrl()));
                notification = mBuilder.build();
                mNotifyManager.notify(nID, notification);


                Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
                progressIntent.putExtra("url", cloudTrack.getUrl());
                progressIntent.putExtra("progress", mProgress);
                mContext.get().sendBroadcast(progressIntent);
            }
        });

        downloadTask.addOnFailureListener(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), exception -> {
            mBuilder.setContentText(mContext.get().getString(R.string.download_error));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);

            Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
            progressIntent.putExtra("url", cloudTrack.getUrl());
            progressIntent.putExtra("progress", -1);
            mContext.get().sendBroadcast(progressIntent);

            FirebaseManager.Instance().setDownloadRunning(false);
            FirebaseManager.Instance().removeFirebaseDownloadTask(cloudTrack.getUrl());

            AsyncJob.doInBackground(() -> {
                try {
                    String cloudResult = CloudManager.Instance().getMuzikoCloud().complete(cloudTrack.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                }
            });
        });

        downloadTask.resume();

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        int min = 0;
        int max = 1000;

        AsyncJob.doInBackground(() -> {
            try {
                String cloudResult = CloudManager.Instance().getMuzikoCloud().download(cloudTrack.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();
                String message = "";
                if (cloudResult.equals(MuzikoConstants.CloudFileActions.UPLOAD.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_uploading_downloading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(cloudTrack.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancelDownload();
                    return;
                } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DOWNLOAD.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_downloading_downloading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(cloudTrack.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancelDownload();
                    return;
                } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DELETE.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_delete_downloading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(cloudTrack.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancelDownload();
                    return;
                }
            } catch (IOException e) {
                Crashlytics.logException(e);
            }

            if (firebaseFileMode == MuzikoConstants.FirebaseFileMode.PLAYLISTS) {
                try {
                    String cloudResult = CloudManager.Instance().getMuzikoCloud().upload(playlistHash, FirebaseInstanceId.getInstance().getToken()).execute().getData();
                    String message = "";
                    if (cloudResult.equals(MuzikoConstants.CloudFileActions.UPLOAD.name())) {
                        cancel(true);
                    } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DOWNLOAD.name())) {
                        cancel(true);
                    } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DELETE.name())) {
                        cancel(true);
                    }

                } catch (IOException e) {
                    Crashlytics.logException(e);
                }
            }
        });


        FirebaseManager.Instance().setDownloadRunning(true);

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;


        mNotifyManager =
                (android.app.NotificationManager)
                        MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(MyApplication.getInstance(), "Muziko");

        String subTitle = "";
        switch (firebaseFileMode) {
            case LIBRARY:
                subTitle = mContext.get().getString(R.string.download_library_track);
                break;
            case FAVS:
                subTitle = mContext.get().getString(R.string.download_fav_track);
                break;
            case PLAYLISTS:
                subTitle = mContext.get().getString(R.string.download_playlist_track);
                break;
        }
        RemoteViews simpleContentView = FileTransferNotification.createStandardViewFromCloudTrack(mContext.get(), cloudTrack, subTitle, 0, false);
        mBuilder.setCustomContentView(simpleContentView);
        mBuilder.setContentTitle(mContext.get().getString(R.string.downloading_files) + " " + cloudTrack.getTitle())
                .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());
        mBuilder.setDeleteIntent(getDeleteIntent(cloudTrack.getUrl()));
        notification = mBuilder.build();

        mNotifyManager.notify(nID, notification);

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        mNotifyManager.cancelAll();

    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
    }


    private PendingIntent getDeleteIntent(String shareUrl) {
        Intent intent = new Intent(mContext.get(), NotificationBroadcast.class);
        intent.setAction(AppController.NOTIFY_CANCEL_FIREBASE_DOWNLOAD);
        intent.putExtra("shareUrl", shareUrl);
        return PendingIntent.getBroadcast(mContext.get(), 0, intent, nID);
    }

    public void cancelDownload() {
        mBuilder.setContentText(mContext.get().getString(R.string.download_cancelled));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        mNotifyManager.cancel(nID);

        FirebaseManager.Instance().setDownloadRunning(false);
        FirebaseManager.Instance().removeFirebaseDownloadTask(cloudTrack.getUrl());

        downloadTask.cancel();
        cancel(true);
    }

}
