package com.muziko.cloud;

import android.content.ContentValues;

import com.box.androidsdk.content.models.BoxItem;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.google.api.services.drive.model.File;
import com.muziko.R;
import com.muziko.cloud.Drive.DriveApiHelpers;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.CloudManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.models.CloudAccount;
import com.onedrive.sdk.extensions.Item;

import hugo.weaving.DebugLog;

/**
 * Created by Bradley on 8/05/2017.
 */

public class CloudHelper {

    @DebugLog
    public static QueueItem getCloudItem(CloudAccount cloudAccount) {

        QueueItem clouditem = new QueueItem();
        clouditem.id = cloudAccount.getCloudAccountId();
        clouditem.title = cloudAccount.getName().isEmpty() ? cloudAccount.getAccountName() : cloudAccount.getName();
        clouditem.date = 0L;
        clouditem.artist_name = cloudAccount.getAccountName();
        if (cloudAccount.getCloudProvider() == CloudManager.GOOGLEDRIVE) {
            clouditem.album = CloudManager.GOOGLEDRIVE;
            clouditem.data = "root";
        } else if (cloudAccount.getCloudProvider() == CloudManager.DROPBOX) {
            clouditem.album = CloudManager.DROPBOX;
            clouditem.data = "";
        } else if (cloudAccount.getCloudProvider() == CloudManager.BOX) {
            clouditem.album = CloudManager.BOX;
            clouditem.data = "0";
        } else if (cloudAccount.getCloudProvider() == CloudManager.ONEDRIVE) {
            clouditem.album = CloudManager.ONEDRIVE;
            clouditem.data = "root";
        } else if (cloudAccount.getCloudProvider() == CloudManager.AMAZON) {
            clouditem.album = CloudManager.AMAZON;
            clouditem.data = "root";
        }
        clouditem.folder_path = "cloud";
        clouditem.songs = 0;
        clouditem.folder = true;
        clouditem.storage = cloudAccount.getCloudAccountId();
        clouditem.order = R.id.drives;

        return clouditem;
    }

    @DebugLog
    public static QueueItem getDrivesItem() {

        QueueItem drivesItem = new QueueItem();
        drivesItem.id = R.id.drives;
        drivesItem.album = 0;
        drivesItem.title = "Drives";
        drivesItem.date = 0L;
        drivesItem.artist_name = "";
        drivesItem.data = "Drives";
        drivesItem.songs = 0;
        drivesItem.folder = true;

        return drivesItem;
    }

    @DebugLog
    public static QueueItem getMainRootItem(boolean withSlash) {

        QueueItem rootItem = new QueueItem();
        rootItem.id = R.id.root;
        rootItem.album = CloudManager.LOCAL;
        rootItem.title = withSlash ? "root > " : "root";
        rootItem.date = 0L;
        rootItem.artist_name = "";
        rootItem.data = withSlash ? "root > " : "root";
        rootItem.songs = 0;
        rootItem.folder = true;
        rootItem.order = R.id.drives;

        return rootItem;
    }

    @DebugLog
    public static QueueItem getFileItem(int cloudAccountId, String accountName, QueueItem queueItem, BoxItem boxItem) {
        QueueItem fileModel = new QueueItem();
        fileModel.id = Utils.randLong();
        fileModel.album = CloudManager.BOX;
        fileModel.artist_name = accountName;
        fileModel.title = boxItem.getName();
        fileModel.date = 0L;
        fileModel.data = "box://" + boxItem.getId();
        fileModel.folder_path = boxItem.getId();
        fileModel.album = CloudManager.BOX;
        fileModel.songs = 0;
        fileModel.folder = false;
        fileModel.storage = cloudAccountId;
        fileModel.order = queueItem.id;
        fileModel.size = boxItem.getSize() == null ? 0 : boxItem.getSize();
        fileModel.date = boxItem.getSize() == null ? System.currentTimeMillis() : boxItem.getCreatedAt().getTime();
        fileModel.dateModified = System.currentTimeMillis();
        return fileModel;

    }

    @DebugLog
    public static QueueItem getFolderItem(int cloudAccountId, String accountName, QueueItem queueItem, BoxItem boxItem) {
        QueueItem folderModel = new QueueItem();
        folderModel.id = Utils.randLong();
        folderModel.album = CloudManager.BOX;
        folderModel.artist_name = accountName;
        folderModel.title = boxItem.getName();
        folderModel.date = 0L;
        folderModel.data = boxItem.getId();
        folderModel.folder_path = boxItem.getId();
        folderModel.songs = 0;
        folderModel.folder = true;
        folderModel.storage = cloudAccountId;
        folderModel.order = queueItem.id;
        return folderModel;
    }

