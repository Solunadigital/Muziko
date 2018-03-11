package com.muziko.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.crashlytics.android.Crashlytics;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.services.drive.Drive;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.cloud.ProgressOutputStream;
import com.muziko.common.events.TrackAddedEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FileTransferNotification;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.MD5;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.NotificationController;
import com.muziko.manager.SettingsManager;
import com.muziko.receivers.NotificationBroadcast;
import com.muziko.service.SongService;
import com.onedrive.sdk.extensions.IOneDriveClient;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Created by dev on 27/10/2016.
 */

public class CloudDownloadTask extends AsyncTask<Void, Double, Void> {

    private final WeakHandler handler = new WeakHandler();
    private WeakReference<Context> mContext;
    private DbxDownloader<FileMetadata> dropBoxDownloader;
    private BoxRequestsFile.DownloadFile boxDownloader;
    private Drive driveService;
    private DbxClientV2 dropBoxService;
    private BoxSession boxService;
    private IOneDriveClient oneDriveClient;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private QueueItem queueItem;
    private int nID;
    private File downloadFile;
    private String downloadPath;
    private Drive.Files.Get driveDownloader;
    private boolean running = true;
    private int mode;
    private OutputStream outputStream;
    private int lastProgress;
    private InputStream inputStream;
    private Notification notification;

    public CloudDownloadTask(Context ctx, Drive driveService, QueueItem queueItem, String downloadPath) {
        mContext = new WeakReference<>(ctx);
        this.driveService = driveService;
        this.queueItem = queueItem;
        this.downloadPath = downloadPath;
        mode = CloudManager.GOOGLEDRIVE;
    }

    public CloudDownloadTask(Context ctx, Drive driveService, QueueItem queueItem) {
        mContext = new WeakReference<>(ctx);
        this.driveService = driveService;
        this.queueItem = queueItem;
        mode = CloudManager.GOOGLEDRIVE;
    }

    public CloudDownloadTask(Context ctx, DbxClientV2 dropBoxService, QueueItem queueItem, String downloadPath) {
        mContext = new WeakReference<>(ctx);
        this.dropBoxService = dropBoxService;
        this.queueItem = queueItem;
        this.downloadPath = downloadPath;
        mode = CloudManager.DROPBOX;
    }

    public CloudDownloadTask(Context ctx, DbxClientV2 dropBoxService, QueueItem queueItem) {
        mContext = new WeakReference<>(ctx);
        this.dropBoxService = dropBoxService;
        this.queueItem = queueItem;
        mode = CloudManager.DROPBOX;
    }

    public CloudDownloadTask(Context ctx, BoxSession boxService, QueueItem queueItem, String downloadPath) {
        mContext = new WeakReference<>(ctx);
        this.boxService = boxService;
        this.queueItem = queueItem;
        this.downloadPath = downloadPath;
        mode = CloudManager.BOX;
    }

    public CloudDownloadTask(Context ctx, BoxSession boxService, QueueItem queueItem) {
        mContext = new WeakReference<>(ctx);
        this.boxService = boxService;
        this.queueItem = queueItem;
        mode = CloudManager.BOX;
    }

    public CloudDownloadTask(Context ctx, IOneDriveClient oneDriveClient, QueueItem queueItem, String downloadPath) {
        mContext = new WeakReference<>(ctx);
        this.oneDriveClient = oneDriveClient;
        this.queueItem = queueItem;
        this.downloadPath = downloadPath;
        mode = CloudManager.ONEDRIVE;
    }

