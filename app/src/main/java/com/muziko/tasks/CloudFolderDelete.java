package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.helpers.QueueHelper;
import com.muziko.manager.CloudManager;
import com.muziko.models.CloudAccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CloudFolderDelete extends AsyncTask<Object, int[], Boolean> {

    private final CloudFolderDeleteListener listener;
    private final Context mContext;
    private BoxApi boxApi;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private OneDriveApi oneDriveApi;
    private int count = 0;


    public CloudFolderDelete(Context mContext, CloudFolderDeleteListener listener) {
        this.mContext = mContext;
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

            CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(queue.storage);
            switch (cloudAccount.getCloudProvider()) {
                case (CloudManager.GOOGLEDRIVE):
                    driveApi = (DriveApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    deleted = driveApi.deleteFile(queue.folder_path);
                    break;

                case (CloudManager.DROPBOX):
                    dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    deleted = dropBoxApi.deleteFile(queue.folder_path);
                    break;

                case (CloudManager.BOX):
                    boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    deleted = boxApi.deleteFolder(queue.folder_path);
                    break;

                case (CloudManager.ONEDRIVE):
                    oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    deleted = oneDriveApi.deleteFile(queue.folder_path);
                    break;
            }

            if (deleted) {
                //remove refs from app

//                if (PlayerConstants.QUEUE_SONG.data.equals(queue.data)) {
//                    PlayerConstants.QUEUE_SONG = new QueueItem();
//                    MyApplication.serviceNext(mContext);
//                }
//
//                PlaylistSongRealmHelper.deleteByData(queue.data);
//                PlaylistRealmHelper.removeOneFromPlaylist(queue);
//                TrackRealmHelper.deleteTrack(queue.data);
//                MyApplication.serviceDelete(mContext, type, queue.hash, queue.data);

                count++;
            }

        }

        QueueHelper.saveQueue(mContext);

        return deleted;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (listener != null) {
            listener.onFolderDeleted();
        }

        super.onPostExecute(result);
    }

    public interface CloudFolderDeleteListener {
        void onFolderDeleted();
    }
}
