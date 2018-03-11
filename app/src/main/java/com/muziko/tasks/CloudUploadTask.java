package com.muziko.tasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.badoo.mobile.util.WeakHandler;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.crashlytics.android.Crashlytics;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ParentReference;
import com.muziko.R;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.cloud.ProgressInputStream;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.NotificationController;
import com.muziko.models.CloudAccount;
import com.muziko.receivers.NotificationBroadcast;
import com.muziko.service.SongService;
import com.onedrive.sdk.concurrency.IProgressCallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.Item;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static com.muziko.cloud.Drive.DriveApiHelpers.MIME_AUDIO;
import static com.muziko.manager.MuzikoConstants.UPLOAD_SETTLE_DELAY;

/**
 * Created by dev on 27/10/2016.
 */
public class CloudUploadTask extends AsyncTask<Void, Double, Void>
        implements ProgressInputStream.OnProgressListener {

    private final Context mContext;
    private final WeakHandler handler = new WeakHandler();
    private Drive driveService;
    private DbxClientV2 dropBoxService;
    private BoxSession boxService;
    private IOneDriveClient oneDriveClient;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private QueueItem queueItem;
    private QueueItem parentQueueItem;
    private int nID;
    private File uploadFile;
    private boolean running = true;
    private int mode;
    private FileInputStream inputStream;
    private int lastProgress;
    private BoxRequestsFile.UploadFile boxUploader;
    private UploadBuilder dropBoxUploader;
    private Drive.Files.Insert driveUploader;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private BoxApi boxApi;
    private OneDriveApi oneDriveApi;

    public CloudUploadTask(Context ctx, Drive driveService, QueueItem queueItem, QueueItem parentQueueItem) {
        mContext = ctx;
        this.driveService = driveService;
        this.queueItem = queueItem;
        this.parentQueueItem = parentQueueItem;
        mode = CloudManager.GOOGLEDRIVE;
    }

    public CloudUploadTask(Context ctx, DbxClientV2 dropBoxService, QueueItem queueItem, QueueItem parentQueueItem) {
        mContext = ctx;
        this.dropBoxService = dropBoxService;
        this.queueItem = queueItem;
        this.parentQueueItem = parentQueueItem;
        mode = CloudManager.DROPBOX;
    }

    public CloudUploadTask(Context ctx, BoxSession boxService, QueueItem queueItem, QueueItem parentQueueItem) {
        mContext = ctx;
        this.boxService = boxService;
        this.queueItem = queueItem;
        this.parentQueueItem = parentQueueItem;
        mode = CloudManager.BOX;
    }

    public CloudUploadTask(Context ctx, IOneDriveClient oneDriveClient, QueueItem queueItem, QueueItem parentQueueItem) {
        mContext = ctx;
        this.oneDriveClient = oneDriveClient;
        this.queueItem = queueItem;
        this.parentQueueItem = parentQueueItem;
        mode = CloudManager.ONEDRIVE;
    }

    @Override
    protected Void doInBackground(Void... params) {

        uploadFile = new File(queueItem.data);

        switch (mode) {
            case CloudManager.GOOGLEDRIVE:
                if (parentQueueItem.folder_path.equalsIgnoreCase("Cloud"))
                    parentQueueItem.folder_path = "root";
                try {
                    com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
                    body.setTitle(uploadFile.getName());
                    body.setMimeType(MIME_AUDIO);
                    body.setParents(Arrays.asList(new ParentReference().setId(parentQueueItem.folder_path)));

                    // File's content.
                    java.io.File fileContent = new java.io.File(queueItem.data);
                    FileContent mediaContent = new FileContent(MIME_AUDIO, fileContent);
                    driveUploader = driveService.files().insert(body, mediaContent);
                    driveUploader.getMediaHttpUploader().setProgressListener(uploader -> {
                        switch (uploader.getUploadState()) {
                            case MEDIA_IN_PROGRESS:
                                if (!running) {
                                    return;
                                }
                                float percent =
                                        ((float) uploader.getProgress()) * 100.0F;
                                UpdateProgress((int) percent);
                                break;
                            case MEDIA_COMPLETE:
                                uploadComplete(uploadFile.getName());
                        }
                    });
                    driveUploader.getMediaHttpUploader().setDirectUploadEnabled(false);
                    driveUploader
                            .getMediaHttpUploader()
                            .setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
                    driveUploader.execute();

                } catch (FileNotFoundException e) {
                    Crashlytics.logException(e);
                    UploadError();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                    UploadError();
                }
                break;

            case CloudManager.DROPBOX:
                if (parentQueueItem.folder_path.equalsIgnoreCase("Cloud"))
                    parentQueueItem.folder_path = "";
                try {
                    dropBoxUploader = dropBoxService.files().uploadBuilder(parentQueueItem.folder_path + "/" + uploadFile.getName());
                    long size = uploadFile.length();
                    inputStream = new FileInputStream(uploadFile);
                    dropBoxUploader.withMode(WriteMode.OVERWRITE);
                    dropBoxUploader.uploadAndFinish(new ProgressInputStream(inputStream, size, queueItem, (percentage, tag) -> {
                        if (!running) {
                            return;
                        }
                        if (percentage < 100) {
                            UpdateProgress(percentage);
                        } else {
                            handler.postDelayed(
                                    () -> {
                                        uploadComplete(uploadFile.getName());
                                    },
                                    UPLOAD_SETTLE_DELAY);
                        }
                    }));
                } catch (DbxException e) {
                    Crashlytics.logException(e);
                } catch (FileNotFoundException e) {
                    Crashlytics.logException(e);
                } catch (IOException e) {
                    Crashlytics.logException(e);
                }
                break;

            case CloudManager.BOX:
                if (parentQueueItem.folder_path.equalsIgnoreCase("Cloud"))
                    parentQueueItem.folder_path = "0";
                try {
                    BoxApiFile boxApiFile = new BoxApiFile(boxService);
                    boxUploader = boxApiFile.getUploadRequest(uploadFile, parentQueueItem.folder_path).setProgressListener((numBytes, totalBytes) -> {
                        if (!running) {
                            return;
                        }
                        float percent = ((float) numBytes / totalBytes) * 100.0F;
                        if (percent < 100) {
                            UpdateProgress((int) percent);
                        } else {
                            handler.postDelayed(() -> {
                                        uploadComplete(uploadFile.getName());
                                    },
                                    UPLOAD_SETTLE_DELAY);
                        }
                    });
                    boxUploader.send();

                } catch (BoxException e) {
                    Crashlytics.logException(e);
                }

                break;

            case CloudManager.ONEDRIVE:
                try {
                    inputStream = new FileInputStream(uploadFile);
                    oneDriveClient.getDrive().getItems(parentQueueItem.folder_path).getChildren().byId(uploadFile.getName()).getContent().buildRequest().put(IOUtils.toByteArray(inputStream), new IProgressCallback<Item>() {
                        @Override
                        public void progress(long current, long max) {
                            if (!running) {
                                return;
                            }
                            float percent = ((float) current / max) * 100.0F;
                            UpdateProgress((int) percent);
                        }

                        @Override
                        public void success(Item item) {
                            handler.postDelayed(() -> {
                                        uploadComplete(uploadFile.getName());
                                    },
                                    UPLOAD_SETTLE_DELAY);
                        }

                        @Override
                        public void failure(ClientException ex) {
                            Crashlytics.logException(ex);
                        }
                    });
                } catch (Exception e) {
                    Crashlytics.logException(e);
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

        mNotifyManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "Muziko");
        mBuilder.setContentTitle(mContext.getString(R.string.uploading_file))
                .setContentText(mContext.getString(R.string.upload_progress))
                .setSmallIcon(NotificationController.Instance().getUploadNotificationIcon());
        mBuilder.setProgress(100, 0, false);
        mBuilder.setProgress(100, 0, false);
        mBuilder.setDeleteIntent(getDeleteIntent(queueItem.folder_path));

        mNotifyManager.notify(nID, mBuilder.build());
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        MediaHelper.Instance().loadMusicFromTrackAsync(
                uploadFile.getAbsolutePath(), false);
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
    }

    private void UpdateProgress(int progress) {
        if (!isCancelled() && (progress - lastProgress > 2 || progress > 90 && running)) {
            lastProgress = progress;
            mBuilder.setProgress(100, progress, false);
            mNotifyManager.notify(nID, mBuilder.build());
        }
    }

    private void uploadComplete(String path) {
        handler.postDelayed(() -> UpdateProgress(101), 1000);
        CloudManager.cloudUploaderList.remove(queueItem.folder_path);
        IOUtils.closeQuietly(inputStream);

        CloudAccount cloudAccount =
                CloudAccountRealmHelper.getCloudAccount(parentQueueItem.storage);
        switch (cloudAccount.getCloudProvider()) {
            case (CloudManager.GOOGLEDRIVE):
                driveApi = (DriveApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                driveApi.search(path);
                break;

            case (CloudManager.DROPBOX):
                dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                dropBoxApi.search(path);
                break;

            case (CloudManager.BOX):
                boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                boxApi.search("mp3");
                break;

            case (CloudManager.ONEDRIVE):
                oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                //  oneDriveApi.recursiveSearch("audio");
                oneDriveApi.searchRecursive();
                break;
        }

        mBuilder.setContentText(mContext.getString(R.string.upload_complete));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        handler.postDelayed(() -> mNotifyManager.cancel(nID), 2000);
    }

    private void UploadError() {

        IOUtils.closeQuietly(inputStream);

        FirebaseManager.firebaseShareUploaderTasks.remove(queueItem.folder_path);

        mBuilder.setContentText(mContext.getString(R.string.upload_error));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());

        mNotifyManager.cancel(nID);
    }

    private PendingIntent getDeleteIntent(String shareUrl) {
        Intent intent = new Intent(mContext, NotificationBroadcast.class);
        intent.setAction(SongService.NOTIFY_CANCEL_CLOUD_UPLOAD);
        intent.putExtra("path", shareUrl);
        return PendingIntent.getBroadcast(mContext, 0, intent, nID);
    }

    public void cancelUpload() {
        IOUtils.closeQuietly(inputStream);

        switch (mode) {
            case CloudManager.GOOGLEDRIVE:
                driveUploader = null;
                break;

            case CloudManager.DROPBOX:
                dropBoxUploader = null;
                break;
            case CloudManager.BOX:
                boxUploader = null;
                break;
            case CloudManager.ONEDRIVE:
                oneDriveClient = null;
                break;
        }

        mBuilder.setContentText(mContext.getString(R.string.upload_cancelled));
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(nID, mBuilder.build());
        mNotifyManager.cancel(nID);
        running = false;
    }

    @Override
    public void onProgress(int percentage, Object tag) {
    }
}
