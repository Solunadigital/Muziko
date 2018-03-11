package com.muziko.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.FirebaseCloudEvent;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudPlaylist;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.MD5;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.NotificationController;
import com.muziko.manager.ThreadManager;
import com.muziko.receivers.NotificationBroadcast;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.muziko.manager.FirebaseManager.favs;
import static com.muziko.manager.FirebaseManager.library;
import static com.muziko.manager.FirebaseManager.playlists;

/**
 * Created by dev on 27/10/2016.
 */
public class FirebaseUploadTask extends AsyncTask<Void, Double, Void> {

    private final WeakReference<Context> mContext;
    private final QueueItem queueItem;
    private final MuzikoConstants.FirebaseFileMode firebaseFileMode;
    private String playlistHash;
    private int mProgress = 0;
    private CloudPlaylist cloudPlaylist;
    private CloudTrack cloudTrack;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;
    private UploadTask uploadTask;

    public FirebaseUploadTask(Context ctx, QueueItem queueItem, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = new WeakReference<>(ctx);
        this.queueItem = queueItem;
        this.firebaseFileMode = firebaseFileMode;
    }

    public FirebaseUploadTask(Context ctx, QueueItem queueItem, String playlistHash, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = new WeakReference<>(ctx);
        this.queueItem = queueItem;
        this.playlistHash = playlistHash;
        this.firebaseFileMode = firebaseFileMode;
    }

    public QueueItem getQueueItem() {
        return queueItem;
    }

    @Override
    protected Void doInBackground(Void... params) {

        File mFile = new File(queueItem.getData());

        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference fileRef = storageRef.getReferenceFromUrl("gs://" + mContext.get().getString(R.string.google_storage_bucket));

//        final String uuid = Utils.generateSha1Hash(queueItem.data);
        if (queueItem.md5 == null || queueItem.md5.isEmpty()) {
            queueItem.md5 = MD5.calculateMD5(mFile);
            TrackRealmHelper.updateMD5Hash(queueItem);
        }

        final String uuid = queueItem.md5;

        final String filename = mFile.getName();
//        final File localfile = FileHelper.getDownloadFolder(Uri.parse(queueItem.data).getLastPathSegment());

        StorageReference storageReference = null;
        switch (firebaseFileMode) {
            case LIBRARY:
                storageReference = fileRef.child(library).child(FirebaseManager.Instance().getCurrentUserId()).child(uuid);
                break;
            case FAVS:
                storageReference = fileRef.child(favs).child(FirebaseManager.Instance().getCurrentUserId()).child(uuid);
                break;
            case PLAYLISTS:
                storageReference = fileRef.child(playlists).child(FirebaseManager.Instance().getCurrentUserId()).child(playlistHash).child(uuid);
                break;
        }

        byte bytes[] = new byte[0];
        try {
            bytes = FileUtils.readFileToByteArray(mFile);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        uploadTask = storageReference.putBytes(bytes);

        uploadTask.addOnSuccessListener(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), taskSnapshot -> {
            mBuilder.setContentText(mContext.get().getString(R.string.upload_complete));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);

            final Uri shareUrl = taskSnapshot.getDownloadUrl();

            cloudTrack = new CloudTrack(
                    uuid,
                    filename,
                    shareUrl.toString(),
                    queueItem.data,
                    queueItem.title,
                    queueItem.artist_name,
                    queueItem.album_name,
                    queueItem.duration,
                    queueItem.md5,
                    queueItem.dateModified,
                    ServerValue.TIMESTAMP);

            DatabaseReference uploadRef = null;
            switch (firebaseFileMode) {
                case LIBRARY:
                    uploadRef = FirebaseManager.Instance().getLibraryRef().child(FirebaseManager.Instance().getCurrentUserId());
                    uploadRef.child(uuid).setValue(cloudTrack, (error, firebase) -> {
                        if (error != null) {
                            AppController.toast(mContext.get(), mContext.get().getString(R.string.network_lost));
                        }
                    });
                    break;
                case FAVS:
                    uploadRef = FirebaseManager.Instance().getFavRef().child(FirebaseManager.Instance().getCurrentUserId());
                    uploadRef.child(uuid).setValue(cloudTrack, (error, firebase) -> {
                        if (error != null) {
                            AppController.toast(mContext.get(), mContext.get().getString(R.string.network_lost));
                        }
                    });
                    break;
                case PLAYLISTS:
                    uploadRef = FirebaseManager.Instance().getPlaylistsTracksRef().child(FirebaseManager.Instance().getCurrentUserId());
                    uploadRef.child(uuid).setValue(cloudTrack, (error, firebase) -> {
                        if (error != null) {
                            AppController.toast(mContext.get(), mContext.get().getString(R.string.network_lost));
                        }
                    });

                    PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(playlistHash);
                    ArrayList<QueueItem> queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, playlistItem.id);
                    List<String> cloudTracks = new ArrayList<>();
                    for (QueueItem queueItem : queueItems) {
                        cloudTracks.add(queueItem.md5);
                    }
                    cloudPlaylist = new CloudPlaylist(playlistHash, playlistItem.id, playlistItem.title, playlistItem.duration, playlistItem.date, cloudTracks);
                    FirebaseManager.Instance().getPlaylistsRef().child(FirebaseManager.Instance().getCurrentUserId()).child(playlistHash).setValue(cloudPlaylist, (error, firebase) -> {
                        if (error != null) {
                            AppController.toast(mContext.get(), "Network connection failed");
                        } else {
                            FirebaseManager.Instance().removePlaylistUploadTask(playlistHash);
                        }
                    });


                    break;
            }

            AsyncJob.doInBackground(() -> {
                try {
                    String cloudResult = CloudManager.Instance().getMuzikoCloud().complete(queueItem.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                }
            });

            FirebaseManager.Instance().setUploadRunning(false);
            FirebaseManager.Instance().removeFirebaseUploadTask(queueItem.data);
        });

