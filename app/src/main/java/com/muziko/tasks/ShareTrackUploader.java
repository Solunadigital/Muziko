package com.muziko.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.badoo.mobile.util.WeakHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Person;
import com.muziko.common.models.firebase.Share;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.NotificationController;
import com.muziko.receivers.NotificationBroadcast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * Created by dev on 27/10/2016.
 */

public class ShareTrackUploader {

    private final Context mContext;
    private final WeakHandler handler = new WeakHandler();
    private File mFile;
    private Uri mUri;
    private int mProgress = 0;
    private ArrayList<Person> mPersonList;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private QueueItem queueItem;
    private int nID;
    private UploadTask uploadTask;

    public ShareTrackUploader(Context ctx, QueueItem queueItem, ArrayList<Person> personList) {
        mContext = ctx;
        this.queueItem = queueItem;
        mPersonList = personList;
    }

    public void startUpload() {

        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager)
                        mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
        mBuilder.setContentTitle("Sharing file")
                .setContentText("Upload in progress - Swipe to cancel")
                .setSmallIcon(NotificationController.Instance().getUploadNotificationIcon());

        mBuilder.setProgress(100, 0, false);
        mBuilder.setDeleteIntent(getDeleteIntent(queueItem.data));
        mNotifyManager.notify(nID, mBuilder.build());

//		final QueueItem queueItem = TrackRealmHelper.getTrack(mqueueItem.data);
        mFile = new File(queueItem.data);
        mUri = Uri.parse(queueItem.data);
        final String uuid = UUID.randomUUID().toString();

        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference fileRef = storageRef.getReferenceFromUrl("gs://" + mContext.getString(R.string.google_storage_bucket));

        final String filename = mFile.getName();

        final StorageReference fullSizeRef = fileRef.child("shares").child(uuid).child(mUri.getLastPathSegment());


        byte bytes[] = new byte[0];
        try {
            bytes = FileUtils.readFileToByteArray(mFile);
        } catch (IOException e) {

            e.printStackTrace();
        }

        uploadTask = fullSizeRef.putBytes(bytes);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            MyApplication.pauseDeletingTempRingtone = false;
            mBuilder.setContentText("Upload complete");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancelAll();

            final Uri shareUrl = taskSnapshot.getDownloadUrl();
            DatabaseReference shareref = FirebaseManager.Instance().getShareRef();

            for (Person person : mPersonList) {
                Share share = new Share(uuid, FirebaseManager.Instance().getCurrentUserId(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), person.getUid(), shareUrl.toString(), filename, queueItem.title, queueItem.artist_name, queueItem.album_name, ServerValue.TIMESTAMP, mPersonList.size(), null);
                shareref.child(person.getUid()).child(uuid).setValue(share, (error, firebase) -> {
                    if (error != null) {
                        AppController.toast(mContext, "Network connection failed");
                    }
                });
            }

            Share creatorShare = new Share(uuid, FirebaseManager.Instance().getCurrentUserId(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), uuid, shareUrl.toString(), filename, queueItem.title, queueItem.artist_name, queueItem.album_name, ServerValue.TIMESTAMP, mPersonList.size(), mPersonList);
            shareref.child(FirebaseManager.Instance().getCurrentUserId()).child(uuid).setValue(creatorShare, (error, firebase) -> {
                if (error != null) {
                    AppController.toast(mContext, "Network connection failed");
                }
            });

            AppController.toast(mContext, "Song shared successfully");
        });

        uploadTask.addOnFailureListener(e -> {
            MyApplication.pauseDeletingTempRingtone = false;
            mBuilder.setContentText("Upload Error");
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);
        });

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            if (progress > 1 && (mProgress + 3 < progress || (mProgress > 90 & mProgress + 1 < progress))) {
                mProgress = (int) progress;
                int currentprogress = (int) progress;
                mBuilder.setProgress(100, currentprogress, false);
                mNotifyManager.notify(nID, mBuilder.build());
            }
        });

        uploadTask.addOnPausedListener(taskSnapshot -> System.out.println("Upload is paused"));

        uploadTask.resume();
    }

    public void cancelUpload() {

        uploadTask.cancel();

        mBuilder.setContentText("Upload Cancelled");
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        mNotifyManager.cancel(nID);

    }

    private PendingIntent getDeleteIntent(String data) {
        Intent intent = new Intent(mContext, NotificationBroadcast.class);
        intent.setAction(AppController.NOTIFY_CANCEL_UPLOAD);
        intent.putExtra("data", data);
        return PendingIntent.getBroadcast(mContext, 0, intent, nID);
    }
}
