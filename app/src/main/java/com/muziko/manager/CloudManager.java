package com.muziko.manager;

import android.app.Activity;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.muziko.cloud.Amazon.AmazonApi;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.GenericCloudApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.cloud.myApi.MyApi;
import com.muziko.common.models.CloudUrl;
import com.muziko.common.models.QueueItem;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.CloudUrlRealmHelper;
import com.muziko.events.StreamProgressEvent;
import com.muziko.models.CloudAccount;
import com.muziko.tasks.CloudDownloadTask;
import com.muziko.tasks.CloudUploadTask;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import hugo.weaving.DebugLog;

/**
 * Created by Bradley on 7/05/2017.
 */
public class CloudManager
        implements DriveApi.DriveConnectionCallbacks,
        BoxApi.BoxConnectionCallbacks,
        DropBoxApi.DropBoxConnectionCallbacks, OneDriveApi.OneDriveConnectionCallbacks {

    public static final int LOCAL = 10000;
    public static final int GOOGLEDRIVE = 10001;
    public static final int DROPBOX = 10002;
    public static final int BOX = 10003;
    public static final int ONEDRIVE = 10004;
    public static final int AMAZON = 10005;
    public static final int FIREBASE = 10006;
    public static HashMap<String, CloudDownloadTask> cloudDownloadList = new HashMap<>();
    public static HashMap<String, CloudUploadTask> cloudUploaderList = new HashMap<>();
    public static ArrayList<Integer> connectedCloudDrives = new ArrayList<>();
    private static CloudManager instance;
    private HashMap<String, DriveApi> driveApiList = new HashMap<>();
    private HashMap<String, DropBoxApi> dropBoxApiList = new HashMap<>();
    private HashMap<String, BoxApi> boxApiList = new HashMap<>();
    private HashMap<String, OneDriveApi> oneDriveApiList = new HashMap<>();
    private HashMap<String, AmazonApi> amazonApiList = new HashMap<>();
    private String downloadPath;
    private Context context;
    private boolean isSyncing = false;
    private WeakReference<Activity> activityWeakReference;
    private MyApi muzikoCloud;

    private CloudManager() {
    }

    public static CloudManager Instance() {
        if (instance == null) instance = new CloudManager();
        return instance;
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    public void setSyncing(boolean syncing) {
        isSyncing = syncing;
    }

    @DebugLog
    public void downloadTrack(QueueItem queueItem) {

        CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(queueItem.storage);
        switch (cloudAccount.getCloudProvider()) {
            case (GOOGLEDRIVE):
                DriveApi driveApi = (DriveApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask driveDownloadTask = new CloudDownloadTask(context, driveApi.getDriveService(), queueItem, downloadPath);
                cloudDownloadList.put(queueItem.folder_path, driveDownloadTask);
                driveDownloadTask.execute();

                break;

            case (DROPBOX):
                DropBoxApi dropBoxApi = (DropBoxApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask dropBoxDownloadTask = new CloudDownloadTask(context, dropBoxApi.getDropBoxService(), queueItem);
                cloudDownloadList.put(queueItem.folder_path, dropBoxDownloadTask);
                dropBoxDownloadTask.execute();

                break;

            case (BOX):
                BoxApi boxApi = (BoxApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask boxDownloadTask =
                        new CloudDownloadTask(context, boxApi.getBoxService(), queueItem);
                cloudDownloadList.put(queueItem.folder_path, boxDownloadTask);
                boxDownloadTask.execute();

                break;

            case (ONEDRIVE):
                OneDriveApi oneDriveApi = (OneDriveApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask oneDriveDownloadTask = new CloudDownloadTask(context, oneDriveApi.getOneDriveClient(), queueItem);
                cloudDownloadList.put(queueItem.folder_path, oneDriveDownloadTask);
                oneDriveDownloadTask.execute();

                break;

            case (AMAZON):
                AmazonApi amazonApi = (AmazonApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask amazonDownloadTask = new CloudDownloadTask(context, amazonApi.getOneDriveClient(), queueItem);
                cloudDownloadList.put(queueItem.folder_path, amazonDownloadTask);
                amazonDownloadTask.execute();

                break;
        }
    }

    @DebugLog
    public void downloadTrack(QueueItem queueItem, String downloadPath) {

        this.downloadPath = downloadPath;
        CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(queueItem.storage);
        switch (cloudAccount.getCloudProvider()) {
            case (GOOGLEDRIVE):
                DriveApi driveApi = (DriveApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask driveDownloadTask = new CloudDownloadTask(context, driveApi.getDriveService(), queueItem, downloadPath);
                cloudDownloadList.put(queueItem.folder_path, driveDownloadTask);
                driveDownloadTask.execute();

                break;

            case (DROPBOX):
                DropBoxApi dropBoxApi = (DropBoxApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask dropBoxDownloadTask = new CloudDownloadTask(context, dropBoxApi.getDropBoxService(), queueItem, downloadPath);
                cloudDownloadList.put(queueItem.folder_path, dropBoxDownloadTask);
                dropBoxDownloadTask.execute();

                break;

            case (BOX):
                BoxApi boxApi = (BoxApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask boxDownloadTask = new CloudDownloadTask(context, boxApi.getBoxService(), queueItem, downloadPath);
                cloudDownloadList.put(queueItem.folder_path, boxDownloadTask);
                boxDownloadTask.execute();

                break;

            case (ONEDRIVE):
                OneDriveApi oneDriveApi = (OneDriveApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask oneDriveDownloadTask = new CloudDownloadTask(context, oneDriveApi.getOneDriveClient(), queueItem, downloadPath);
                cloudDownloadList.put(queueItem.folder_path, oneDriveDownloadTask);
                oneDriveDownloadTask.execute();

                break;

            case (AMAZON):
                AmazonApi amazonApi = (AmazonApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                CloudDownloadTask amazonDownloadTask = new CloudDownloadTask(context, amazonApi.getOneDriveClient(), queueItem, downloadPath);
                cloudDownloadList.put(queueItem.folder_path, amazonDownloadTask);
                amazonDownloadTask.execute();

                break;
        }
    }

    public void addDriveApi(String accountName, DriveApi driveApi) {
        driveApiList.put(accountName, driveApi);
    }

    public void removeDriveApi(String accountName) {
        driveApiList.remove(accountName);
    }

    public void addDropBoxApi(String accountName, DropBoxApi dropBoxApi) {
        dropBoxApiList.put(accountName, dropBoxApi);
    }

    public void removeDropBoxApi(String accountName) {
        dropBoxApiList.remove(accountName);
    }

    public void addBoxApi(String accountName, BoxApi boxApi) {
        boxApiList.put(accountName, boxApi);
    }

    public void removeBoxApi(String accountName) {
        boxApiList.remove(accountName);
    }

    public void addOneDriveApi(String accountName, OneDriveApi oneDriveApi) {
        oneDriveApiList.put(accountName, oneDriveApi);
    }

    public void removeOneDriveApi(String accountName) {
        oneDriveApiList.remove(accountName);
    }

    public void addAmazonApi(String accountName, AmazonApi amazonApi) {
        amazonApiList.put(accountName, amazonApi);
    }

    public void removeAmazonApi(String accountName) {
        amazonApiList.remove(accountName);
    }

    public void refreshCloudDrives() {
        CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccountToUpdate();
        switch (cloudAccount.getCloudProvider()) {
            case GOOGLEDRIVE:
                DriveApi driveApi = driveApiList.get(cloudAccount.getAccountName());
                if (driveApi != null) {
                    driveApi.searchAudio();
                }
                break;
            case DROPBOX:
                DropBoxApi dropBoxApi = dropBoxApiList.get(cloudAccount.getAccountName());
                if (dropBoxApi != null) {
                    dropBoxApi.search("mp3");
                }
                break;
            case BOX:
                BoxApi boxApi = boxApiList.get(cloudAccount.getAccountName());
                if (boxApi != null) {
                    boxApi.search("mp3");
                }
                break;
            case ONEDRIVE:
                OneDriveApi oneDriveApi = oneDriveApiList.get(cloudAccount.getAccountName());
                if (oneDriveApi != null) {
                    oneDriveApi.searchRecursive();
                }
                break;
        }

    }

    @DebugLog
    public GenericCloudApi getCloudDrive(Context context, int cloudAccountId) {
        CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(cloudAccountId);
        switch (cloudAccount.getCloudProvider()) {
            case GOOGLEDRIVE:
                DriveApi driveApi = driveApiList.get(cloudAccount.getAccountName());
                if (driveApi == null) {
                    driveApi = new DriveApi();
                    driveApi.initialize(context, cloudAccount.getCloudAccountId(), cloudAccount.getAccountName());
                    driveApi.connect(CloudManager.this);
                    return null;
                } else {
                    return driveApi;
                }
            case DROPBOX:
                DropBoxApi dropBoxApi = dropBoxApiList.get(cloudAccount.getAccountName());
                if (dropBoxApi == null) {
                    dropBoxApi = new DropBoxApi();
                    dropBoxApi.createBasicListener(
                            context, cloudAccount.getCloudAccountId(), this);
                    dropBoxApi.connect(cloudAccount.getAccessToken());
                    return null;
                } else {
                    return dropBoxApi;
                }
            case BOX:
                BoxApi boxApi = boxApiList.get(cloudAccount.getAccountName());
                if (boxApi == null) {
                    boxApi = new BoxApi();
                    boxApi.initialize(
                            context,
                            cloudAccount.getCloudAccountId(),
                            cloudAccount.getAccountName(),
                            cloudAccount.getAccessToken(),
                            this);
                    return null;
                } else {
                    return boxApi;
                }

            case ONEDRIVE:
                OneDriveApi oneDriveApi = oneDriveApiList.get(cloudAccount.getAccountName());
                if (oneDriveApi == null) {
                    oneDriveApi = new OneDriveApi();
                    oneDriveApi.connect(activityWeakReference.get(), cloudAccount.getAccountName(), cloudAccount.getSharedPrefKey(), this);
                    return null;
                } else {
                    return oneDriveApi;
                }

            case AMAZON:
                AmazonApi amazonApi = amazonApiList.get(cloudAccount.getAccountName());
                if (amazonApi == null) {
                    amazonApi = new AmazonApi();
                    amazonApi.connect(activityWeakReference.get(), cloudAccount.getAccountName(), cloudAccount.getSharedPrefKey(), this);
                    return null;
                } else {
                    return amazonApi;
                }
        }

        return null;
    }

    @DebugLog
    public void streamBoxSong(Context context, QueueItem queueItem) {
        try {
            CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(queueItem.storage);
            switch (cloudAccount.getCloudProvider()) {
                case BOX:
                    EventBus.getDefault().post(new StreamProgressEvent(0));
                    BoxApi boxApi = (BoxApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                    boxApi.streamFile(queueItem.data);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public String getCloudFileUrl(Context context, QueueItem queueItem) {
        String url = "";
        CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(queueItem.storage);
        CloudUrl cloudUrl = null;
        switch (cloudAccount.getCloudProvider()) {
            case DROPBOX:
                cloudUrl = CloudUrlRealmHelper.getCloudUrl(queueItem.data, cloudAccount.getCloudProvider());
                if (cloudUrl != null) {
                    url = cloudUrl.getUrl();
                } else {
                    DropBoxApi dropBoxApi = (DropBoxApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                    url = dropBoxApi.getUrl(queueItem.data);
                    CloudUrlRealmHelper.insertCloudUrl(queueItem.data, cloudAccount.getCloudProvider(), url);
                }

                break;

            case ONEDRIVE:
                cloudUrl = CloudUrlRealmHelper.getCloudUrl(queueItem.data, cloudAccount.getCloudProvider());
                if (cloudUrl != null) {
                    url = cloudUrl.getUrl();
                } else {
                    OneDriveApi oneDriveApi = (OneDriveApi) getCloudDrive(context, cloudAccount.getCloudAccountId());
                    url = oneDriveApi.getUrl(queueItem.data);
                    CloudUrlRealmHelper.insertCloudUrl(queueItem.data, cloudAccount.getCloudProvider(), url);
                }

                break;
        }

        return url;
    }

    public MyApi getMuzikoCloud() {
        return muzikoCloud;
    }

    public void initActivityReference(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    public void init(Context context) {
        this.context = context;
        if (muzikoCloud == null) {  // Only do this once

//            begin options for devappserver

//            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
//                    new AndroidJsonFactory(), null)
//                    // options for running against local devappserver
//                    // - 10.0.2.2 is localhost's IP address in Android emulator
//                    // - turn off compression when running against local devappserver
//                    .setRootUrl("http://10.1.1.100:8080/_ah/api/")
//                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
//                        @Override
//                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
//                            abstractGoogleClientRequest.setDisableGZipContent(true);
//                        }
//                    });

//           end options for devappserver
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl("https://muziko-48de4.appspot.com/_ah/api/");

            muzikoCloud = builder.build();
        }
    }

    @Override
    public void onDriveConnectionFailed(Exception ex) {
        Crashlytics.logException(ex);




    }

    @Override
    public void onDriveConnected(int cloudAccountId) {
    }

    @Override
    public void onBoxConnectionFailed(Exception ex) {
        Crashlytics.logException(ex);
    }

    @Override
    public void onBoxConnectionFailed() {
    }

    @Override
    public void onBoxConnected(int cloudAccountId) {
    }

    @Override
    public void onBoxConnectionStarted() {

    }

    @Override
    public void onDropBoxConnectionFailed(Exception ex) {
        Crashlytics.logException(ex);
    }

    @Override
    public void onDropBoxConnectionFailed() {
    }

    @Override
    public void onDropBoxConnected(int cloudAccountId) {
    }

    @Override
    public void onDropBoxConnectionStarted() {

    }

    @Override
    public void onOneDriveConnectionFailed(String message) {

    }

    @Override
    public void onOneDriveConnectionFailed(Exception ex) {

    }

    @Override
    public void onOneDriveConnected(int cloudAccountId) {

    }

    @Override
    public void onOneDriveConnectionStarted() {

    }

    public enum StorageMode {
        DRIVES,
        LOCAL,
        DRIVE,
        DROPBOX,
        BOX,
        ONEDRIVE,
        AMAZON
    }
}