    @DebugLog
    public static QueueItem getFolderItem(int cloudAccountId, String accountName, QueueItem queueItem, ContentValues contentValues) {
        QueueItem folderModel = new QueueItem();
        folderModel.id = Utils.randLong();
        folderModel.album = CloudManager.GOOGLEDRIVE;
        folderModel.artist_name = accountName;
        folderModel.title = contentValues.getAsString(DriveApiHelpers.TITL);
        folderModel.date = 0L;
        folderModel.data = contentValues.getAsString(DriveApiHelpers.GDID);
        folderModel.folder_path = contentValues.getAsString(DriveApiHelpers.GDID);
        folderModel.songs = 0;
        folderModel.folder = true;
        folderModel.storage = cloudAccountId;
        folderModel.order = queueItem.id;
        return folderModel;
    }

    @DebugLog
    public static QueueItem getFolderItem(int cloudAccountId, String accountName, QueueItem queueItem, File file) {
        QueueItem folderModel = new QueueItem();
        folderModel.id = Utils.randLong();
        folderModel.album = CloudManager.GOOGLEDRIVE;
        folderModel.artist_name = accountName;
        folderModel.title = file.getTitle();
        folderModel.date = 0L;
        folderModel.data = file.getId();
        folderModel.folder_path = file.getId();
        folderModel.songs = 0;
        folderModel.folder = true;
        folderModel.storage = cloudAccountId;
        folderModel.order = queueItem.id;
        return folderModel;
    }

    @DebugLog
    public static QueueItem getFileItem(int cloudAccountId, String accountName, QueueItem queueItem, FileMetadata fileMetadata) {
        QueueItem fileModel = new QueueItem();
        fileModel.id = Utils.randLong();
        fileModel.album = CloudManager.DROPBOX;
        fileModel.artist_name = accountName;
        fileModel.title = fileMetadata.getName();
        fileModel.date = 0L;
        fileModel.data = fileMetadata.getPathLower();
        fileModel.folder_path = fileMetadata.getPathLower();
        fileModel.songs = 0;
        fileModel.folder = false;
        fileModel.storage = cloudAccountId;
        fileModel.order = queueItem.id;
        fileModel.size = fileMetadata.getSize();
        fileModel.date = fileMetadata.getClientModified().getTime();
        fileModel.dateModified = System.currentTimeMillis();
        return fileModel;

    }

    @DebugLog
    public static QueueItem getFolderItem(int cloudAccountId, String accountName, QueueItem queueItem, FolderMetadata folderMetadata) {
        QueueItem folderModel = new QueueItem();
        folderModel.id = Utils.randLong();
        folderModel.album = CloudManager.DROPBOX;
        folderModel.artist_name = accountName;
        folderModel.title = folderMetadata.getName();
        folderModel.date = 0L;
        folderModel.data = folderMetadata.getPathDisplay();
        folderModel.folder_path = folderMetadata.getPathLower();
        folderModel.songs = 0;
        folderModel.folder = true;
        folderModel.storage = cloudAccountId;
        folderModel.order = queueItem.id;
        return folderModel;
    }

    @DebugLog
    public static QueueItem getFileItem(int cloudAccountId, String accountName, QueueItem queueItem, Item item) {
        QueueItem fileModel = new QueueItem();
        fileModel.id = Utils.randLong();
        fileModel.album = CloudManager.ONEDRIVE;
        fileModel.artist_name = accountName;
        fileModel.album_name = item.audio.album;
        fileModel.genre_name = item.audio.genre;
        fileModel.track = item.audio.track != null ? item.audio.track : 0;
        fileModel.year = item.audio.year != null ? item.audio.year : 0;
        if (item.audio.title == null || item.audio.title.isEmpty()) {
            fileModel.title = MuzikoConstants.UNKNOWN_TITLE;
        } else {
            fileModel.title = item.audio.title;
        }
        fileModel.date = 0L;
        fileModel.data = item.id;
        fileModel.folder_path = item.id;
        fileModel.songs = 0;
        fileModel.folder = false;
        fileModel.storage = cloudAccountId;
        fileModel.order = queueItem.id;
        fileModel.size = item.size;
        fileModel.date = item.createdDateTime.getTimeInMillis();
        fileModel.dateModified = item.lastModifiedDateTime.getTimeInMillis();
        return fileModel;
    }

    public static QueueItem getFolderItem(int cloudAccountId, String accountName, QueueItem queueItem, Item item) {
        QueueItem folderModel = new QueueItem();
        folderModel.id = Utils.randLong();
        folderModel.album = CloudManager.ONEDRIVE;
        folderModel.artist_name = accountName;
        folderModel.title = item.name;
        folderModel.date = 0L;
        folderModel.data = item.id;
        folderModel.folder_path = item.id;
        folderModel.songs = 0;
        folderModel.folder = true;
        folderModel.storage = cloudAccountId;
        folderModel.order = queueItem.id;
        return folderModel;
    }
}
