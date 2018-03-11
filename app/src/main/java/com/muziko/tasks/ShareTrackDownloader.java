package com.muziko.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.muziko.MyApplication;
import com.muziko.common.events.TrackAddedEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Contact;
import com.muziko.common.models.firebase.Share;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.interfaces.DownloadListener;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.NotificationController;
import com.muziko.receivers.NotificationBroadcast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by dev on 27/10/2016.
 */

public class ShareTrackDownloader {

    private final Context mContext;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Share mShare;
    private int mProgress = 0;
    private DatabaseReference shareRef;
    private ValueEventListener shareListener;
    private int nID;
    private DownloadListener mListener;
    private FileDownloadTask downloadTask;

    public ShareTrackDownloader(Context ctx, Share share, DownloadListener downloadCompleteListener) {
        mContext = ctx;
        mShare = share;
        mListener = downloadCompleteListener;
    }

    public void startDownload() {
        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager)
                        mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
        mBuilder.setContentTitle("Getting shared file")
                .setContentText("Download in progress - Swipe to cancel")
                .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());

        mBuilder.setProgress(100, 0, false);
        mBuilder.setDeleteIntent(getDeleteIntent(mShare.getShareUrl()));
        mNotifyManager.notify(nID, mBuilder.build());

        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        final StorageReference httpsReference = storageRef.getReferenceFromUrl(mShare.getShareUrl());

        File parentDirFile = FileHelper.getMuzikoFolder();

        final File localfile = new File(parentDirFile, mShare.getFilename());

        downloadTask = httpsReference.getFile(localfile);

        downloadTask.addOnSuccessListener(taskSnapshot -> {

            FirebaseManager.firebaseShareDownloaderTasks.remove(mShare.getShareUrl());

            mBuilder.setContentText("Download complete");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancelAll();

            MediaScannerConnection.scanFile(mContext,
                    new String[]{localfile.getAbsolutePath()}, null,
                    (path, uri) -> {

                        MediaHelper.Instance().loadMusicFromTrack(localfile.getAbsolutePath(), false);

                        QueueItem queueItem = TrackRealmHelper.getTrack(localfile.getAbsolutePath());

                        if (queueItem != null) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
                            boolean prefArtworkDownload = prefs.getBoolean("prefArtworkDownload", false);
                            if (queueItem != null && prefArtworkDownload) {
                                ArtworkHelper artworkHelper = new ArtworkHelper();
                                artworkHelper.autoPickAlbumArt(mContext, queueItem, true);
                            }
                        }

                        EventBus.getDefault().post(new TrackAddedEvent(queueItem.data));

                        Intent shareintent = new Intent(AppController.INTENT_SHARE_DOWNLOADED);
                        shareintent.putExtra("data", queueItem.data);
                        mContext.sendBroadcast(shareintent);
                    });

            shareRef = FirebaseManager.Instance().getShareRef().child(mShare.getSenderId()).child(mShare.getUid());

            shareListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Share share = dataSnapshot.getValue(Share.class);

                    if (share != null) {

                        if (share.getDownloads() >= share.getShareCount() - 1) {
                            // Delete the file
                            httpsReference.delete().addOnSuccessListener((OnSuccessListener) o -> {

                            }).addOnFailureListener(Crashlytics::logException);
                        }

                        // Update download count for sender share
                        Map<String, Object> senderShareValues = new HashMap<>();
                        senderShareValues.put("downloads", share.getDownloads() + 1);

                        shareRef.updateChildren(
                                senderShareValues,
                                (firebaseError, databaseReference) -> {
                                    if (firebaseError != null) {
                                        AppController.toast(mContext, "Couldn't save user data: " + firebaseError.getMessage());
                                    }
                                });


                        // update user share
                        Map<String, Object> userShareValues = new HashMap<>();
                        userShareValues.put("localfile", localfile.getAbsolutePath());
                        userShareValues.put("downloaded", ServerValue.TIMESTAMP);

                        FirebaseManager.Instance().getShareRef().child(FirebaseManager.Instance().getCurrentUserId()).child(mShare.getUid()).updateChildren(
                                userShareValues,
                                (firebaseError, databaseReference) -> {
                                    if (firebaseError != null) {
                                        AppController.toast(mContext, "Couldn't save user data: " + firebaseError.getMessage());
                                    }
                                });

                        // remove user entry
//							shareRef.child(FirebaseUtil.getCurrentUserId()).child(mShare.getUid()).removeValue();


                        // MARK AS ALLOWED CONTACT

                        DatabaseReference userref = FirebaseManager.Instance().getContactsRef();

                        Contact contact = new Contact(mShare.getSenderId(), false, ServerValue.TIMESTAMP);

                        userref.child(mShare.getSenderId()).setValue(contact, (error, firebase) -> {
                            if (error != null) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    AppController.toast(mContext, "Problem connecting to database");
                }
            };

            shareRef.addListenerForSingleValueEvent(shareListener);
        });

        downloadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            if (progress > 1 && (mProgress + 3 < progress || (mProgress > 90 & mProgress + 1 < progress))) {
                mProgress = (int) progress;
                if (mProgress >= 96) mProgress = 100;
                mBuilder.setProgress(100, mProgress, false);
                mNotifyManager.notify(nID, mBuilder.build());


                Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
                progressIntent.putExtra("url", mShare.getShareUrl());
                progressIntent.putExtra("progress", mProgress);
                mContext.sendBroadcast(progressIntent);
            }
        });

        downloadTask.addOnFailureListener(exception -> {
            mBuilder.setContentText("Download Error");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);

            Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
            progressIntent.putExtra("url", mShare.getShareUrl());
            progressIntent.putExtra("progress", -1);
            mContext.sendBroadcast(progressIntent);

            FirebaseManager.firebaseShareDownloaderTasks.remove(mShare.getShareUrl());
        });

        downloadTask.resume();
    }

    public void cancelDownload() {

        downloadTask.cancel();

        mBuilder.setContentText("Download Cancelled");
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        mNotifyManager.cancel(nID);

    }

    private PendingIntent getDeleteIntent(String shareUrl) {
        Intent intent = new Intent(mContext, NotificationBroadcast.class);
        intent.setAction(AppController.NOTIFY_CANCEL_DOWNLOAD);
        intent.putExtra("shareUrl", shareUrl);
        return PendingIntent.getBroadcast(mContext, 0, intent, nID);
    }

}
