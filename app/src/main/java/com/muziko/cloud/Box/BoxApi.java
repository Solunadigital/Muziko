package com.muziko.cloud.Box;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import com.arasthel.asyncjob.AsyncJob;
import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxApiSearch;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxDownload;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.crashlytics.android.Crashlytics;
import com.muziko.BuildConfig;
import com.muziko.R;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.CloudHelper;
import com.muziko.cloud.GenericCloudApi;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.StreamProgressEvent;
import com.muziko.helpers.FileHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.ThreadManager;
import com.oasisfeng.condom.CondomContext;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import hugo.weaving.DebugLog;

import static com.muziko.manager.CloudManager.connectedCloudDrives;
import static com.muziko.manager.MuzikoConstants.boxCacheBytes;
import static com.muziko.manager.MuzikoConstants.boxCachePercent;
import static com.muziko.manager.MuzikoConstants.extensions;

public class BoxApi extends GenericCloudApi {

    private BoxConnectionCallbacks boxConnectionCallbacks;
    private BoxSession boxService = null;
    private boolean streamStarted = false;
    private BoxDownload boxDownload;
    private BoxApiSearch boxApiSearch;
    private BoxApiFile boxApiFile;
    private BoxApiFolder boxApiFolder;

    @DebugLog
    public void initialize(Context context, String accessToken, BoxConnectionCallbacks boxConnectionCallbacks) {

        BoxConfig.IS_LOG_ENABLED = false;
        BoxConfig.CLIENT_ID = BuildConfig.BoxAppKey;
        BoxConfig.CLIENT_SECRET = BuildConfig.BoxAppSecret;

        this.context = context;
        this.boxConnectionCallbacks = boxConnectionCallbacks;

        boxService = new BoxSession(CondomContext.wrap(context, "Box"), accessToken);
        boxService.setSessionAuthListener(
                new BoxAuthentication.AuthListener() {
                    @Override
                    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
                        mConnected = true;
                        if (CloudAccountRealmHelper.getCloudAccount(accountName, CloudManager.GOOGLEDRIVE) != null) {
                            AppController.toast(context, context.getString(R.string.cloud_account_already_added));
                        }
                        cloudAccountId =
                                CloudAccountRealmHelper.insert(
                                        info.getUser().getName(),
                                        info.getUser().getId(),
                                        CloudManager.BOX,
                                        false);
                        accountName = info.getUser().getName();
                        CloudManager.Instance().addBoxApi(info.getUser().getName(), BoxApi.this);
                        connectedCloudDrives.add(cloudAccountId);
                        if (boxConnectionCallbacks != null) {
                            boxConnectionCallbacks.onBoxConnected(cloudAccountId);
                        }
                        boxApiSearch = new BoxApiSearch(boxService);
                        boxApiFile = new BoxApiFile(boxService);
                        boxApiFolder = new BoxApiFolder(boxService);
                        search("mp3");
                    }

                    @Override
                    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
                        mConnected = true;
                        cloudAccountId =
                                CloudAccountRealmHelper.insert(
                                        info.getUser().getName(),
                                        info.getUser().getId(),
                                        CloudManager.BOX,
                                        false);
                        accountName = info.getUser().getName();
                        CloudManager.Instance().addBoxApi(info.getUser().getName(), BoxApi.this);
                        connectedCloudDrives.add(cloudAccountId);
                        if (boxConnectionCallbacks != null) {
                            if (mConnected) {
                                boxConnectionCallbacks.onBoxConnected(cloudAccountId);
                            } else { // null indicates general error (fatal)
                                boxConnectionCallbacks.onBoxConnectionFailed();
                            }
                        }
                        boxApiSearch = new BoxApiSearch(boxService);
                        boxApiFile = new BoxApiFile(boxService);
                        boxApiFolder = new BoxApiFolder(boxService);
                        search("mp3");
                    }

                    @Override
                    public void onAuthFailure(
                            BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                        if (boxConnectionCallbacks != null) {
                            boxConnectionCallbacks.onBoxConnectionFailed(ex);
                        }
                    }

                    @Override
                    public void onLoggedOut(
                            BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                        if (boxConnectionCallbacks != null) {
                            boxConnectionCallbacks.onBoxConnectionFailed(ex);
                        }
                    }
                });
        boxService.authenticate(context);
        if (boxConnectionCallbacks != null) {
            boxConnectionCallbacks.onBoxConnectionStarted();
        }
    }

    @DebugLog
    public void initialize(
            Context context,
            int cloudAccountId,
            String accountName,
            String accessToken,
            BoxConnectionCallbacks boxConnectionCallbacks) {

        BoxConfig.IS_LOG_ENABLED = true;
        BoxConfig.CLIENT_ID = BuildConfig.BoxAppKey;
        BoxConfig.CLIENT_SECRET = BuildConfig.BoxAppSecret;

        this.context = context;
        this.cloudAccountId = cloudAccountId;
        this.accountName = accountName;
        this.boxConnectionCallbacks = boxConnectionCallbacks;

        boxService = new BoxSession(CondomContext.wrap(context, "Box"), accessToken);
        boxService.setSessionAuthListener(
                new BoxAuthentication.AuthListener() {
                    @Override
                    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
                        mConnected = true;
                        CloudManager.Instance().addBoxApi(info.getUser().getName(), BoxApi.this);
                        connectedCloudDrives.add(cloudAccountId);
                        if (boxConnectionCallbacks != null) {
                            boxConnectionCallbacks.onBoxConnected(cloudAccountId);
                        }
                        boxApiSearch = new BoxApiSearch(boxService);
                        boxApiFile = new BoxApiFile(boxService);
                        boxApiFolder = new BoxApiFolder(boxService);
                        search("mp3");
                    }

                    @Override
                    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {

                        mConnected = true;
                        CloudManager.Instance().addBoxApi(info.getUser().getName(), BoxApi.this);
                        connectedCloudDrives.add(cloudAccountId);
                        if (boxConnectionCallbacks != null) {
                            if (mConnected) {
                                boxConnectionCallbacks.onBoxConnected(cloudAccountId);
                            } else { // null indicates general error (fatal)
                                boxConnectionCallbacks.onBoxConnectionFailed();
                            }
                        }
                        boxApiSearch = new BoxApiSearch(boxService);
                        boxApiFile = new BoxApiFile(boxService);
                        boxApiFolder = new BoxApiFolder(boxService);
                        search("mp3");
                    }

                    @Override
                    public void onAuthFailure(
                            BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                        if (boxConnectionCallbacks != null) {
                            boxConnectionCallbacks.onBoxConnectionFailed(ex);
                        }
                    }

                    @Override
                    public void onLoggedOut(
                            BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
                        if (boxConnectionCallbacks != null) {
                            boxConnectionCallbacks.onBoxConnectionFailed(ex);
                        }
                    }
                });
        boxService.authenticate(context);
    }

    @DebugLog
    public void search(String searchString) {
        AsyncJob.doInBackground(() -> {
            BoxIteratorItems searchResults = null;
            if (boxService != null && mConnected)
                try {

                    searchResults =
                            boxApiSearch
                                    .getSearchRequest(searchString)
                                    .setOffset(0) // default is 0
                                    .setLimit(200) // default is 30, max is 200
                                    // Optional: Specify advanced recursiveSearch parameters. See BoxRequestsSearch.Search for the full list of parameters supported.
                                    //                .limitAncestorFolderIds(new String[]{"folderId1", "folderId2"}) // only items in these folders will be returned.
                                    .limitFileExtensions(extensions)
                                    .send();
                } catch (BoxException e) {
                    Crashlytics.logException(e);
                }

            if (searchResults != null) {
                processSearchResults(searchResults);
            }
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    @DebugLog
    public String getUrl(String fileId) {
        if (boxService != null && mConnected && fileId != null) {
            try {
                BoxFile updatedFile =
                        boxApiFile
                                .getCreateSharedLinkRequest(fileId)
                                .setAccess(BoxSharedLink.Access.OPEN)
                                .setCanDownload(true)
                                .send();

                return updatedFile.getSharedLink().getURL();
            } catch (BoxException e) {
                Crashlytics.logException(e);
            }
        }
        return null;
    }


    public ArrayList<QueueItem> getFolderPath(QueueItem queueItem) {
        ArrayList<QueueItem> folderPath = new ArrayList<>();

        BoxItem boxinfo = getFile(queueItem.data);
        for (BoxItem boxItem : boxinfo.getPathCollection()) {
            folderPath.add(
                    CloudHelper.getFolderItem(
                            cloudAccountId,
                            accountName,
                            queueItem,
                            boxItem));
        }

        return folderPath;
    }

    public BoxFile getFile(String fileId) {
        BoxFile boxinfo = null;
        if (boxService != null && mConnected && fileId != null) {
            try {
                boxinfo = boxApiFile.getInfoRequest(fileId).send();
            } catch (BoxException e) {
                Crashlytics.logException(e);
            }
        }
        return boxinfo;
    }

//    public void getParentFolder(BoxItem boxItem) {
//        BoxFile boxinfo = null;
//        try {
//            boxinfo = boxApiFile.getInfoRequest(boxItem.getId()).send();
//        } catch (BoxException e) {
//            Crashlytics.logException(e);
//        }
//        boxParentPath.add(boxinfo);
//
//        if (boxinfo != null) {
//            getParentFolder(boxinfo.getParent());
//        }
//    }

    @DebugLog
    public void getFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacks) {

        AsyncJob.doInBackground(
                () -> {
                    ArrayList<QueueItem> items = new ArrayList<>();

                    if (boxService != null && mConnected && queueItem != null) {
                        try {
                            BoxFolder boxFolder =
                                    boxApiFolder.getFolderWithAllItems(queueItem.data).send();
                            for (BoxItem boxItem : boxFolder.getItemCollection()) {
                                if (boxItem instanceof BoxFolder) {
                                    items.add(
                                            CloudHelper.getFolderItem(
                                                    cloudAccountId,
                                                    accountName,
                                                    queueItem,
                                                    boxItem));
                                } else {
                                    String ext = FilenameUtils.getExtension(boxItem.getName());
                                    if (Arrays.asList(extensions).contains(ext)) {
                                        items.add(
                                                CloudHelper.getFileItem(
                                                        cloudAccountId,
                                                        accountName,
                                                        queueItem,
                                                        boxItem));
                                    }
                                    //                            if (ext.equalsIgnoreCase("mp3")) {
                                    //                                items.add(CloudHelper.getFileItem(cloudAccountId, accountName, queueItem, boxItem));
                                    //                            }
                                }
                            }
                        } catch (BoxException e) {
                            Crashlytics.logException(e);
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
    public void scanAll() {
        scan("0");
    }

    @DebugLog
    private void scan(String folderId) {
        if (boxService != null && mConnected && folderId != null) {
            try {
                BoxFolder folder = boxApiFolder.getFolderWithAllItems(folderId).send();
                processSearchResults(folder.getItemCollection());
            } catch (BoxException e) {
                Crashlytics.logException(e);
            }
            EventBus.getDefault().post(new RefreshEvent(1000));
        }
    }

    @DebugLog
    private void processSearchResults(BoxIteratorItems boxIteratorItems) {
        if (CloudManager.Instance().isSyncing()) return;
        AsyncJob.doInBackground(
                () -> {
                    try {
                        CloudManager.Instance().setSyncing(true);
                        LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                        LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                        for (BoxItem boxItem : boxIteratorItems) {
                            if (boxItem instanceof BoxFolder) {
                                scan(boxItem.getId());
                            } else {
                                String ext = FilenameUtils.getExtension(boxItem.getName());
                                if (Arrays.asList(extensions).contains(ext)) {
                                    BoxFile boxinfo = null;
                                    try {
                                        boxinfo = boxApiFile.getInfoRequest(boxItem.getId()).send();
                                    } catch (BoxException e) {
                                        Crashlytics.logException(e);
                                        CloudManager.Instance().setSyncing(false);
                                    }

                                    QueueItem queueItem = new QueueItem();
                                    queueItem.data = boxItem.getId();
                                    queueItem.cloudId = boxItem.getId();
                                    String folderPath = boxItem.getParent().getId();
                                    for (BoxItem folder : boxinfo.getPathCollection()) {
                                        folderPath = folderPath + "/" + folder.getName();
                                    }
                                    queueItem.folder_path = boxItem.getParent().getId();
                                    queueItem.album = CloudManager.BOX;
                                    if (boxItem.getName() == null || boxItem.getName().isEmpty()) {
                                        queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                                    } else {
                                        queueItem.title = boxItem.getName();
                                    }
                                    queueItem.artist_name = CloudAccountRealmHelper.getCloudAccount(cloudAccountId).getName();
                                    queueItem.storage = cloudAccountId;
                                    queueItem.size = boxItem.getSize();
                                    queueItem.date = boxItem.getCreatedAt().getTime();
                                    queueItem.dateModified = boxItem.getModifiedAt().getTime();

                                    String url = getUrl(boxItem.getId());
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
                                    boolean trackExists = TrackRealmHelper.trackExists(boxItem.getId(), cloudAccountId);
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
                        CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                        CloudManager.Instance().setSyncing(false);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                        CloudManager.Instance().setSyncing(false);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public void streamFile(String fileId) {

        AsyncJob.doInBackground(
                () -> {
                    streamStarted = false;
                    if (boxService != null && mConnected && fileId != null) {
                        try {
                            BoxDownload boxStream = boxApiFile.getDownloadRequest(FileHelper.getStreamSaveFolder(context, fileId), fileId)
                                    // Optional: Set a listener to track download progress.
                                    .setProgressListener(
                                            (numBytes, totalBytes) -> {
                                                float percent = ((float) numBytes / totalBytes * 100);
                                                if (percent > boxCachePercent || numBytes > boxCacheBytes) {
                                                    if (!streamStarted) {
                                                        streamStarted = true;
                                                        EventBus.getDefault().post(new StreamProgressEvent(1));
                                                    }
                                                }
                                            })
                                    .send();
                        } catch (BoxException | IOException e) {
                            Crashlytics.logException(e);
                        }
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public boolean deleteFile(String fileId) {
        if (boxService != null && mConnected && fileId != null) {
            try {
                boxApiFile.getDeleteRequest(fileId).send();
            } catch (BoxException e) {
                Crashlytics.logException(e);
                return false;
            }
        }
        return true;
    }

    @DebugLog
    public String newFolder(String parentFolderid, String folderName) {
        if (parentFolderid.equalsIgnoreCase("Cloud")) parentFolderid = "0";
        String newFolderId = null;
        if (boxService != null && mConnected && folderName != null) {
            try {
                BoxFolder newFolder =
                        boxApiFolder.getCreateRequest(parentFolderid, folderName).send();
                newFolderId = newFolder.getId();
            } catch (BoxException e) {
                Crashlytics.logException(e);
            }
        }
        return newFolderId;
    }

    @DebugLog
    public boolean deleteFolder(String folderid) {
        try {
            boxApiFolder
                    .getDeleteRequest(folderid)
                    // Optional: By default the folder will be deleted including all the files/folders within.
                    // Set 'recursive' to false to only allow for the deletion if the folder is empty.
                    .setRecursive(true)
                    .send();
        } catch (BoxException e) {
            Crashlytics.logException(e);
            return false;
        }
        return true;
    }

    @DebugLog
    public BoxSession getBoxService() {
        return boxService;
    }

    @DebugLog
    public interface BoxConnectionCallbacks {
        void onBoxConnectionFailed(Exception ex);

        void onBoxConnectionFailed();

        void onBoxConnected(int cloudAccountId);

        void onBoxConnectionStarted();
    }
}
