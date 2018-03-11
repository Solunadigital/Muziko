package com.muziko.cloud.OneDrive;

import android.app.Activity;

import com.crashlytics.android.Crashlytics;
import com.muziko.BuildConfig;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.CloudHelper;
import com.muziko.cloud.GenericCloudApi;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.CloudManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.OkHttpManager;
import com.onedrive.sdk.authentication.MSAAuthenticator;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.extensions.Folder;
import com.onedrive.sdk.extensions.IItemCollectionPage;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.Item;
import com.onedrive.sdk.extensions.OneDriveClient;
import com.onedrive.sdk.logger.LoggerLevel;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import hugo.weaving.DebugLog;
import retrofit2.Call;
import retrofit2.Callback;

import static com.muziko.manager.CloudManager.connectedCloudDrives;
import static com.muziko.manager.MuzikoConstants.extensions;

/**
 * Created by Bradley on 8/08/2017.
 */

public class OneDriveApi extends GenericCloudApi implements ICallback<IOneDriveClient> {

    private final AtomicReference<IOneDriveClient> oneDriveClient = new AtomicReference<>();
    private IClientConfig iClientConfig;
    private boolean started = false;
    private OneDriveConnectionCallbacks oneDriveConnectionCallbacks;
    private Activity activity;
    private String sharedPrefKey;
    private String newFolderId;

    private IClientConfig getInitialClientConfig() {
        final MSAAuthenticator msaAuthenticator = new MSAAuthenticator() {
            @Override
            public String getClientId() {
                return BuildConfig.OneDriveAppKey;
            }

            @Override
            public String getSharedPref() {
                return sharedPrefKey;
            }

            @Override
            public String[] getScopes() {
                return new String[]{"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access", "wl.emails"};
            }
        };

        iClientConfig = DefaultClientConfig.createWithAuthenticator(msaAuthenticator);
        iClientConfig.getLogger().setLoggingLevel(LoggerLevel.Debug);
        return iClientConfig;
    }

    private IClientConfig getClientConfig() {
        final MSAAuthenticator msaAuthenticator = new MSAAuthenticator() {
            @Override
            public String getClientId() {
                return BuildConfig.OneDriveAppKey;
            }

            @Override
            public String getSharedPref() {
                return sharedPrefKey;
            }

            @Override
            public String[] getScopes() {
                return new String[]{"onedrive.readwrite", "onedrive.appfolder", "wl.offline_access", "wl.emails"};
            }
        };

        iClientConfig = DefaultClientConfig.createWithAuthenticator(msaAuthenticator);
        iClientConfig.getLogger().setLoggingLevel(LoggerLevel.Debug);
        return iClientConfig;
    }

    /**
     * Get an instance of the service
     *
     * @return The Service
     */
    public synchronized IOneDriveClient getOneDriveClient() {
        if (oneDriveClient.get() == null) {
            throw new UnsupportedOperationException("Unable to generate a new service object");
        }
        return oneDriveClient.get();
    }

    public synchronized void initialize(final Activity activity, OneDriveConnectionCallbacks oneDriveConnectionCallbacks) {
        this.activity = activity;
        sharedPrefKey = UUID.randomUUID().toString();
        this.oneDriveConnectionCallbacks = oneDriveConnectionCallbacks;
        if (oneDriveConnectionCallbacks != null) {
            oneDriveConnectionCallbacks.onOneDriveConnectionStarted();
        }
        new OneDriveClient
                .Builder()
                .fromConfig(getInitialClientConfig())
                .loginAndBuildClient(activity, this);
    }

    public synchronized void connect(final Activity activity, String email, String sharedPrefKey, OneDriveConnectionCallbacks oneDriveConnectionCallbacks) {
        this.activity = activity;
        this.sharedPrefKey = sharedPrefKey;
        this.oneDriveConnectionCallbacks = oneDriveConnectionCallbacks;
        new OneDriveClient
                .Builder()
                .fromConfig(getClientConfig())
                .loginExistingAndBuildClient(activity, email, this);
    }

    @Override
    public void success(IOneDriveClient iOneDriveClient) {
        started = true;
        oneDriveClient.set(iOneDriveClient);
        String accessToken = oneDriveClient.get().getAuthenticator().getAccountInfo().getAccessToken();
        getUserData(accessToken);
    }