    public CloudDownloadTask(Context ctx, IOneDriveClient oneDriveClient, QueueItem queueItem) {
        mContext = new WeakReference<>(ctx);
        this.oneDriveClient = oneDriveClient;
        this.queueItem = queueItem;
        mode = CloudManager.ONEDRIVE;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (downloadPath == null) {
            downloadFile = FileHelper.getDownloadFolder(queueItem.title);
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
                                    downloadFile = FileHelper.getDownloadFolder(mContext.get(), downloadPath, queueItem.title);
                                })
                        .negativeText(R.string.cancel)
                        .onNegative(
                                (dialog, which) -> {
                                    cancelDownload();
                                })
                        .show();
            }
        } else {
            downloadFile = FileHelper.getDownloadFolder(mContext.get(), downloadPath, queueItem.title);
        }

        switch (mode) {
            case CloudManager.GOOGLEDRIVE:

                try {
                    outputStream = FileHelper.getOutputStreamForFile(mContext.get(), downloadFile);
                    driveDownloader = driveService.files().get(queueItem.cloudId);
                    driveDownloader.getMediaHttpDownloader().setProgressListener(downloader -> {
                        switch (downloader.getDownloadState()) {
                            case MEDIA_IN_PROGRESS:
                                if (!running) {
                                    return;
                                }
                                float percent = ((float) downloader.getProgress()) * 100.0F;
                                UpdateProgress((int) percent);
                                break;
                            case MEDIA_COMPLETE:
                                DownloadComplete();
                        }
                    });
                    driveDownloader.getMediaHttpDownloader().setDirectDownloadEnabled(false);
                    driveDownloader.getMediaHttpDownloader().setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
                    driveDownloader.executeMediaAndDownloadTo(outputStream);
                } catch (IOException e) {
                    Crashlytics.logException(e);
                    DownloadError();
                }
                break;

            case CloudManager.DROPBOX:

                try {
                    dropBoxDownloader = dropBoxService.files().download(queueItem.cloudId);
                    long size = dropBoxDownloader.getResult().getSize();
                    outputStream = FileHelper.getOutputStreamForFile(mContext.get(), downloadFile);
                    dropBoxDownloader.download(new ProgressOutputStream(size, outputStream, (completed, totalSize) -> {
                        if (!running) {
                            return;
                        }
                        float percent = ((float) completed / totalSize) * 100.0F;
                        if (percent < 100) {
                            CloudDownloadTask.this.UpdateProgress((int) percent);
                        } else {
                            DownloadComplete();
                        }
                    }));
                } catch (DbxException | IOException e) {
                    Crashlytics.logException(e);
                    DownloadError();
                }
                break;

            case CloudManager.BOX:

                try {
                    BoxApiFile boxApiFile = new BoxApiFile(boxService);
                    boxDownloader = boxApiFile.getDownloadRequest(downloadFile, queueItem.cloudId)
                            // Optional: Set a listener to track download progress.
                            .setProgressListener((numBytes, totalBytes) -> {
                                if (!running) {
                                    return;
                                }
                                float percent = ((float) numBytes / totalBytes) * 100.0F;
                                if (percent < 100) {
                                    UpdateProgress((int) percent);
                                } else {
                                    DownloadComplete();
                                }

                            });
                    boxDownloader.send();
                } catch (BoxException | IOException e) {
                    Crashlytics.logException(e);
                    DownloadError();
                }

                break;

            case CloudManager.ONEDRIVE:

                try {
                    outputStream = FileHelper.getOutputStreamForFile(mContext.get(), downloadFile);
                    inputStream = oneDriveClient.getDrive().getItems(queueItem.cloudId).getContent().buildRequest().get();
                    byte data[] = new byte[1024];
                    long total = 0;
                    int count = 0;
                    while ((count = inputStream.read(data)) != -1) {
                        total += count;
                        // writing data to file
                        outputStream.write(data, 0, count);
                        float percent = ((float) total / queueItem.size) * 100.0F;
                        if (percent < 100) {
                            UpdateProgress((int) percent);
                        } else {
                            DownloadComplete();
                        }
                    }

                    // flushing output
                    outputStream.flush();

                    // closing streams
                    IOUtils.closeQuietly(outputStream);
                    IOUtils.closeQuietly(inputStream);

                } catch (Exception e) {
                    Crashlytics.logException(e);
                    DownloadError();
                }

                break;
        }


        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        int min = 0;
        int max = 1000;

        Random r = new Random();
        nID = r.nextInt(max - min + 1) + min;
        mNotifyManager = (NotificationManager) mContext.get().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext.get(), "Muziko");
        String subTitle = "";
        switch (mode) {
            case CloudManager.GOOGLEDRIVE:
                subTitle = "Google Drive";
                break;

            case CloudManager.DROPBOX:
                subTitle = "DropBox";
                break;
            case CloudManager.BOX:
                subTitle = "Box";
                break;

            case CloudManager.ONEDRIVE:
                subTitle = "OneDrive";
                break;
        }
        RemoteViews simpleContentView = FileTransferNotification.createStandardViewFromCloudTrack(mContext.get(), queueItem, subTitle, 0, false);
        mBuilder.setCustomContentView(simpleContentView);
        mBuilder.setContentTitle(mContext.get().getString(R.string.downloading_files) + " " + queueItem.title)
                .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());
        mBuilder.setDeleteIntent(getDeleteIntent(queueItem.folder_path));
        notification = mBuilder.build();

        mNotifyManager.notify(nID, notification);
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (downloadFile.exists() && downloadFile.length() == 0) {
            downloadFile.delete();
            DownloadError();
        }
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
    }

    private void UpdateProgress(int progress) {
        if (!isCancelled() && (progress - lastProgress > 2 || progress > 90 && running)) {
            lastProgress = progress;

            String subTitle = "";
            switch (mode) {
                case CloudManager.GOOGLEDRIVE:
                    subTitle = "Google Drive";
                    break;

                case CloudManager.DROPBOX:
                    subTitle = "DropBox";
                    break;
                case CloudManager.BOX:
                    subTitle = "Box";
                    break;

                case CloudManager.ONEDRIVE:
                    subTitle = "OneDrive";
                    break;
            }
            RemoteViews simpleContentView = FileTransferNotification.createStandardViewFromCloudTrack(mContext.get(), queueItem, subTitle, progress, false);
            mBuilder.setCustomContentView(simpleContentView);
            mBuilder.setContentTitle(mContext.get().getString(R.string.downloading_files) + " " + queueItem.title)
                    .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());
            mBuilder.setDeleteIntent(getDeleteIntent(queueItem.folder_path));
            notification = mBuilder.build();
            mNotifyManager.notify(nID, notification);

            Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
            progressIntent.putExtra("url", queueItem.data);
            progressIntent.putExtra("progress", progress);
            mContext.get().sendBroadcast(progressIntent);
        }
    }

    private void DownloadComplete() {
        handler.postDelayed(() -> UpdateProgress(101), 1000);
        CloudManager.cloudDownloadList.remove(queueItem.folder_path);

        IOUtils.closeQuietly(outputStream);
        IOUtils.closeQuietly(inputStream);

        mBuilder.setContentText(mContext.get().getString(R.string.download_complete));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        handler.postDelayed(() -> mNotifyManager.cancel(nID), 2000);

        MediaHelper.Instance().loadMusicFromTrack(downloadFile.getAbsolutePath(), false);

        QueueItem newQueueItem = TrackRealmHelper.getTrack(downloadFile.getAbsolutePath());

        EventBus.getDefault().post(new TrackAddedEvent(queueItem.data));

        if (newQueueItem != null) {
            if (newQueueItem.md5 == null) {
                newQueueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
                TrackRealmHelper.updateMD5Hash(newQueueItem);
            }
        }

        Intent shareintent = new Intent(AppController.INTENT_SHARE_DOWNLOADED);
        shareintent.putExtra("data", newQueueItem.data);
        MyApplication.getInstance().sendBroadcast(shareintent);
    }

    private void DownloadError() {

        IOUtils.closeQuietly(outputStream);
        IOUtils.closeQuietly(inputStream);

        CloudManager.cloudDownloadList.remove(queueItem.folder_path);

        Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
        progressIntent.putExtra("url", queueItem.data);
        progressIntent.putExtra("progress", -1);
        mContext.get().sendBroadcast(progressIntent);

        mBuilder.setContentText(mContext.get().getString(R.string.download_error));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());

        mNotifyManager.cancel(nID);
    }


    private PendingIntent getDeleteIntent(String shareUrl) {
        Intent intent = new Intent(mContext.get(), NotificationBroadcast.class);
        intent.setAction(SongService.NOTIFY_CANCEL_CLOUD_DOWNLOAD);
        intent.putExtra("path", shareUrl);
        return PendingIntent.getBroadcast(mContext.get(), 0, intent, nID);
    }

    public void cancelDownload() {
        IOUtils.closeQuietly(outputStream);

        switch (mode) {
            case CloudManager.GOOGLEDRIVE:
                if (driveDownloader != null) {
                    driveDownloader = null;
                }
                break;

            case CloudManager.DROPBOX:
                if (dropBoxDownloader != null) {
                    dropBoxDownloader.close();
                    dropBoxDownloader = null;
                }

                break;
            case CloudManager.BOX:
                if (boxDownloader != null) {
                    boxDownloader = null;
                }
                break;

            case CloudManager.ONEDRIVE:
                if (oneDriveClient != null) {
                    oneDriveClient = null;
                }
                break;
        }

        mBuilder.setContentText(mContext.get().getString(R.string.download_cancelled));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        mNotifyManager.cancel(nID);
        running = false;

    }
}
