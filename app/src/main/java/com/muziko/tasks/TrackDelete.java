package com.muziko.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.cloud.Amazon.AmazonApi;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.MediaStoreHack;
import com.muziko.helpers.QueueHelper;
import com.muziko.helpers.SAFHelpers;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.models.CloudAccount;
import com.muziko.service.MuzikoFirebaseService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TrackDelete extends AsyncTask<Object, int[], Boolean> {

    private final TrackRemoveListener listener;
    private final int type;
    private Context context;
    private int count = 0;
    private BoxApi boxApi;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private OneDriveApi oneDriveApi;
    private AmazonApi amazonApi;

    public TrackDelete(Context context, int type, TrackRemoveListener listener) {
        this.context = context;
        this.type = type;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        List<QueueItem> list = new ArrayList<>();
        boolean deleted = false;

        if (params[0] instanceof QueueItem) {
            QueueItem item = (QueueItem) params[0];
            list.add(item);
        } else if (params[0] instanceof Collection) {
            list.addAll((Collection) params[0]);
        }

        for (QueueItem queue : list) {

            if (queue.storage == 0 || queue.storage == 1 || queue.storage == 2) {

//                if (queue.isLibrary()) {
//                    FirebaseManager.Instance().deleteLibrary(queue);
//                }

                if (queue.isSync()) {
                    FirebaseManager.Instance().deleteFav(queue);
                }

                File file = new File(queue.data);
                deleted = file.delete();

                if (!deleted) {
                    // Try with Storage Access Framework.
//                    && FileHelper.isOnExtSdCard(context, file)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                         DocumentFile targetDocument = null;
                        try {
                            targetDocument = SAFHelpers.getDocumentFile(new File(queue.data), false);
                        } catch (Exception e) {
                            Log.e(">>>>",e.getStackTrace().toString());
                            Crashlytics.logException(e);
                        }

                        if (targetDocument != null) {
                            deleted = targetDocument.delete();
                        }

                        try {
                            if (file.exists()) {
                                file.setWritable(true, false);
                                String where = MediaStore.Audio.Media.DATA + "=\"" + queue.data + "\"";
                                if (context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null) == 1) {
                                    if (file.exists()) {
                                        deleted = file.delete();
                                    }

                                }
                            }
                        }catch (Exception e){
                            Log.e(">>>>",e.getStackTrace().toString());
                        }
                    }

                    // Try the Kitkat workaround.
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                        ContentResolver resolver = context.getContentResolver();
                        try {
                            Uri uri = MediaStoreHack.getUriFromFile(file.getAbsolutePath(), context);
                            resolver.delete(uri, null, null);
                            deleted = !file.exists();
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                        }
                    }
                }

                if (deleted) {
                    final String where = MediaStore.MediaColumns.DATA + "=?";
                    final String[] selectionArgs = new String[]{file.getAbsolutePath()};
                    final ContentResolver contentResolver = context.getContentResolver();
                    final Uri filesUri = MediaStore.Files.getContentUri("external");
                    // Delete the entry from the media database. This will actually delete media files.
                    contentResolver.delete(filesUri, where, selectionArgs);
                }

            } else if (queue.storage == CloudManager.FIREBASE) {
                if (queue.isLibrary()) {
                    LibraryEdit libraryEdit = new LibraryEdit(context, true, s -> {

                    });
                    libraryEdit.execute(queue);
                } else {
                    FirebaseManager.Instance().deleteFav(queue);
                    deleted = true;
                }
            } else {
                CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(queue.storage);
                switch (cloudAccount.getCloudProvider()) {
                    case (CloudManager.GOOGLEDRIVE):
                        driveApi = (DriveApi) CloudManager.Instance().getCloudDrive(context, cloudAccount.getCloudAccountId());
                        deleted = driveApi.deleteFile(queue.folder_path);

                        break;

                    case (CloudManager.DROPBOX):
                        dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(context, cloudAccount.getCloudAccountId());
                        deleted = dropBoxApi.deleteFile(queue.folder_path);

                        break;

                    case (CloudManager.BOX):
                        boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(context, cloudAccount.getCloudAccountId());
                        deleted = boxApi.deleteFile(queue.folder_path);
                        break;

                    case (CloudManager.ONEDRIVE):
                        oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(context, cloudAccount.getCloudAccountId());
                        deleted = oneDriveApi.deleteFile(queue.folder_path);
                        break;

                    case (CloudManager.AMAZON):
                        amazonApi = (AmazonApi) CloudManager.Instance().getCloudDrive(context, cloudAccount.getCloudAccountId());
                        deleted = amazonApi.deleteFile(queue.folder_path);
                        break;
                }
            }

            if (deleted) {
                //remove refs from app

                if (PlayerConstants.QUEUE_SONG.data.equals(queue.data)) {
                    PlayerConstants.QUEUE_SONG = new QueueItem();
                    AppController.Instance().serviceNext();
                }

                PlaylistSongRealmHelper.deleteByData(queue.data);
                PlaylistRealmHelper.removeOneFromPlaylist(queue);
                TrackRealmHelper.deleteTrack(queue.data);
                AppController.Instance().serviceDelete(type, queue.hash, queue.data);
                count++;
            }
        }

        QueueHelper.saveQueue(context);

        return deleted;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
        if (s) {
            AppController.toast(context, String.format("Deleted %d song%s", count, count != 1 ? "s" : ""));
            EventBus.getDefault().post(new RefreshEvent(1000));
            if (FirebaseManager.Instance().isFirebaseStarted()) {
                Intent intent = new Intent(context, MuzikoFirebaseService.class);
                intent.setAction(AppController.ACTION_REFRESH_LIBRARY);
                context.startService(intent);
            }
            if (listener != null) {
                listener.onTrackRemoved();
            }
        } else {
            AppController.toast(context, "Unable to delete file");
        }


    }

    public interface TrackRemoveListener {
        void onTrackRemoved();
    }
}