    private void getUserData(String accessToken) {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("access_token", accessToken);

        Call<UserData> userDataCall = OkHttpManager.Instance().getOneDriveRestApi().getUserData(hashMap);

        userDataCall.enqueue(
                new Callback<UserData>() {
                    @Override
                    public void onResponse(Call<UserData> call, retrofit2.Response<UserData> rawResponse) {
                        try {

                            if (rawResponse.isSuccessful()) {
                                mConnected = true;
                                accountName = rawResponse.body().getEmails().getAccount() == null ? rawResponse.body().getId() : rawResponse.body().getEmails().getAccount();
                                cloudAccountId = CloudAccountRealmHelper.insert(accountName, accessToken, CloudManager.ONEDRIVE, false, sharedPrefKey);
                                connectedCloudDrives.add(cloudAccountId);
                                CloudManager.Instance().addOneDriveApi(accountName, OneDriveApi.this);
                                if (oneDriveConnectionCallbacks != null) {
                                    oneDriveConnectionCallbacks.onOneDriveConnected(cloudAccountId);
                                }
//                                recursiveSearch("audio");
                                searchRecursive();
                            }


                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            mConnected = false;
                            if (oneDriveConnectionCallbacks != null) {
                                oneDriveConnectionCallbacks.onOneDriveConnectionFailed(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserData> call, Throwable throwable) {
                        Crashlytics.logException(throwable);
                        mConnected = false;
                        if (oneDriveConnectionCallbacks != null) {
                            oneDriveConnectionCallbacks.onOneDriveConnectionFailed(throwable.getMessage());
                        }
                    }
                });
    }


    @DebugLog
    public boolean deleteFile(String fileId) {
        if (mConnected && fileId != null) {
            try {
                getOneDriveClient().getDrive().getItems(fileId).buildRequest().delete();
            } catch (Exception e) {
                Crashlytics.logException(e);
                return false;
            }
        }
        return true;
    }

    @DebugLog
    public String getUrl(String fileId) {
        String url = "";
        if (mConnected && fileId != null) {
            try {
                Item item = getOneDriveClient().getDrive().getItems(fileId).buildRequest().get();
                url = item.getRawObject().get("@content.downloadUrl").getAsString();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        return url;
    }

    public Item getFile(String fileId) {
        Item item = null;
        if (oneDriveClient != null && mConnected && fileId != null) {
            try {
                item = getOneDriveClient().getDrive().getItems(fileId).buildRequest().get();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        return item;
    }

    public ArrayList<QueueItem> getFolderPath(QueueItem queueItem) {
        ArrayList<QueueItem> folderPath = new ArrayList<>();
        ArrayList<String> parentFiles = new ArrayList<>();

//        Item item = getFile(queueItem.folder_path);
//        ParentReference parent = item.getParents().get(0);
//        parentFiles.add(parent.getId());
//        while (!parent.getIsRoot()) {
//            item = getFile(parent.getId());
//            parent = item.getParents().get(0);
//            parentFiles.add(parent.getId());
//        }

        for (String parentId : parentFiles) {
            folderPath.add(CloudHelper.getFolderItem(cloudAccountId, accountName, queueItem, getFile(parentId)));
        }

        Collections.reverse(folderPath);
        return folderPath;

    }


    public void searchRecursive() {
        if (mConnected) {
            getOneDriveClient().getDrive().getRoot().getChildren().buildRequest().get(new ICallback<IItemCollectionPage>() {
                @Override
                public void success(IItemCollectionPage iItemCollectionPage) {
                    LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                    LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                    List<Item> oneDriveItems = iItemCollectionPage.getCurrentPage();
                    for (Item item : oneDriveItems) {
                        if (item.file != null && item.audio != null) {
                            String ext = FilenameUtils.getExtension(item.name);
                            if (Arrays.asList(extensions).contains(ext)) {
                                QueueItem queueItem = new QueueItem();
                                queueItem.data = item.id;
                                queueItem.cloudId = item.id;
                                queueItem.folder_path = "root";
                                queueItem.album = CloudManager.ONEDRIVE;
                                if (item.audio.title == null || item.audio.title.isEmpty()) {
                                    queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                                } else {
                                    queueItem.title = item.audio.title;
                                }
                                queueItem.artist_name = item.audio.artist;
                                queueItem.album_name = item.audio.album;
                                queueItem.genre_name = item.audio.genre;
                                queueItem.track = item.audio.track != null ? item.audio.track : 0;
                                queueItem.year = item.audio.year != null ? item.audio.year : 0;
                                queueItem.storage = cloudAccountId;
                                queueItem.size = item.size;
                                queueItem.date = item.createdDateTime.getTimeInMillis();
                                queueItem.dateModified = item.lastModifiedDateTime.getTimeInMillis();
                                allCloudTracks.put(queueItem.data, queueItem);
                                boolean trackExists = TrackRealmHelper.trackExists(item.id, cloudAccountId);
                                if (!trackExists) {
                                    queueItems.put(queueItem.data, queueItem);
                                }
                            }
                        } else if (item.folder != null) {
                            searchRecursiveSubFolder(item.id);
                        }
                    }
                    boolean shouldRefresh = false;
                    if (queueItems.size() > 0) {
                        if (TrackRealmHelper.insertCloudList(cloudAccountId, queueItems)) {
                            shouldRefresh = true;
                        }
                    }
//                    if (allCloudTracks.size() > 0) {
//                        if (TrackRealmHelper.removedCloudTracks(cloudAccountId, allCloudTracks)) {
//                            shouldRefresh = true;
//                        }
//                    }
                    if (shouldRefresh) {
                        EventBus.getDefault().post(new RefreshEvent(1000));
                    }

                }

                @Override
                public void failure(ClientException ex) {
                    Crashlytics.logException(ex);
                }
            });

        }
    }

    public void searchRecursiveSubFolder(String folderId) {
        if (mConnected && folderId != null) {
            getOneDriveClient().getDrive().getItems(folderId).getChildren().buildRequest().get(new ICallback<IItemCollectionPage>() {
                @Override
                public void success(IItemCollectionPage iItemCollectionPage) {
                    LinkedHashMap<String, QueueItem> queueItems = new LinkedHashMap<>();
                    LinkedHashMap<String, QueueItem> allCloudTracks = new LinkedHashMap<>();
                    List<Item> oneDriveItems = iItemCollectionPage.getCurrentPage();
                    for (Item item : oneDriveItems) {
                        if (item.file != null && item.audio != null) {
                            String ext = FilenameUtils.getExtension(item.name);
                            if (Arrays.asList(extensions).contains(ext)) {
                                QueueItem queueItem = new QueueItem();
                                queueItem.data = item.id;
                                queueItem.cloudId = item.id;
                                queueItem.folder_path = folderId;
                                queueItem.album = CloudManager.ONEDRIVE;
                                if (item.audio.title == null || item.audio.title.isEmpty()) {
                                    queueItem.title = MuzikoConstants.UNKNOWN_TITLE;
                                } else {
                                    queueItem.title = item.audio.title;
                                }
                                queueItem.artist_name = item.audio.artist;
                                queueItem.album_name = item.audio.album;
                                queueItem.genre_name = item.audio.genre;
                                queueItem.track = item.audio.track != null ? item.audio.track : 0;
                                queueItem.year = item.audio.year != null ? item.audio.year : 0;
                                queueItem.storage = cloudAccountId;
                                queueItem.size = item.size;
                                queueItem.date = item.createdDateTime.getTimeInMillis();
                                queueItem.dateModified = item.lastModifiedDateTime.getTimeInMillis();
                                allCloudTracks.put(queueItem.data, queueItem);
                                boolean trackExists = TrackRealmHelper.trackExists(item.id, cloudAccountId);
                                if (!trackExists) {
                                    queueItems.put(queueItem.data, queueItem);
                                }
                            }
                        } else if (item.folder != null) {
                            searchRecursiveSubFolder(item.id);
                        }
                    }
                    boolean shouldRefresh = false;
                    if (queueItems.size() > 0) {
                        if (TrackRealmHelper.insertCloudList(cloudAccountId, queueItems)) {
                            shouldRefresh = true;
                        }
                    }
//                    if (allCloudTracks.size() > 0) {
//                        if (TrackRealmHelper.removedCloudTracks(cloudAccountId, allCloudTracks)) {
//                            shouldRefresh = true;
//                        }
//                    }
                    if (shouldRefresh) {
                        EventBus.getDefault().post(new RefreshEvent(1000));
                    }
                    CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                }

                @Override
                public void failure(ClientException ex) {
                    Crashlytics.logException(ex);
                    CloudAccountRealmHelper.refreshedCloudAccount(cloudAccountId);
                }
            });

        }
    }

    public void getRootFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacks) {

        if (mConnected && queueItem != null) {
            getOneDriveClient().getDrive().getRoot().getChildren().buildRequest().get(new ICallback<IItemCollectionPage>() {
                @Override
                public void success(IItemCollectionPage iItemCollectionPage) {
                    ArrayList<QueueItem> fileExplorerItems = new ArrayList<>();
                    List<Item> oneDrveItems = iItemCollectionPage.getCurrentPage();
                    for (Item item : oneDrveItems) {
                        if (item.file != null && item.audio != null) {
                            String ext = FilenameUtils.getExtension(item.name);
                            if (Arrays.asList(extensions).contains(ext)) {
                                fileExplorerItems.add(CloudHelper.getFileItem(cloudAccountId, accountName, queueItem, item));
                            }
                        } else if (item.folder != null) {
                            fileExplorerItems.add(CloudHelper.getFolderItem(cloudAccountId, accountName, queueItem, item));
                        }
                    }

                    Collections.sort(fileExplorerItems, (p1, p2) -> {
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

                    cloudFolderCallbacks.onFoldersReturned(fileExplorerItems);
                }

                @Override
                public void failure(ClientException ex) {

                }
            });

        } else {
            cloudFolderCallbacks.onFoldersFailed();
        }
    }

    public void getFolderItems(QueueItem queueItem, CloudFolderCallbacks cloudFolderCallbacks) {
        if (mConnected && queueItem != null) {
            getOneDriveClient().getDrive().getItems(queueItem.data).getChildren().buildRequest().get(new ICallback<IItemCollectionPage>() {
                @Override
                public void success(IItemCollectionPage iItemCollectionPage) {
                    ArrayList<QueueItem> fileExplorerItems = new ArrayList<>();
                    List<Item> oneDrveItems = iItemCollectionPage.getCurrentPage();
                    for (Item item : oneDrveItems) {
                        if (item.file != null && item.audio != null) {
                            String ext = FilenameUtils.getExtension(item.name);
                            if (Arrays.asList(extensions).contains(ext)) {
                                fileExplorerItems.add(CloudHelper.getFileItem(cloudAccountId, accountName, queueItem, item));
                            }
                        } else if (item.folder != null) {
                            fileExplorerItems.add(CloudHelper.getFolderItem(cloudAccountId, accountName, queueItem, item));
                        }
                    }

                    Collections.sort(fileExplorerItems, (p1, p2) -> {
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

                    cloudFolderCallbacks.onFoldersReturned(fileExplorerItems);
                }

                @Override
                public void failure(ClientException ex) {

                }
            });

        } else {
            cloudFolderCallbacks.onFoldersFailed();
        }
    }

    @DebugLog
    public String newFolder(String parentFolderid, String folderName) {

        newFolderId = null;
        final Item folderToCreate = new Item();
        folderToCreate.name = folderName;
        folderToCreate.folder = new Folder();
        if (mConnected && folderName != null) {
            try {
                if (parentFolderid.equalsIgnoreCase("Cloud")) {
                    getOneDriveClient().getDrive().getRoot().getChildren().buildRequest().create(folderToCreate, new ICallback<Item>() {
                        @Override
                        public void success(Item item) {
                            newFolderId = item.id;
                        }

                        @Override
                        public void failure(ClientException ex) {
                            Crashlytics.logException(ex);
                        }
                    });
                } else {
                    getOneDriveClient().getDrive().getItems(parentFolderid).getChildren().buildRequest().create(folderToCreate, new ICallback<Item>() {
                        @Override
                        public void success(Item item) {
                            newFolderId = item.id;
                        }

                        @Override
                        public void failure(ClientException ex) {
                            Crashlytics.logException(ex);
                        }
                    });
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        return newFolderId;
    }

    @Override
    public void failure(ClientException ex) {
        started = false;
        Crashlytics.logException(ex);
        if (oneDriveConnectionCallbacks != null) {
            oneDriveConnectionCallbacks.onOneDriveConnectionFailed(ex);
        }
    }

    @DebugLog
    public interface OneDriveConnectionCallbacks {
        void onOneDriveConnectionFailed(String message);

        void onOneDriveConnectionFailed(Exception ex);

        void onOneDriveConnected(int cloudAccountId);

        void onOneDriveConnectionStarted();
    }


}
