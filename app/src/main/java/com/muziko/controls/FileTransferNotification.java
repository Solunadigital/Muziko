package com.muziko.controls;

import android.content.Context;
import android.widget.RemoteViews;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;

/**
 * Created by Bradley on 21/12/2017.
 */

public class FileTransferNotification {


    public static RemoteViews createStandardViewFromCloudTrack(Context context, QueueItem queueItem, String subTitle, int progress, boolean isUpload) {

        RemoteViews simpleContentView = new RemoteViews(context.getPackageName(), R.layout.notification_download);
        simpleContentView.setImageViewResource(R.id.icon, isUpload ? R.drawable.ic_cloud_upload_black_24dp : R.drawable.ic_cloud_download_black_24dp);
        simpleContentView.setTextViewText(R.id.titleText, subTitle);
        simpleContentView.setTextViewText(R.id.contextText, queueItem.title);
        simpleContentView.setTextViewText(R.id.footerText, isUpload ? context.getString(R.string.upload_progress) : context.getString(R.string.download_progress));
        simpleContentView.setProgressBar(R.id.progressBar, 100, progress, false);
        return simpleContentView;
    }

    public static RemoteViews createStandardViewFromCloudTrack(Context context, CloudTrack cloudTrack, String subTitle, int progress, boolean isUpload) {

        RemoteViews simpleContentView = new RemoteViews(context.getPackageName(), R.layout.notification_download);
        simpleContentView.setImageViewResource(R.id.icon, isUpload ? R.drawable.ic_cloud_upload_black_24dp : R.drawable.ic_cloud_download_black_24dp);
        simpleContentView.setTextViewText(R.id.titleText, subTitle);
        simpleContentView.setTextViewText(R.id.contextText, cloudTrack.getTitle());
        simpleContentView.setTextViewText(R.id.footerText, isUpload ? context.getString(R.string.upload_progress) : context.getString(R.string.download_progress));
        simpleContentView.setProgressBar(R.id.progressBar, 100, progress, false);
        return simpleContentView;
    }

}