        uploadTask.addOnFailureListener(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), e -> {
            MyApplication.pauseDeletingTempRingtone = false;
            mBuilder.setContentText(mContext.get().getString(R.string.upload_error));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);

            Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
            progressIntent.putExtra("url", cloudTrack.getUrl());
            progressIntent.putExtra("progress", -1);
            mContext.get().sendBroadcast(progressIntent);

            AsyncJob.doInBackground(() -> {
                try {
                    String cloudResult = CloudManager.Instance().getMuzikoCloud().complete(queueItem.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();
                } catch (IOException ex) {
                    Crashlytics.logException(ex);
                }
            });

            FirebaseManager.Instance().setUploadRunning(false);
            FirebaseManager.Instance().removeFirebaseUploadTask(queueItem.data);
        });

        uploadTask.addOnProgressListener(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), taskSnapshot -> {
            double progress =
                    (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            if (!isCancelled() && (progress > 1 && (mProgress + 3 < progress || (mProgress > 90 & mProgress + 1 < progress)))) {
                mProgress = (int) progress;
                int currentprogress = (int) progress;
                mBuilder.setProgress(100, currentprogress, false);
                mNotifyManager.notify(nID, mBuilder.build());

                Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
                progressIntent.putExtra("url", queueItem.data);
                progressIntent.putExtra("progress", mProgress);
                mContext.get().sendBroadcast(progressIntent);
            }
        });

        uploadTask.addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused"));

        uploadTask.resume();

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        int min = 10000;
        int max = 100000;

        AsyncJob.doInBackground(() -> {
            try {
                String cloudResult = CloudManager.Instance().getMuzikoCloud().upload(queueItem.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();
                String message = "";
                if (cloudResult.equals(MuzikoConstants.CloudFileActions.UPLOAD.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_uploading_uploading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(queueItem.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancelUpload();
                    return;
                } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DOWNLOAD.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_downloading_uploading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(queueItem.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancelUpload();
                    return;
                } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DELETE.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_delete_uploading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent(queueItem.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancelUpload();
                    return;
                }

            } catch (IOException e) {
                Crashlytics.logException(e);
                return;
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


        FirebaseManager.Instance().setUploadRunning(true);

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager)
                        MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(MyApplication.getInstance(), "Muziko");
        String title = null;
        switch (firebaseFileMode) {
            case LIBRARY:
                title = mContext.get().getString(R.string.upload_library_track);
                mBuilder.setContentTitle(title)
                        .setContentText(queueItem.title)
                        .setSmallIcon(NotificationController.Instance().getUploadNotificationIcon())
                        .setProgress(100, 0, false)
                        .setDeleteIntent(getDeleteIntent(queueItem.getData()));
                break;
            case FAVS:
                title = mContext.get().getString(R.string.upload_fav_track);
                mBuilder.setContentTitle(title)
                        .setContentText(queueItem.title)
                        .setSmallIcon(NotificationController.Instance().getUploadNotificationIcon())
                        .setProgress(100, 0, false)
                        .setDeleteIntent(getDeleteIntent(queueItem.getData()));
                break;
            case PLAYLISTS:
                title = mContext.get().getString(R.string.upload_playlist_track);
                mBuilder.setContentTitle(title)
                        .setContentText(queueItem.title)
                        .setSmallIcon(NotificationController.Instance().getUploadNotificationIcon())
                        .setProgress(100, 0, false)
                        .addAction(NotificationController.Instance().getCancelNotificationIcon(), "Cancel", getCancelIntent(queueItem.getData()));
                break;
        }

        mNotifyManager.notify(nID, mBuilder.build());
    }

    @Override
    protected void onPostExecute(Void result) {
        mNotifyManager.cancelAll();
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
    }

    private PendingIntent getCancelIntent(String data) {
        Intent intent = new Intent(mContext.get(), NotificationBroadcast.class);
        intent.setAction(AppController.NOTIFY_CANCEL_FIREBASE_UPLOAD);
        intent.putExtra("data", data);
        return PendingIntent.getBroadcast(mContext.get(), 0, intent, nID);
    }

    private PendingIntent getDeleteIntent(String data) {
        Intent intent = new Intent(mContext.get(), NotificationBroadcast.class);
        if (firebaseFileMode == MuzikoConstants.FirebaseFileMode.LIBRARY) {
            intent.setAction(AppController.NOTIFY_REMOVE_FIREBASE_LIBRARY_UPLOAD);
        } else if (firebaseFileMode == MuzikoConstants.FirebaseFileMode.FAVS) {
            intent.setAction(AppController.NOTIFY_REMOVE_FIREBASE_FAV_UPLOAD);
        }
        intent.putExtra("data", data);
        return PendingIntent.getBroadcast(mContext.get(), 0, intent, nID);
    }

    public void cancelUpload() {

        try {

            mBuilder.setContentText(mContext.get().getString(R.string.upload_cancelled));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);

            FirebaseManager.Instance().setUploadRunning(false);
            FirebaseManager.Instance().removeFirebaseUploadTask(cloudTrack.getUrl());

            uploadTask.cancel();

        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
}
