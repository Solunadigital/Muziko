package com.muziko.cloud.Drive;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.muziko.R;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.CloudHelper;
import com.muziko.cloud.GenericCloudApi;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.ThreadManager;
import com.oasisfeng.condom.CondomContext;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import hugo.weaving.DebugLog;

import static com.muziko.cloud.Drive.DriveApiHelpers.MIME_AUDIO;
import static com.muziko.cloud.Drive.DriveApiHelpers.MIME_MPEG;
import static com.muziko.cloud.Drive.DriveApiHelpers.MIME_XMPEG;
import static com.muziko.manager.CloudManager.connectedCloudDrives;
import static com.muziko.manager.MuzikoConstants.audioContentTypes;

public class DriveApi extends GenericCloudApi {

    private Drive driveService;
    private Permission newPermission;

    private static Permission insertPermission(Drive service, String fileId) throws IOException {
        Permission newPermission = new Permission();
        newPermission.setType("anyone");
        newPermission.setRole("reader");

        return service.permissions().insert(fileId, newPermission).execute();
    }

    public Drive getDriveService() {
        return driveService;
    }

    @DebugLog
    public boolean initialize(Context context, String accountName) {
        try {

            this.context = context;
            this.accountName = accountName;

            if (this.accountName != null) {

                ArrayList<String> scopes = new ArrayList<>();
                scopes.add(DriveScopes.DRIVE);
                scopes.add(DriveScopes.DRIVE_APPDATA);
                scopes.add(DriveScopes.DRIVE_APPS_READONLY);
                scopes.add(DriveScopes.DRIVE_FILE);
                scopes.add(DriveScopes.DRIVE_METADATA);
                scopes.add(DriveScopes.DRIVE_METADATA_READONLY);
                scopes.add(DriveScopes.DRIVE_PHOTOS_READONLY);
                scopes.add(DriveScopes.DRIVE_READONLY);

                driveService =
                        new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                GoogleAccountCredential.usingOAuth2(CondomContext.wrap(context, "Drive"), scopes)
                                        .setSelectedAccountName(this.accountName))
                                .setApplicationName("Muziko/1.0")
                                .build();

                return true;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return false;
    }

    @DebugLog
    public void initialize(Context context, int cloudAccountId, String accountName) {
        try {

            this.context = context;
            this.cloudAccountId = cloudAccountId;
            this.accountName = accountName;

            if (this.accountName != null) {

                ArrayList<String> scopes = new ArrayList<>();
                scopes.add(DriveScopes.DRIVE);
                scopes.add(DriveScopes.DRIVE_APPDATA);
                scopes.add(DriveScopes.DRIVE_APPS_READONLY);
                scopes.add(DriveScopes.DRIVE_FILE);
                scopes.add(DriveScopes.DRIVE_METADATA);
                scopes.add(DriveScopes.DRIVE_METADATA_READONLY);
                scopes.add(DriveScopes.DRIVE_PHOTOS_READONLY);
                scopes.add(DriveScopes.DRIVE_READONLY);

                driveService =
                        new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
                                GoogleAccountCredential.usingOAuth2(CondomContext.wrap(context, "Drive"), scopes)
                                        .setSelectedAccountName(this.accountName))
                                .setApplicationName("Muziko/1.0")
                                .build();

            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void create(DriveConnectionCallbacks driveConnectionCallbacks) {
        if (driveService != null) {

            newPermission = new Permission();
            newPermission.setType("anyone");
            newPermission.setRole("reader");

            mConnected = false;
            Exception exception = null;
            try {
                // GoogleAuthUtil.getToken(mAct, email, DriveScopes.DRIVE_FILE);   SO 30122755
                driveService.files().get("root").setFields("title").execute();
            } catch (
                    UserRecoverableAuthIOException
                            uraIOEx) { // standard authorization failure - user fixable
                exception = uraIOEx;
            } catch (
                    GoogleAuthIOException
                            gaIOEx) { // usually PackageName /SHA1 mismatch in DevConsole
                exception = gaIOEx;
            } catch (IOException e) { // '404 not found' in FILE scope, consider connected
                if (e instanceof GoogleJsonResponseException) {
                    if (404 == ((GoogleJsonResponseException) e).getStatusCode())
                        mConnected = true;
                }
            } catch (Exception e) { // "the name must not be empty" indicates
                Crashlytics.logException(
                        e); // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
            }
            if (exception instanceof UserRecoverableAuthIOException || exception != null) {

                if (driveConnectionCallbacks != null) {
                    driveConnectionCallbacks.onDriveConnectionFailed(exception);
                }
            } else {
                mConnected = true;
                if (CloudAccountRealmHelper.getCloudAccount(accountName, CloudManager.GOOGLEDRIVE) != null) {
                    AppController.toast(context, context.getString(R.string.cloud_account_already_added));
                }
                cloudAccountId =
                        CloudAccountRealmHelper.insert(
                                accountName, "", CloudManager.GOOGLEDRIVE, false);
                CloudManager.Instance().addDriveApi(accountName, DriveApi.this);
                connectedCloudDrives.add(cloudAccountId);
                if (driveConnectionCallbacks != null) {
                    driveConnectionCallbacks.onDriveConnected(cloudAccountId);
                }
                searchAudio();
            }

        }
    }

    @DebugLog
    public void connect(DriveConnectionCallbacks driveConnectionCallbacks) {
        if (driveService != null) {

            newPermission = new Permission();
            newPermission.setType("anyone");
            newPermission.setRole("reader");

            mConnected = false;
            Exception exception = null;
            try {
                // GoogleAuthUtil.getToken(mAct, email, DriveScopes.DRIVE_FILE);   SO 30122755
                driveService.files().get("root").setFields("title").execute();
            } catch (
                    UserRecoverableAuthIOException
                            uraIOEx) { // standard authorization failure - user fixable
                exception = uraIOEx;
            } catch (
                    GoogleAuthIOException
                            gaIOEx) { // usually PackageName /SHA1 mismatch in DevConsole
                exception = gaIOEx;
            } catch (IOException e) { // '404 not found' in FILE scope, consider connected
                if (e instanceof GoogleJsonResponseException) {
                    if (404 == ((GoogleJsonResponseException) e).getStatusCode())
                        mConnected = true;
                }
            } catch (Exception e) { // "the name must not be empty" indicates
                Crashlytics.logException(
                        e); // UNREGISTERED / EMPTY account in 'setSelectedAccountName()' above
            }

            mConnected = true;
            CloudManager.Instance().addDriveApi(accountName, DriveApi.this);
            connectedCloudDrives.add(cloudAccountId);
            if (driveConnectionCallbacks != null) {
                if (mConnected) {
                    driveConnectionCallbacks.onDriveConnected(cloudAccountId);
                } else { // null indicates general error (fatal)
                    driveConnectionCallbacks.onDriveConnectionFailed(exception);
                }
            }
            searchAudio();
        }
    }

    /**
     * **********************************************************************************************
     * find file/folder in GOODrive
     *
     * @param prnId parent ID (optional), null searches full drive, "root" searches Drive root
     * @param titl  file/folder name (optional)
     * @param mime  file/folder mime type (optional)
     * @return arraylist of found objects
     */
    private ArrayList<ContentValues> search(String prnId, String titl, String mime) {
        ArrayList<ContentValues> gfs = new ArrayList<>();
        if (driveService != null && mConnected)
            try {
                // add query conditions, build query
                String qryClause = "'me' in owners and ";
                if (prnId != null) qryClause += "'" + prnId + "' in parents and ";
                if (titl != null) qryClause += "title = '" + titl + "' and ";
                if (mime != null) qryClause += "mimeType = '" + mime + "' and ";
                qryClause = qryClause.substring(0, qryClause.length() - " and ".length());
                Drive.Files.List qry =
                        driveService
                                .files()
                                .list()
                                .setQ(qryClause)
                                .setFields("items(id,mimeType,labels/trashed,title),nextPageToken");
                String npTok = null;
                if (qry != null)
                    do {
                        FileList gLst = qry.execute();
                        if (gLst != null) {
                            for (File gFl : gLst.getItems()) {
                                if (gFl.getLabels().getTrashed()) continue;
                                gfs.add(DriveApiHelpers.newCVs(gFl.getTitle(), gFl.getId(), gFl.getMimeType()));
                            } //else DriveApiHelpers.lg("failed " + gFl.getTitle());
                            npTok = gLst.getNextPageToken();
                            qry.setPageToken(npTok);
                        }
                    }
                    while (npTok != null && npTok.length() > 0); //DriveApiHelpers.lg("found " + vlss.size());
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        return gfs;
    }

    @DebugLog
    private String getUrl(String fileId) {
        if (driveService != null && mConnected && fileId != null)
            try {
                Permission newPermission = new Permission();
                newPermission.setType("anyone");
                newPermission.setRole("reader");

                driveService.permissions().insert(fileId, newPermission).execute();
                File gFl = driveService.files().get(fileId).execute();
                if (gFl != null) {
                    return gFl.getWebContentLink();
                }
            } catch (Exception e) {
                DriveApiHelpers.le(e);
            }
        return null;
    }

    @DebugLog
    private QueueItem getFileItem(
            int cloudAccountId,
            String accountName,
            QueueItem queueItem,
            ContentValues contentValues) {
        File file = null;
        QueueItem fileModel = new QueueItem();
        try {
            file = driveService.files().get(contentValues.getAsString(DriveApiHelpers.GDID)).execute();
            fileModel.id = Utils.randLong();
            fileModel.album = CloudManager.GOOGLEDRIVE;
            fileModel.artist_name = accountName;
            if (contentValues.getAsString(DriveApiHelpers.TITL) == null || contentValues.getAsString(DriveApiHelpers.TITL).isEmpty()) {
                fileModel.title = MuzikoConstants.UNKNOWN_TITLE;
            } else {
                fileModel.title = contentValues.getAsString(DriveApiHelpers.TITL);
            }
            driveService.permissions().insert(contentValues.getAsString(DriveApiHelpers.GDID), newPermission).execute();
            File gFl = driveService.files().get(contentValues.getAsString(DriveApiHelpers.GDID)).execute();
            if (gFl != null) {
                fileModel.data = gFl.getWebContentLink();
            } else {
                return fileModel;
            }
            fileModel.folder_path = contentValues.getAsString(DriveApiHelpers.GDID);
            fileModel.album = CloudManager.GOOGLEDRIVE;
            fileModel.folder = false;
            fileModel.storage = cloudAccountId;
            fileModel.order = queueItem.id;
            fileModel.size = file.getFileSize();
            fileModel.date = file.getCreatedDate().getValue();
            fileModel.dateModified = System.currentTimeMillis();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return fileModel;
    }

    //    @DebugLog
    public void getFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacks) {

        AsyncJob.doInBackground(
                () -> {
                    ArrayList<QueueItem> items = new ArrayList<>();
                    if (driveService != null && mConnected && queueItem != null) {
                        ArrayList<ContentValues> cvs = search(queueItem.data, null, null);
                        for (int i = 0; i < cvs.size(); i++) {
                            ContentValues contentValues = cvs.get(i);
                            String mime = contentValues.getAsString(DriveApiHelpers.MIME);
                            if (Arrays.asList(audioContentTypes).contains(mime)) {
                                items.add(
                                        getFileItem(
                                                cloudAccountId,
                                                accountName,
                                                queueItem,
                                                contentValues));
                            }
                            if (mime.equalsIgnoreCase(DriveApiHelpers.MIME_FLDR)) {
                                items.add(
                                        CloudHelper.getFolderItem(
                                                cloudAccountId,
                                                accountName,
                                                queueItem,
                                                contentValues));
                            }
                        }

                        Collections.sort(items, (p1, p2) -> {
                            int b1 = p1.isFolder() ? 1 : 0;
                            int b2 = p2.isFolder() ? 1 : 0;
                            if (b2 - b1 == 0) {
                                return p1.getTitle()
                                        .toLowerCase()
                                        .compareTo(p2.getTitle().toLowerCase());
                            } else {
                                return b2 - b1;
                            }
                        });

                        cloudFolderCallbacks.onFoldersReturned(items);
                    } else {
                        cloudFolderCallbacks.onFoldersFailed();
                    }
                });
    }

    @DebugLog
    public void getFiles() {
        if (driveService != null && mConnected)
            try {
                FileList fileList = driveService.files().list().execute();

            } catch (Exception e) {
                DriveApiHelpers.le(e);
            }
    }


    public File getFile(String fileId) {
        File file = null;
        if (driveService != null && mConnected && fileId != null) {
            try {
                file = driveService.files().get(fileId).execute();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }

        }

        return file;
    }

    public ArrayList<QueueItem> getFolderPath(QueueItem queueItem) {
        ArrayList<QueueItem> folderPath = new ArrayList<>();
        ArrayList<String> parentFiles = new ArrayList<>();

        File file = getFile(queueItem.folder_path);
        ParentReference parent = file.getParents().get(0);
        parentFiles.add(parent.getId());
        while (!parent.getIsRoot()) {
            file = getFile(parent.getId());
            parent = file.getParents().get(0);
            parentFiles.add(parent.getId());
        }

        for (String parentId : parentFiles) {
            folderPath.add(
                    CloudHelper.getFolderItem(
                            cloudAccountId,
                            accountName,
                            queueItem,
                            getFile(parentId)));
        }

        Collections.reverse(folderPath);
        return folderPath;
    }

    public void searchAudio() {
        if (CloudManager.Instance().isSyncing()) return;
        AsyncJob.doInBackground(
                () -> {
                    try {
                        CloudManager.Instance().setSyncing(true);
                        ArrayList<QueueItem> cloudTracks = new ArrayList<>();
                        cloudTracks.addAll(TrackRealmHelper.getTracks(cloudAccountId).values());
                        if (cloudTracks.size() == 0) {
                            showingProgress = true;
                            showProgress(context, "Syncing Google Drive");
                        }
                        LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                        LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                        ArrayList<ContentValues> mp3 = search(null, null, MIME_AUDIO);
                        ArrayList<ContentValues> mpeg = search(null, null, MIME_MPEG);
                        ArrayList<ContentValues> xmpeg = search(null, null, MIME_XMPEG);
                        ArrayList<ContentValues> cvs = new ArrayList<>();
                        cvs.addAll(mp3);
                        cvs.addAll(mpeg);
                        cvs.addAll(xmpeg);
                        for (int i = 0; i < cvs.size(); i++) {
                            ContentValues contentValues = cvs.get(i);
                            String id = contentValues.getAsString(DriveApiHelpers.GDID);
                            File file = null;
                            try {
                                file = driveService.files().get(contentValues.getAsString(DriveApiHelpers.GDID)).execute();
                            } catch (IOException e) {
                                Crashlytics.logException(e);
                                CloudManager.Instance().setSyncing(false);
                            }
                            QueueItem queueItem = new QueueItem();

                            driveService.permissions().insert(cvs.get(i).getAsString(DriveApiHelpers.GDID), newPermission).execute();
                            File gFl = driveService.files().get(cvs.get(i).getAsString(DriveApiHelpers.GDID)).execute();
                            if (gFl != null) {
                                queueItem.data = gFl.getWebContentLink();
                            } else {
                                continue;
                            }

                            queueItem.cloudId = cvs.get(i).getAsString(DriveApiHelpers.GDID);
                            queueItem.folder_path = file.getParents().get(0).getId();
                            queueItem.album = CloudManager.GOOGLEDRIVE;
                            if (contentValues.getAsString(DriveApiHelpers.TITL) == null || contentValues.getAsString(DriveApiHelpers.TITL).isEmpty()) {
                                queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                            } else {
                                queueItem.title = contentValues.getAsString(DriveApiHelpers.TITL);
                            }
                            queueItem.artist_name = CloudAccountRealmHelper.getCloudAccount(cloudAccountId).getAccountName();
                            queueItem.storage = cloudAccountId;
                            queueItem.size = file.getFileSize() != null ? file.getFileSize() : 0;
                            queueItem.date = file.getCreatedDate().getValue();
                            queueItem.dateModified = System.currentTimeMillis();

//                            FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
//                            mmr.setDataSource(queueItem.data);
//                            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
//                            mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
//
//                            queueItem.title = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE);
//                            if (queueItem.title == null || queueItem.title.isEmpty()) {
//                                queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
//                            }
//                            queueItem.album_name = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
//                            queueItem.artist_name = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
//                            queueItem.duration = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
//
//                            mmr.release();

                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(queueItem.data, new HashMap<String, String>());
                            String id3Title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            if (id3Title != null) {
                                queueItem.title = id3Title;
                            }
                            String id3AlbumName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                            if (id3AlbumName != null) {
                                queueItem.album_name = id3AlbumName;
                            }
                            String id3ArtistName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            if (id3Title != null) {
                                queueItem.artist_name = id3ArtistName;
                            }
                            String id3Duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            if (id3Title != null) {
                                queueItem.duration = id3Duration;
                            }
                            mediaMetadataRetriever.release();

                            queueItems.put(queueItem.data, queueItem);
                            allCloudTracks.put(queueItem.data, queueItem);
                            boolean trackExists = TrackRealmHelper.trackExists(id, cloudAccountId);
                            if (!trackExists) {
                                queueItems.put(queueItem.data, queueItem);
                            }

                        }
                        boolean shouldRefresh = false;
                        if (queueItems.size() > 0) {
                            if (TrackRealmHelper.insertCloudList(cloudAccountId, queueItems)) {
                                shouldRefresh = true;
                            }
                        }
                        if (allCloudTracks.size() > 0) {
                            if (TrackRealmHelper.removedCloudTracks(cloudAccountId, allCloudTracks)) {
                                shouldRefresh = true;
                            }
                        }
                        if (shouldRefresh) {
                            EventBus.getDefault().post(new RefreshEvent(1000));
                        }
                        if (showingProgress) {
                            cancelProgress();
                        }
                        CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                        CloudManager.Instance().setSyncing(false);
                    } catch (Exception e) {
                        Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();

                        Crashlytics.logException(e);
                        if (showingProgress) {
                            cancelProgress();
                        }
                        CloudManager.Instance().setSyncing(false);
                        CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    public void search(String folder) {
        AsyncJob.doInBackground(
                () -> {
                    try {
                        LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                        LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                        ArrayList<ContentValues> cvs = search(folder, null, null);
                        for (int i = 0; i < cvs.size(); i++) {
                            ContentValues contentValues = cvs.get(i);
                            String id = contentValues.getAsString(DriveApiHelpers.GDID);
                            String mime = contentValues.getAsString(DriveApiHelpers.MIME);
                            if (Arrays.asList(audioContentTypes).contains(mime)) {

                                File file = null;
                                try {
                                    file = driveService
                                            .files()
                                            .get(contentValues.getAsString(DriveApiHelpers.GDID))
                                            .execute();
                                } catch (IOException e) {
                                    Crashlytics.logException(e);
                                }
                                QueueItem queueItem = new QueueItem();
                                driveService.permissions().insert(cvs.get(i).getAsString(DriveApiHelpers.GDID), newPermission).execute();
                                File gFl = driveService.files().get(cvs.get(i).getAsString(DriveApiHelpers.GDID)).execute();
                                if (gFl != null) {
                                    queueItem.data = gFl.getWebContentLink();
                                } else {
                                    continue;
                                }
                                queueItem.cloudId = cvs.get(i).getAsString(DriveApiHelpers.GDID);
                                queueItem.folder_path = file.getParents().get(0).getId();
                                queueItem.album = CloudManager.GOOGLEDRIVE;
                                if (contentValues.getAsString(DriveApiHelpers.TITL) == null || contentValues.getAsString(DriveApiHelpers.TITL).isEmpty()) {
                                    queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                                } else {
                                    queueItem.title = contentValues.getAsString(DriveApiHelpers.TITL);
                                }
                                queueItem.artist_name = CloudAccountRealmHelper.getCloudAccount(cloudAccountId).getAccountName();
                                queueItem.storage = cloudAccountId;
                                queueItem.size =
                                        file.getFileSize() != null ? file.getFileSize() : 0;
                                queueItem.date = file.getCreatedDate().getValue();
                                queueItem.dateModified = System.currentTimeMillis();

                                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                mediaMetadataRetriever.setDataSource(queueItem.data, new HashMap<String, String>());
                                String id3Title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                if (id3Title != null) {
                                    queueItem.title = id3Title;
                                }
                                String id3AlbumName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                                if (id3AlbumName != null) {
                                    queueItem.album_name = id3AlbumName;
                                }
                                String id3ArtistName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                                if (id3Title != null) {
                                    queueItem.artist_name = id3ArtistName;
                                }
                                String id3Duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                if (id3Title != null) {
                                    queueItem.duration = id3Duration;
                                }
                                mediaMetadataRetriever.release();

                                queueItems.put(queueItem.data, queueItem);
                                allCloudTracks.put(queueItem.data, queueItem);
                                boolean trackExists = TrackRealmHelper.trackExists(id, cloudAccountId);
                                if (!trackExists) {
                                    queueItems.put(queueItem.data, queueItem);
                                }

                            }
                        }
                        boolean shouldRefresh = false;
                        if (queueItems.size() > 0) {
                            if (TrackRealmHelper.insertCloudList(cloudAccountId, queueItems)) {
                                shouldRefresh = true;
                            }
                        }
                        if (allCloudTracks.size() > 0) {
                            if (TrackRealmHelper.removedCloudTracks(cloudAccountId, allCloudTracks)) {
                                shouldRefresh = true;
                            }
                        }
                        if (shouldRefresh) {
                            EventBus.getDefault().post(new RefreshEvent(1000));
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public void scanAll() {
        scan("root");
    }

    @DebugLog
    private void scan(String folder) {
        AsyncJob.doInBackground(
                () -> {
                    ArrayList<ContentValues> cvs = search(folder, null, null);
                    for (int i = 0; i < cvs.size(); i++) {
                        processScanResults(cvs.get(i), getUrl(cvs.get(i).getAsString(DriveApiHelpers.GDID)));
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    private void processScanResults(ContentValues contentValues, String url) {
        AsyncJob.doInBackground(
                () -> {
                    try {
                        String id = contentValues.getAsString(DriveApiHelpers.GDID);
                        String mime = contentValues.getAsString(DriveApiHelpers.MIME);
                        if (Arrays.asList(audioContentTypes).contains(mime)) {
                            File file = null;
                            try {
                                file = driveService.files().get(contentValues.getAsString(DriveApiHelpers.GDID)).execute();
                            } catch (IOException e) {
                                Crashlytics.logException(e);
                            }
                            QueueItem queueItem = new QueueItem();
                            queueItem.data = url;
                            queueItem.folder_path = id;
                            queueItem.album = CloudManager.GOOGLEDRIVE;
                            if (contentValues.getAsString(DriveApiHelpers.TITL) == null || contentValues.getAsString(DriveApiHelpers.TITL).isEmpty()) {
                                queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                            } else {
                                queueItem.title = contentValues.getAsString(DriveApiHelpers.TITL);
                            }
                            queueItem.artist_name = CloudAccountRealmHelper.getCloudAccount(cloudAccountId).getAccountName();
                            queueItem.storage = cloudAccountId;
                            queueItem.size = file.getFileSize() != null ? file.getFileSize() : 0;
                            queueItem.date = file.getCreatedDate().getValue();
                            queueItem.dateModified = System.currentTimeMillis();
                            TrackRealmHelper.insertTrack(queueItem);
                        }
                        if (mime.equalsIgnoreCase(DriveApiHelpers.MIME_FLDR)) {
                            scan(id);
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public String newFolder(String parentFolderid, String folderName) {
        String newFolderId = null;
        if (parentFolderid.equalsIgnoreCase("Cloud")) parentFolderid = null;

        if (driveService != null && mConnected && folderName != null) {
            try {
                File meta = new File();
                meta.setParents(
                        Collections.singletonList(
                                new ParentReference()
                                        .setId(parentFolderid == null ? "root" : parentFolderid)));
                meta.setTitle(folderName);
                meta.setMimeType(DriveApiHelpers.MIME_FLDR);

                File gFl = null;
                try {
                    gFl = driveService.files().insert(meta).execute();
                } catch (Exception e) {
                    DriveApiHelpers.le(e);
                }
                if (gFl != null && gFl.getId() != null) {
                    newFolderId = gFl.getId();
                }

            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        return newFolderId;
    }

    @DebugLog
    public boolean deleteFile(String fileId) {
        if (driveService != null && mConnected && fileId != null) {
            try {
                driveService.files().delete(fileId).execute();
            } catch (Exception e) {
                Crashlytics.logException(e);
                return false;
            }
        }
        return true;
    }

    @DebugLog
    public interface DriveConnectionCallbacks {
        void onDriveConnectionFailed(Exception ex);

        void onDriveConnected(int cloudAccountId);
    }
}
