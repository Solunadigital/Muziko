package com.muziko.cloud.DropBox;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.users.FullAccount;
import com.github.simonpercic.aircycle.ActivityAirCycle;
import com.muziko.BuildConfig;
import com.muziko.R;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.CloudHelper;
import com.muziko.cloud.GenericCloudApi;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.ThreadManager;
import com.oasisfeng.condom.CondomContext;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.muziko.manager.CloudManager.connectedCloudDrives;
import static com.muziko.manager.MuzikoConstants.extensions;

public class DropBoxApi extends GenericCloudApi {

    private boolean started = false;
    private DbxClientV2 dropBoxService = null;
    private DropBoxConnectionCallbacks dropBoxConnectionCallbacks;
    private DropBoxAirCycleListener dropBoxAirCycleListener;
    private FileMetadata dropboxDownload;
    private boolean searchRunning;

    public DbxClientV2 getDropBoxService() {
        return dropBoxService;
    }

    @DebugLog
    public DropBoxAirCycleListener createActivityListener(
            Context context, DropBoxConnectionCallbacks dropBoxConnectionCallbacks) {
        this.context = context;
        dropBoxAirCycleListener = new DropBoxAirCycleListener();
        this.dropBoxConnectionCallbacks = dropBoxConnectionCallbacks;
        return dropBoxAirCycleListener;
    }

    @DebugLog
    public void createBasicListener(
            Context context,
            int cloudAccountId,
            DropBoxConnectionCallbacks dropBoxConnectionCallbacks) {
        this.context = context;
        this.cloudAccountId = cloudAccountId;
        this.dropBoxConnectionCallbacks = dropBoxConnectionCallbacks;
    }

    @DebugLog
    public void initialize(Context context) {
        this.context = context;
        started = true;
        Auth.startOAuth2Authentication(CondomContext.wrap(context, "DropBox"), BuildConfig.DropBoxAppKey);
    }

