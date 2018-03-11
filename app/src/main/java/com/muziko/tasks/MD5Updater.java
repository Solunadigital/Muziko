package com.muziko.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.MD5;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.NotificationController;
import com.muziko.receivers.NotificationBroadcast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class MD5Updater extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = MD5Updater.class.getSimpleName();
    private final Context mContext;
    private final ArrayList<QueueItem> md5MissingList = new ArrayList<>();
    private final ArrayList<QueueItem> updateList = new ArrayList<>();
    private File file;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;
    private int total = 0;
    private int count = 0;
    private int mProgress = 0;

    public MD5Updater(Context ctx) {
        mContext = ctx;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {

            if (total > 0) {
                Log.d(TAG, "Getting MD5 hashes for " + md5MissingList.size() + " tracks");
                for (final QueueItem queueItem : md5MissingList) {
                    file = new File(queueItem.data);
                    queueItem.md5 = MD5.calculateMD5(file);
                    updateList.add(queueItem);
                    Log.d(TAG, queueItem.data + " - MD5: " + queueItem.md5);
                    count++;
                    if (count < total) {
                        double progress = (100.0 * count / total);
                        mProgress = (int) progress;
                        mBuilder.setContentText(String.format(mContext.getString(R.string.md5_progress), count, total));
                        mBuilder.setProgress(100, mProgress, false);
                        mNotifyManager.notify(nID, mBuilder.build());
                    } else {
                        mBuilder.setContentText(mContext.getString(R.string.md5_complete));
                        mBuilder.setProgress(0, 0, false);
                        mNotifyManager.notify(nID, mBuilder.build());
                        mNotifyManager.cancel(nID);
                    }
                }

                TrackRealmHelper.updateMD5Hashs(updateList);
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
            AppController.Instance().cancelMd5Updater();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        md5MissingList.clear();
        md5MissingList.addAll(TrackRealmHelper.getTracksWithoutMD5());
        total = md5MissingList.size();

        if (total > 0) {
            int min = 0;
            int max = 1000;

            Random r = new Random();
            nID = r.nextInt(max - min + 1) + min;

            mNotifyManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
            mBuilder.setContentTitle(mContext.getString(R.string.md5_title))
                    .setContentText(mContext.getString(R.string.md5_desc))
                    .setSmallIcon(NotificationController.Instance().getSyncNotificationIcon());

            mBuilder.setProgress(100, 0, false);
            mBuilder.setDeleteIntent(getDeleteIntent());
            mNotifyManager.notify(nID, mBuilder.build());
        }

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (total > 0) {
            Log.d(TAG, count + " MD5 Hashes Updated");
            mBuilder.setContentText(mContext.getString(R.string.md5_complete));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(nID, mBuilder.build());
            mNotifyManager.cancel(nID);
            AppController.Instance().cancelMd5Updater();
            FirebaseManager.Instance().checkforTransfers();
        }
    }

    private PendingIntent getDeleteIntent() {
        Intent intent = new Intent(mContext, NotificationBroadcast.class);
        intent.setAction(MyApplication.NOTIFY_CANCEL_HASH);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
