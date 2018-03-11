package com.muziko.cloud;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.muziko.manager.NotificationController;

import java.util.Random;

/** Created by Bradley on 14/05/2017. */
public class GenericCloudApi {

    public boolean showingProgress;
    protected boolean mConnected;
    protected String accountName;
    protected int cloudAccountId;
    protected Context context;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;

    public void showProgress(Context context, String message) {
        this.context = context;
        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context, "Muziko");
        mBuilder.setContentTitle(message)
                .setContentText("Please wait")
                .setSmallIcon(NotificationController.Instance().getSyncNotificationIcon());

        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(nID, mBuilder.build());
    }

    public void cancelProgress() {
        mBuilder.setContentText("Sync complete");
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        mNotifyManager.cancelAll();
    }
}