    @DebugLog
    public void create(String accessToken) {
        try {
            if (dropBoxConnectionCallbacks != null) {
                dropBoxConnectionCallbacks.onDropBoxConnectionStarted();
            }
            mConnected = false;
            if (dropBoxService == null) {
                DbxRequestConfig requestConfig =
                        DbxRequestConfig.newBuilder(context.getString(R.string.app_name))
                                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                                .build();

                dropBoxService = new DbxClientV2(requestConfig, accessToken);

                AsyncJob.doInBackground(
                        () -> {
                            FullAccount fullAccount = null;

                            try {
                                fullAccount = dropBoxService.users().getCurrentAccount();

                            } catch (DbxException e) {
                                Crashlytics.logException(e);
                                if (dropBoxConnectionCallbacks != null) {
                                    dropBoxConnectionCallbacks.onDropBoxConnectionFailed(e);
                                }
                            }
                            mConnected = true;
                            accountName = fullAccount.getEmail();
                            if (CloudAccountRealmHelper.getCloudAccount(accountName, CloudManager.GOOGLEDRIVE) != null) {
                                AppController.toast(context, context.getString(R.string.cloud_account_already_added));
                            }
                            cloudAccountId =
                                    CloudAccountRealmHelper.insert(
                                            accountName,
                                            accessToken,
                                            CloudManager.DROPBOX,
                                            false);
                            CloudManager.Instance().addDropBoxApi(accountName, DropBoxApi.this);
                            connectedCloudDrives.add(cloudAccountId);
                            if (dropBoxConnectionCallbacks != null) {
                                if (mConnected) {
                                    dropBoxConnectionCallbacks.onDropBoxConnected(cloudAccountId);
                                } else { // null indicates general error (fatal)
                                    dropBoxConnectionCallbacks.onDropBoxConnectionFailed();
                                }
                            }
                            search("mp3");
                        });
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void connect(String accessToken) {
        try {
            mConnected = false;
            if (dropBoxService == null) {
                DbxRequestConfig requestConfig =
                        DbxRequestConfig.newBuilder(context.getString(R.string.app_name))
                                .withHttpRequestor(
                                        new OkHttp3Requestor(
                                                OkHttp3Requestor.defaultOkHttpClient()))
                                .build();

                dropBoxService = new DbxClientV2(requestConfig, accessToken);

                AsyncJob.doInBackground(
                        () -> {
                            FullAccount fullAccount = null;

                            try {
                                fullAccount = dropBoxService.users().getCurrentAccount();

                            } catch (DbxException e) {
                                Crashlytics.logException(e);
                                if (dropBoxConnectionCallbacks != null) {
                                    dropBoxConnectionCallbacks.onDropBoxConnectionFailed(e);
                                }
                            }
                            if (fullAccount != null) {
                                mConnected = true;
                                if (fullAccount.getEmail() != null) {
                                    accountName = fullAccount.getEmail();
                                }
                                CloudManager.Instance().addDropBoxApi(accountName, DropBoxApi.this);
                                connectedCloudDrives.add(cloudAccountId);
                                if (dropBoxConnectionCallbacks != null) {
                                    if (mConnected) {
                                        dropBoxConnectionCallbacks.onDropBoxConnected(cloudAccountId);
                                    } else { // null indicates general error (fatal)
                                        dropBoxConnectionCallbacks.onDropBoxConnectionFailed();
                                    }
                                }
                                search("mp3");
                            }
                        });
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void search(String searchString) {
        if (CloudManager.Instance().isSyncing()) return;
        AsyncJob.doInBackground(
                () -> {
                    try {
                        if (dropBoxService != null && mConnected && searchString != null) {
                            CloudManager.Instance().setSyncing(true);
                            ArrayList<QueueItem> cloudTracks = new ArrayList<>();
                            cloudTracks.addAll(TrackRealmHelper.getTracks(cloudAccountId).values());
                            if (cloudTracks.size() == 0) {
                                showingProgress = true;
                                showProgress(context, "Syncing DropBox");
                            }
                            LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                            LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                            SearchResult searchResult = null;
                            try {
                                searchResult = dropBoxService.files().search("", searchString);
                            } catch (DbxException | IllegalArgumentException e) {
                                Crashlytics.logException(e);
                                CloudManager.Instance().setSyncing(false);
                            }
                            if (searchResult != null) {
                                List<SearchMatch> searchMatches = searchResult.getMatches();
                                for (SearchMatch searchMatch : searchMatches) {

                                    Metadata metadata = searchMatch.getMetadata();
                                    if (metadata instanceof FileMetadata) {

                                        FileMetadata fileMetadata = (FileMetadata) metadata;

                                        String ext = FilenameUtils.getExtension(fileMetadata.getName());
                                        if (Arrays.asList(extensions).contains(ext)) {
                                            QueueItem queueItem = new QueueItem();
                                            queueItem.data = metadata.getPathLower();
                                            queueItem.cloudId = metadata.getPathLower();
                                            queueItem.folder_path = new File(metadata.getPathDisplay()).getParent();
                                            queueItem.album = CloudManager.DROPBOX;
                                            queueItem.title = metadata.getName();
                                            queueItem.artist_name = "Cloud > " + CloudAccountRealmHelper.getCloudAccount(cloudAccountId).getName();
                                            queueItem.storage = cloudAccountId;
                                            queueItem.size = fileMetadata.getSize();
                                            queueItem.date = fileMetadata.getClientModified().getTime();
                                            queueItem.dateModified = System.currentTimeMillis();

                                            String url = getUrl(fileMetadata);
                                            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                            mediaMetadataRetriever.setDataSource(url, new HashMap<String, String>());
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
                                            boolean trackExists = TrackRealmHelper.trackExists(metadata.getPathLower(), cloudAccountId);
                                            if (!trackExists) {
                                                queueItems.put(queueItem.data, queueItem);
                                            }
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
                                if (showingProgress) {
                                    cancelProgress();
                                }
                                CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                                CloudManager.Instance().setSyncing(false);
                            }
                        }
                    } catch (Exception e) {
                        if (showingProgress) {
                            cancelProgress();
                        }
                        Crashlytics.logException(e);
                        CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                        CloudManager.Instance().setSyncing(false);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public void recursiveSearch(String searchString) {
        AsyncJob.doInBackground(
                () -> {
                    try {
                        if (dropBoxService != null && mConnected && searchString != null && !searchRunning)  {
                            searchRunning = true;
                            LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                            LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                            ListFolderResult listFolderResult = null;
                            ArrayList<Metadata> metadataList = new ArrayList<>();
                            try {
                                listFolderResult = dropBoxService.files().listFolderBuilder("").withRecursive(true).start();
                                while (true) {
                                    if (listFolderResult.getEntries().size() > 0) {
                                        metadataList.addAll(listFolderResult.getEntries());
                                        if (listFolderResult.getHasMore()) {
                                            listFolderResult = dropBoxService.files().listFolderContinue(listFolderResult.getCursor());
                                        } else {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } catch (DbxException | IllegalArgumentException e) {
                                Crashlytics.logException(e);
                                searchRunning = false;
                            }

                            for (Metadata metadata : metadataList) {
                                if (metadata instanceof FileMetadata) {

                                    FileMetadata fileMetadata = (FileMetadata) metadata;

                                    String ext = FilenameUtils.getExtension(fileMetadata.getName());
                                    if (Arrays.asList(extensions).contains(ext)) {
                                        QueueItem queueItem = new QueueItem();
                                        queueItem.data = metadata.getPathLower();
                                        queueItem.cloudId = metadata.getPathLower();
                                        queueItem.folder_path = new File(metadata.getPathDisplay()).getParent();
                                        queueItem.album = CloudManager.DROPBOX;
                                        if (metadata.getName() == null || metadata.getName().isEmpty()) {
                                            queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                                        } else {
                                            queueItem.title = metadata.getName();
                                        }
                                        queueItem.artist_name = "Cloud > " + CloudAccountRealmHelper.getCloudAccount(cloudAccountId).getName();
                                        queueItem.storage = cloudAccountId;
                                        queueItem.size = fileMetadata.getSize();
                                        queueItem.date = fileMetadata.getClientModified().getTime();
                                        queueItem.dateModified = System.currentTimeMillis();
                                        queueItems.put(queueItem.data, queueItem);
                                        allCloudTracks.put(queueItem.data, queueItem);
                                        boolean trackExists = TrackRealmHelper.trackExists(metadata.getPathLower(), cloudAccountId);
                                        if (!trackExists) {
                                            queueItems.put(queueItem.data, queueItem);
                                        }
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
                            searchRunning = false;
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        searchRunning = false;
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public void scanAll() {
        scan("");
    }

    @DebugLog
    private void scan(String folder) {
        try {
            if (dropBoxService != null && mConnected && folder != null) {

                ListFolderResult listFolderResult = null;
                try {
                    listFolderResult = dropBoxService.files().listFolder(folder);
                } catch (DbxException | IllegalArgumentException e) {
                    Crashlytics.logException(e);
                }
                if (listFolderResult != null) {
                    List<Metadata> mFiles = listFolderResult.getEntries();
                    for (Metadata metadata : mFiles) {

                        if (metadata instanceof FolderMetadata) {
                            scan(metadata.getPathDisplay());
                        } else {

                            String ext = FilenameUtils.getExtension(metadata.getName());

                            if (ext.equalsIgnoreCase("mp3")) {

                                QueueItem queueItem = new QueueItem();
                                queueItem.data = metadata.getPathLower();
                                queueItem.folder_path = metadata.getPathLower();
                                if (metadata.getName() == null || metadata.getName().isEmpty()) {
                                    queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                                } else {
                                    queueItem.title = metadata.getName();
                                }
                                queueItem.artist_name =
                                        "Cloud > "
                                                + CloudAccountRealmHelper.getCloudAccount(
                                                cloudAccountId)
                                                .getName();
                                queueItem.storage = cloudAccountId;
                                TrackRealmHelper.insertTrack(queueItem);
                            }
                        }
                    }
                }
                EventBus.getDefault().post(new RefreshEvent(1000));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void getFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacks) {

        AsyncJob.doInBackground(
                () -> {
                    ArrayList<QueueItem> items = new ArrayList<>();

                    if (dropBoxService != null && mConnected && queueItem != null) {

                        ListFolderResult listFolderResult = null;
                        try {
                            listFolderResult = dropBoxService.files().listFolder(queueItem.data);
                        } catch (DbxException | IllegalArgumentException e) {
                            Crashlytics.logException(e);
                        }
                        if (listFolderResult != null) {
                            List<Metadata> mFiles = listFolderResult.getEntries();
                            for (Metadata metadata : mFiles) {
                                if (metadata instanceof FolderMetadata) {
                                    FolderMetadata folderMetadata = (FolderMetadata) metadata;
                                    items.add(CloudHelper.getFolderItem(cloudAccountId, accountName, queueItem, folderMetadata));
                                } else {
                                    FileMetadata fileMetadata = (FileMetadata) metadata;
                                    String ext = FilenameUtils.getExtension(metadata.getName());
                                    if (Arrays.asList(extensions).contains(ext)) {
                                        items.add(CloudHelper.getFileItem(cloudAccountId, accountName, queueItem, fileMetadata));
                                    }
                                }
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
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public String getUrl(String file) {
        if (dropBoxService != null && mConnected && file != null) {
            String url = "";
            try {
                Metadata metadata = dropBoxService.files().getMetadata(file);
                url = dropBoxService.files().getTemporaryLink(metadata.getPathLower()).getLink();
            } catch (DbxException e) {
                Crashlytics.logException(e);
            }

            return url;
        }
        return null;
    }

    @DebugLog
    public String getUrl(FileMetadata metadata) {
        if (dropBoxService != null && mConnected && metadata != null) {

            try {
                return dropBoxService.files().getTemporaryLink(metadata.getPathLower()).getLink();
            } catch (DbxException e) {
                Crashlytics.logException(e);
            }

            //            try {
            //                return dropBoxService.sharing().createSharedLinkWithSettings(metadata.getPathLower()).getUrl();
            //            } catch (CreateSharedLinkWithSettingsErrorException ex) {
            //
            //                if (ex.errorValue.isSharedLinkAlreadyExists()) {
            //                    try {
            //                        String origUrl = dropBoxService.sharing().getFileMetadata(metadata.getPathLower()).getLinkMetadata().getUrl();
            //                        Uri downloadString = Utils.formatDropBoxUrl(Uri.parse(origUrl));
            //                        return downloadString.toString();
            //                    } catch (DbxException e) {
            //                        Crashlytics.logException(e);
            //                    }
            //
            //                }
            //            } catch (DbxException ex) {
            //                Crashlytics.logException(ex);
            //            }

        }
        return null;
    }

    public FileMetadata getFile(String fileId) {
        FileMetadata metadata = null;
        if (dropBoxService != null && mConnected && fileId != null) {
            try {
                metadata = (FileMetadata) dropBoxService.files().getMetadata(fileId);
            } catch (DbxException e) {
                Crashlytics.logException(e);
            }
        }

        return metadata;
    }

    public FolderMetadata getFolder(String fileId) {
        FolderMetadata metadata = null;
        if (dropBoxService != null && mConnected && fileId != null) {
            try {
                metadata = (FolderMetadata) dropBoxService.files().getMetadata(fileId);
            } catch (DbxException e) {
                Crashlytics.logException(e);
            }
        }

        return metadata;
    }

    public ArrayList<QueueItem> getFolderPath(QueueItem queueItem) {
        ArrayList<QueueItem> folderPath = new ArrayList<>();
        Metadata metadata = getFile(queueItem.data);
        if (metadata == null) return null;
        String path = metadata.getPathDisplay();
        String[] folders = metadata.getPathDisplay().split("/+");
        ArrayList<String> parents = new ArrayList<>(Arrays.asList(folders));
        parents.remove(parents.size() - 1);
        for (int i = 1; i < parents.size(); i++) {
            path = path.substring(0, path.lastIndexOf("/"));
            folderPath.add(
                    CloudHelper.getFolderItem(
                            cloudAccountId,
                            accountName,
                            queueItem,
                            getFolder(path)));
        }
        folderPath.add(new QueueItem());
        Collections.reverse(folderPath);
        return folderPath;
    }

    @DebugLog
    public String newFolder(String parentFolderid, String folderName) {
        if (parentFolderid.equalsIgnoreCase("Cloud")) parentFolderid = "";
        String newFolderId = null;
        if (dropBoxService != null && mConnected && folderName != null) {
            try {
                FolderMetadata newFolder =
                        dropBoxService.files().createFolder(parentFolderid + "/" + folderName);
                newFolderId = newFolder.getId();
            } catch (DbxException e) {
                Crashlytics.logException(e);
            }
        }
        return newFolderId;
    }

    @DebugLog
    public boolean deleteFile(String fileId) {
        if (dropBoxService != null && mConnected && fileId != null) {
            try {
                dropBoxService.files().delete(fileId);
            } catch (DbxException e) {
                Crashlytics.logException(e);
                return false;
            }
        }
        return true;
    }

    @DebugLog
    public interface DropBoxConnectionCallbacks {
        void onDropBoxConnectionFailed(Exception ex);

        void onDropBoxConnectionFailed();

        void onDropBoxConnected(int cloudAccountId);

        void onDropBoxConnectionStarted();
    }

    public class DropBoxAirCycleListener implements ActivityAirCycle {

        @Override
        public void onCreate() {
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onResume() {
            if (started) {
                String accessToken = Auth.getOAuth2Token();
                if (accessToken != null) {
                    create(Auth.getOAuth2Token());
                } else {
                    if (dropBoxConnectionCallbacks != null) {
                        dropBoxConnectionCallbacks.onDropBoxConnectionFailed();
                    }
                }
            }
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onSaveInstanceState() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
