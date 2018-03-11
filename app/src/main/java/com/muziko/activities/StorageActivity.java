package com.muziko.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.ActionBar;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.adapter.StorageAdapter;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.Amazon.AmazonApi;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.CloudHelper;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.StorageFolderRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.DownloadFile;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.SAFHelpers;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.models.CloudAccount;
import com.muziko.models.StorageFolder;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.CloudFolderDelete;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.FolderDelete;
import com.muziko.tasks.LibraryEdit;
import com.muziko.tasks.ScanMediaFilesForPath;
import com.muziko.tasks.TrackDelete;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.muziko.MyApplication.networkState;
import static com.muziko.R.string.storage;
import static com.muziko.manager.CloudManager.StorageMode.DRIVES;
import static com.muziko.manager.CloudManager.StorageMode.LOCAL;
import static com.muziko.manager.CloudManager.connectedCloudDrives;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_STORAGE_ACCESS;
import static com.muziko.manager.MuzikoConstants.extensions;
import static com.muziko.objects.MenuObject.ADD_TO_LIBRARY;
import static com.muziko.objects.MenuObject.DOWNLOAD;

public class StorageActivity extends BaseActivity
        implements RecyclerItemListener,
        ActionMode.Callback,
        SearchView.OnCloseListener,
        SearchView.OnQueryTextListener,
        View.OnClickListener,
        MaterialMenuAdapter.Callback,
        CloudFolderCallbacks {
    int scrollPosition=0;
    private final String TAG = StorageActivity.class.getName();
    private final ArrayList<QueueItem> folderList = new ArrayList<>();
    private final LinkedHashMap<Long, QueueItem> folderMap = new LinkedHashMap<>();
    private final WeakHandler handler = new WeakHandler();
    private String startPath;
    private ArrayList<QueueItem> storagePath = new ArrayList<>();
    private String currentPath = "";
    private String[] paths = currentPath.split("/");
    private String rootPath = "/";
    private boolean isFaving = false;
    private FastScrollRecyclerView recyclerView;
    private HorizontalScrollView scrollview;
    private LinearLayout folderlayout;
    private RelativeLayout notConnectedLayout;
    private ImageButton closeNotConnectedButton;
    private MainReceiver mainReceiver;
    private MenuItem menuItemSearch;
    private MenuItem menuItemAll;
    private MenuItem menuItemShowHidden;
    private MenuItem menuItemHideHidden;
    private CoordinatorLayout coordinatorlayout;
    private StorageAdapter storageAdapter;
    private ActionMode actionMode = null;
    private int currentElement;
    private int textviewLoop = 0;
    private ArrayList<QueueItem> folderItems = new ArrayList<>();
    private GetMediaFilesForFolder getMediaFilesForFolder;
    private ScanMediaFilesForPath scanMediaFilesForPath;
    private DeleteFolder deleteFolder;
    private ArrayList<File> filesToDelete = new ArrayList<>();
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private ImageButton advancedSearch;
    private int advancedSearchInset;
    private Toolbar toolbar;
    private RelativeLayout footer;
    private QueueItem initialQueueItem = null;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private BoxApi boxApi;
    private AmazonApi amazonApi;
    private OneDriveApi oneDriveApi;
    private TextView textView;
    private ArrayList<QueueItem> parentFolders = new ArrayList<>();
    private RelativeLayout progressLayout;
    private boolean isAddingToLibrary;
    private boolean loading;
    private MiniPlayer miniPlayer;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private View.OnClickListener storageHeaderClick =
            v -> {
                try {

                    CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(v.getId());
                    if (cloudAccount != null) {
                        if (v.getTag().equals("root")
                                || v.getTag().equals("")
                                || v.getTag().equals("0")) {
                            QueueItem queueItem = CloudHelper.getCloudItem(cloudAccount);
                            folderMap.put(queueItem.id, queueItem);
                            storagePath.clear();
                            QueueItem drivesItem = CloudHelper.getDrivesItem();
                            folderMap.put(drivesItem.id, drivesItem);
                            storagePath.add(drivesItem);
                            storagePath.add(queueItem);
                            updateStoragePath();
                            openRootDrive(queueItem);
                        }
                    } else if (v.getId() == R.id.drives) {
                        storageAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
                        storagePath.clear();
                        QueueItem drivesItem = CloudHelper.getDrivesItem();
                        folderMap.put(drivesItem.id, drivesItem);
                        storagePath.add(drivesItem);
                        loadStorageRoot(false);
                        updateStoragePath();
                    } else if (v.getId() == R.id.root) {
                        storageAdapter.setStorageMode(LOCAL);
                        storagePath.clear();
                        QueueItem drivesItem = CloudHelper.getDrivesItem();
                        folderMap.put(drivesItem.id, drivesItem);
                        storagePath.add(drivesItem);
                        QueueItem mainRootItem = CloudHelper.getMainRootItem(true);
                        folderMap.put(mainRootItem.id, mainRootItem);
                        storagePath.add(mainRootItem);
                        currentPath = "";
                        loadLocal(false,0);
                        updateStoragePath();
                        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
                        if (storageInfoList.size() > 1) {
                            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?");
                        }
                    } else {
                        QueueItem queueItem = folderMap.get((long) v.getId());
                        if (queueItem != null) {
                            storagePath.clear();
                            loadFolderPathRecursive(queueItem);
                            Collections.reverse(storagePath);
                            updateStoragePath();
                            openRootDrive(queueItem);
                        } else {
                            String path = "";
                            for (int j = 0; j <= v.getId(); j++) {
                                path = path + paths[j] + "/";
                            }
                            if (path.equals("/storage/emulated/")) {
                                path = "/storage/emulated/0";
                            }
                            currentPath = path;
                            loadLocal(false,0);
                            updateStoragePath();
                        }
                    }

                } catch (Exception ex) {
                    Crashlytics.logException(ex);
                }
            };
    private boolean loadingCancelled;
    private boolean alreadyResumed;
    private LinearLayout mainPlayerLayout;


    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }


    private void loadFolderPathRecursive(QueueItem queueItem) {
        storagePath.add(queueItem);
        for (QueueItem folderItem : folderMap.values()) {
            if (folderItem.id == queueItem.order) {
                loadFolderPathRecursive(folderItem);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        findViewsById();

        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
        toolbar.setTitle(storage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

//        rootPath = currentPath = file.getPath(); //.getAbsolutePath();
        rootPath = currentPath = "/storage/";

        QueueItem drivesItem = CloudHelper.getDrivesItem();
        folderMap.put(drivesItem.id, drivesItem);
        storagePath.add(drivesItem);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        folderList.clear();
        storageAdapter = new StorageAdapter(this, folderList, PlayerConstants.QUEUE_TYPE_FOLDERS, prefShowArtwork, TAG, this);
        storageAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new PicassoScrollListener(this, TAG));
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(storageAdapter);

        setupMainPlayer();

        footer.setOnClickListener(this);
        closeNotConnectedButton.setOnClickListener(this);

        if (getIntent().getStringExtra(MyApplication.ARG_DATA) != null) {
            startPath = getIntent().getStringExtra(MyApplication.ARG_DATA);
            initialQueueItem = TrackRealmHelper.getTrack(startPath);
        }
        if (initialQueueItem != null) {
            if (initialQueueItem.storage == 1 || initialQueueItem.storage == 2) {
                if (startPath != null) {
                    File folder = new File(startPath);
                    currentPath = folder.getParent();
                    storageAdapter.setStorageMode(LOCAL);
                    QueueItem mainRootItem = CloudHelper.getMainRootItem(true);
                    folderMap.put(mainRootItem.id, mainRootItem);
                    storagePath.add(mainRootItem);
                    loadLocal(false,0);
                    updateStoragePath();
                    checkStoragePermissions("Do you want to grant access to extermal storage");
                }
            } else {
                loadCloudStartFolder(initialQueueItem);
            }
        } else {
            loadStorageRoot(false);
            List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
            if (storageInfoList.size() > 1) {
                checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?");
            }
        }


        register();
        updateStoragePath();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (storageAdapter.getStorageMode().equals(DRIVES)) {
            loadStorageRoot(false);
        }
        register();
        mainUpdate();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();

        if (getMediaFilesForFolder != null) {
            getMediaFilesForFolder.cancel(true);
            getMediaFilesForFolder = null;
        }

        if (scanMediaFilesForPath != null) {
            scanMediaFilesForPath.cancel(true);
            scanMediaFilesForPath = null;
        }

        if (deleteFolder != null) {
            deleteFolder.cancel(true);
            deleteFolder = null;
        }

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(
            final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            if (Settings.System.canWrite(this)) {
                SetRingtone createRingtone = new SetRingtone();
                createRingtone.open(StorageActivity.this, selectedItem);
            } else {
                AppController.toast(
                        this,
                        "Write settings permission wasn't provided. Muziko can't set default ringtone");
            }
        } else if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = resultData.getData();

                // Persist URI in shared preference so that you can use it later.
                // Use your own framework here instead of PreferenceUtil.
                String uri = String.valueOf(treeUri);
                PrefsManager.Instance().setStoragePermsURI(uri);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("prefStoragePerms", true);
                editor.apply();

                grantUriPermission(
                        getPackageName(),
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver()
                        .takePersistableUriPermission(
                                treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (loading) {
            loadingCancelled = true;
            toggleRefresh(false);
        }

        if (slidingUpPanelLayout != null && (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            miniPlayer.close();
            miniPlayer.open();
            return;
        } else if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();

            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
                return;
            }
        }

        if (storagePath.size() > 1) {
            if (storageAdapter.getStorageMode() == LOCAL) {
                if (currentPath.equalsIgnoreCase("/storage/")) {
                    storageAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
                    loadStorageRoot(false);
                    updateStoragePath();
                } else {
                    int backOne = currentElement - 1;
                    String path = "";
                    final String[] paths = currentPath.split("/");
                    if (paths.length > 0) {
                        for (int j = 0; j < backOne; j++) {
                            path = path + paths[j] + "/";
                        }
                    }
                    currentPath = path;
                    loadLocal(true,scrollPosition);
                    updateStoragePath();
                }
            } else {
                QueueItem currentItem = storagePath.get(storagePath.size() - 2);
                if (currentItem.id == R.id.drives) {
                    storagePath.remove(storagePath.size() - 1);
                    storageAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
                    loadStorageRoot(false);
                    updateStoragePath();
                } else if (currentItem.id == R.id.root) {
                    storageAdapter.setStorageMode(LOCAL);
                    storagePath.remove(storagePath.size() - 1);
                    currentPath = "";
                    loadLocal(false,scrollPosition);
                    updateStoragePath();
                } else {
                    QueueItem queueItem = folderMap.get(currentItem.id);
                    if (queueItem != null) {
                        storagePath.clear();
                        loadFolderPathRecursive(queueItem);
                        Collections.reverse(storagePath);
                        updateStoragePath();
                        openRootDrive(queueItem);
                    }
                }
            }
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        unregister();
        super.onPause();
    }

    @DebugLog
    private void loadCloudStartFolder(QueueItem queueItem) {
        switch ((int) queueItem.album) {
            case CloudManager.GOOGLEDRIVE:
                storageAdapter.setStorageMode(CloudManager.StorageMode.DRIVE);
                driveApi = (DriveApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (driveApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    AsyncJob.doInBackground(() -> {
                        parentFolders.clear();
                        parentFolders = driveApi.getFolderPath(queueItem);
                        final boolean result = true;
                        AsyncJob.doOnMainThread(() -> {
                            populateCloudStartFolders(queueItem);
                        });
                    });
                }
                break;

            case CloudManager.DROPBOX:
                storageAdapter.setStorageMode(CloudManager.StorageMode.DROPBOX);
                dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (dropBoxApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    AsyncJob.doInBackground(() -> {
                        parentFolders.clear();
                        parentFolders = dropBoxApi.getFolderPath(queueItem);
                        final boolean result = true;
                        AsyncJob.doOnMainThread(() -> {
                            populateCloudStartFolders(queueItem);
                        });
                    });
                }
                break;

            case CloudManager.BOX:
                storageAdapter.setStorageMode(CloudManager.StorageMode.BOX);
                boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (boxApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    AsyncJob.doInBackground(() -> {
                        parentFolders.clear();
                        parentFolders = boxApi.getFolderPath(queueItem);
                        final boolean result = true;
                        AsyncJob.doOnMainThread(() -> {
                            populateCloudStartFolders(queueItem);
                        });
                    });
                }
                break;

            case CloudManager.ONEDRIVE:
                storageAdapter.setStorageMode(CloudManager.StorageMode.AMAZON);
                amazonApi = (AmazonApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (amazonApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    AsyncJob.doInBackground(() -> {
                        parentFolders.clear();
                        parentFolders = oneDriveApi.getFolderPath(queueItem);
                        final boolean result = true;
                        AsyncJob.doOnMainThread(() -> {
                            populateCloudStartFolders(queueItem);
                        });
                    });
                }
                break;

            case CloudManager.AMAZON:
                storageAdapter.setStorageMode(CloudManager.StorageMode.ONEDRIVE);
                oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (oneDriveApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    AsyncJob.doInBackground(() -> {
                        parentFolders.clear();
                        parentFolders = oneDriveApi.getFolderPath(queueItem);
                        final boolean result = true;
                        AsyncJob.doOnMainThread(() -> {
                            populateCloudStartFolders(queueItem);
                        });
                    });
                }
                break;
        }

    }

    private void populateCloudStartFolders(QueueItem queueItem) {
        parentFolders.remove(0);
        parentFolders.add(0, CloudHelper.getCloudItem(CloudAccountRealmHelper.getCloudAccount(queueItem.storage)));
        for (QueueItem folder : parentFolders) {
            folderMap.put(folder.id, folder);
        }
        storagePath.addAll(parentFolders);
        updateStoragePath();
        openRootDrive(storagePath.get(storagePath.size() - 1));
    }

    private void toggleRefresh(boolean refresh) {

        if (refresh) {
            loading = true;
            progressLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            loading = false;
            progressLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadStorageRoot(boolean scroll) {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        storageAdapter.setShowArtwork(prefShowArtwork);

        folderList.clear();
        QueueItem mainRootItem = CloudHelper.getMainRootItem(false);
        folderMap.put(mainRootItem.id, mainRootItem);
        folderList.add(mainRootItem);
        ArrayList<CloudAccount> cloudAccounts = CloudAccountRealmHelper.getCloudAccounts();
        for (CloudAccount cloudAccount : cloudAccounts) {
            QueueItem queue = CloudHelper.getCloudItem(cloudAccount);
            folderMap.put(queue.id, queue);
            folderList.add(queue);
        }

        folderList.addAll(getHomeItems());
        Collections.sort(folderList, (s1, s2) -> (int) s1.album - (int) s2.album);

        storageAdapter.update();

        if (scroll && folderList.size() > 0) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {

        if (miniPlayer != null) {
            miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {

        miniPlayer.updateProgress(event.getProgress(), event.getDuration());
    }

    private ArrayList<QueueItem> getHomeItems() {

        ArrayList<QueueItem> homeItems = new ArrayList<>();
        ArrayList<StorageFolder> homeFolders = StorageFolderRealmHelper.getHomeFolders();
        for (StorageFolder storageFolder : homeFolders) {
            if (!storageAdapter.isShowHidden() && storageFolder.isHiddenFolder()) continue;
            QueueItem folderModel = new QueueItem();
            folderModel.id = Utils.randLong();
            if (storageFolder.getCloudAccountId() != 0) {
                folderModel.album =
                        CloudAccountRealmHelper.getCloudAccount(storageFolder.getCloudAccountId())
                                .getCloudProvider();
                folderModel.artist_name =
                        CloudAccountRealmHelper.getCloudAccount(storageFolder.getCloudAccountId())
                                .getAccountName();
                folderModel.folder_path = storageFolder.getFolder_path();
            } else {
                folderModel.album = CloudManager.LOCAL;
                folderModel.folder_path = storageFolder.getFolder_path();
            }
            folderModel.title = storageFolder.getTitle();
            folderModel.date = 0L;
            folderModel.data = storageFolder.getPath();
            folderModel.songs = 0;
            folderModel.folder = true;
            folderModel.storage = storageFolder.getCloudAccountId();
            folderModel.order = storageFolder.getCloudAccountId();
            homeItems.add(folderModel);
        }

        return homeItems;
    }

    private void findViewsById() {
        toolbar = findViewById(R.id.toolbar);
        footer = findViewById(R.id.footer);
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        scrollview = findViewById(R.id.scrollview);
        folderlayout = findViewById(R.id.folderlayout);
        recyclerView = findViewById(R.id.itemList);
        progressLayout = findViewById(R.id.progressLayout);
        notConnectedLayout = findViewById(R.id.notConnectedLayout);
        closeNotConnectedButton = findViewById(R.id.closeNotConnectedButton);
        mainPlayerLayout = findViewById(R.id.mainPlayerLayout);
    }

    private void updateStoragePath() {

        if (miniPlayer != null) {
            if (storagePath.size() > 1) {
                miniPlayer.open();
            } else {
                miniPlayer.close();
            }
        }

        folderlayout.removeAllViews();

        for (int i = 0; i < storagePath.size(); i++) {
            QueueItem queueItem = storagePath.get(i);

            currentElement = storagePath.size();

            textView = new TextView(this);
            textView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

            if (i != storagePath.size() - 1) {
                textView.setText(queueItem.title + " > ");
                if (currentElement != 1) {
                    textView.setAlpha(0.7f);
                }
            } else {
                textView.setText(queueItem.title);
                if (currentElement != 1) {
                    textView.setAlpha(0.7f);
                }
            }

            textView.setTextColor(Color.WHITE);
            textView.setTag(queueItem.data);
            textView.setTextSize(18f);
            textView.setBackgroundColor(Color.TRANSPARENT); // hex color 0xAARRGGBB
            textView.setPadding(5, 5, 5, 5); // in pixels (left, top, right, bottom)

            textView.setId((int) queueItem.id);

            if (queueItem.folder_path.equals("cloud")) {
                switch ((int) queueItem.album) {
                    case CloudManager.GOOGLEDRIVE:
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.drive_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.DROPBOX:
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.dropbox_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.BOX:
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.box_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.ONEDRIVE:
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.onedrive_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.AMAZON:
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.onedrive_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                }
            }
            textView.setOnClickListener(storageHeaderClick);
            folderlayout.addView(textView);

            if (startPath != null) {
//                scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                startPath = null;
            }
        }

        if (storageAdapter.getStorageMode() == LOCAL) {
            paths = currentPath.split("/");

            for (int i = 0; i < paths.length; i++) {
                String element = paths[i];

                currentElement = paths.length;

                textView = new TextView(this);
                textView.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));

                if (element.isEmpty()) {
                    continue;
                }

                if (i != paths.length - 1) {
                    textView.setText(element + " > ");
                    textView.setAlpha(0.7f);
                } else {
                    textView.setText(element);
                }
                textView.setTextColor(Color.WHITE);
                textView.setTag(String.valueOf(i));
                textView.setTextSize(18f);
                textView.setBackgroundColor(Color.TRANSPARENT); // hex color 0xAARRGGBB
                textView.setPadding(5, 5, 5, 5); // in pixels (left, top, right, bottom)

                textView.setId(i);
                textView.setOnClickListener(storageHeaderClick);
                folderlayout.addView(textView);

                if (startPath != null) {
//                    scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                    startPath = null;
                }
            }
        }

        autoSmoothScroll();

    }

    private void autoSmoothScroll() {

        scrollview.postDelayed(() -> {
            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
//                scrollview.smoothScrollBy(500, 0);
        }, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = null;

        menu.clear();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.folders_menu, menu);

        menuItemSearch = menu.findItem(R.id.player_search);
        menuItemAll = menu.findItem(R.id.player_play_songs);
        menuItemShowHidden = menu.findItem(R.id.show_hidden_folders);
        menuItemHideHidden = menu.findItem(R.id.hide_hidden_folders);
        if (menuItemSearch != null) menuItemSearch.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menuItemShowHidden != null)
            menuItemShowHidden.setVisible(!storageAdapter.isShowHidden());
        if (menuItemHideHidden != null)
            menuItemHideHidden.setVisible(storageAdapter.isShowHidden());
        if (menuItemAll != null) menuItemAll.setVisible(storageAdapter.getItemCount() != 0);
        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();
            AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
            Resources resources = getResources();
            advancedSearch = advancedSearchButton.addButton(this, resources, searchView);
            advancedSearch.setOnClickListener(this);
            ActionBar.LayoutParams searchviewParams =
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint("Search folders");
            searchView.setOnQueryTextListener(this);
            searchView.setOnSearchClickListener(this);
            searchView.setOnCloseListener(this);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.player_search:
                return true;

            case R.id.player_play_songs:
                play();
                return true;

            case R.id.show_hidden_folders:
                storageAdapter.setShowHidden(true);
                if (storageAdapter.getStorageMode().equals(DRIVES)) {
                    loadStorageRoot(false);
                }
                storageAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
                return true;

            case R.id.hide_hidden_folders:
                storageAdapter.setShowHidden(false);
                if (storageAdapter.getStorageMode().equals(DRIVES)) {
                    loadStorageRoot(false);
                }
                storageAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
                return true;

            case R.id.player_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.player_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(StorageActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.player_exit:
                AppController.Instance().exit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        storageAdapter.getFilter().filter(newText);
        PlayerConstants.QUEUE_TYPE = 0;

        return false;
    }

    @Override
    public boolean onClose() {
        advancedSearch.setVisibility(View.GONE);
        toolbar.setContentInsetStartWithNavigation(advancedSearchInset);
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == advancedSearch) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (v == footer) {
            Intent intent = new Intent(this, StorageAddAccountActivity.class);
            startActivity(intent);
        } else if (v == closeNotConnectedButton) {
            notConnectedLayout.setVisibility(View.GONE);
        } else {
            advancedSearch.setVisibility(View.VISIBLE);
            toolbar.setContentInsetStartWithNavigation(0);
        }
    }

    private void play() {
        PlayerConstants.QUEUE_TYPE = PlayerConstants.QUEUE_TYPE_QUEUE;
        PlayerConstants.QUEUE_INDEX = 0;
        PlayerConstants.QUEUE_TIME = 0;
        AppController.Instance().servicePlay(false);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();

        alreadyResumed = true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.context_multiselect, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<QueueItem> list = storageAdapter.getSelectedItems();
        if (list.size() > 0) {

            switch (item.getItemId()) {

                case R.id.play:
                    AppController.Instance().clearAddToQueue(this, list);
                    break;

                case R.id.add_to_queue:
                    AppController.Instance().addToQueue(StorageActivity.this, list, false);
                    break;

                case R.id.play_next:
                    AppController.Instance().addToQueue(StorageActivity.this, list, true);
                    break;

                case R.id.add_to_playlist:
                    AppController.Instance().addToPlaylist(StorageActivity.this, list, false);
                    break;

                case R.id.delete:
                    deleteItems(list);
                    break;

                case R.id.trash:
                    movetoNegative(list);
                    break;

                case R.id.favourite:
                    favorite(list);
                    break;

                default:
                    return false;
            }
        }

        mode.finish();
        actionMode = null;
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        handler.post(() -> {
            if (!recyclerView.isComputingLayout()) {
                ((SelectableAdapter) recyclerView.getAdapter()).setMultiSelect(false);
                actionMode = null;
            } else {
                onDestroyActionMode(this.actionMode);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (networkState == NetworkInfo.State.CONNECTED) {
            notConnectedLayout.setVisibility(View.GONE);
        } else {
            notConnectedLayout.setVisibility(View.VISIBLE);
        }
    }

    private void favorite(final ArrayList<QueueItem> queueItems) {

        for (int i = 0; i < queueItems.size(); i++) {
            QueueItem queueItem = queueItems.get(i);
            TrackRealmHelper.addFavorite(queueItem.data);
        }

        AppController.toast(this, "Songs added to Favorites");
        sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
        sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
        sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
    }

    private void movetoNegative(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(
                this,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    for (int i = 0; i < queueItems.size(); i++) {
                        QueueItem queueItem = queueItems.get(i);
                        TrackRealmHelper.movetoNegative(queueItem);
                    }
                    loadLocal(false,0);
                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    @Override
    public void onDragTouched(RecyclerView.ViewHolder viewHolder) {
    }

    @Override
    public void onMenuClicked(final int position) {
        final QueueItem item = storageAdapter.getItem(position);
        if (item == null) return;

        selectedItem = item;
        selectedItemPosition = position;

        final ArrayList<MenuObject> driveItems = new ArrayList<>();
        driveItems.add(new MenuObject(MenuObject.RENAME_CLOUD));
        driveItems.add(new MenuObject(MenuObject.REMOVE_CLOUD));

        final ArrayList<MenuObject> cloudFolderItems = new ArrayList<>();
        //        cloudFolderItems.add(new MenuObject(MenuObject.SET_AS_START_DIRECTORY));
        cloudFolderItems.add(new MenuObject(MenuObject.DELETE_ITEM));
        if (StorageFolderRealmHelper.getFolder(selectedItem.storage, selectedItem.data)
                .isHomeFolder()) {
            cloudFolderItems.add(new MenuObject(MenuObject.REMOVE_FROM_HOME));
        } else {
            cloudFolderItems.add(new MenuObject(MenuObject.ADD_TO_HOME));
        }
        if (StorageFolderRealmHelper.getFolder(selectedItem.storage, selectedItem.data)
                .isHiddenFolder()) {
            cloudFolderItems.add(new MenuObject(MenuObject.SHOW_FOLDER));
        } else {
            cloudFolderItems.add(new MenuObject(MenuObject.HIDE_FOLDER));
        }
        if (StorageFolderRealmHelper.getFolder(selectedItem.storage, selectedItem.data)
                .isExcludedFolder()) {
            cloudFolderItems.add(new MenuObject(MenuObject.INCLUDE_FOLDER));
        } else {
            cloudFolderItems.add(new MenuObject(MenuObject.EXCLUDE_FOLDER));
        }

        final ArrayList<MenuObject> folderItems = new ArrayList<>();
        folderItems.add(new MenuObject(MenuObject.PLAY_NEXT));
        folderItems.add(new MenuObject(MenuObject.ADD_TO_QUEUE));
        folderItems.add(new MenuObject(MenuObject.ADD_TO_PLAYLIST));
        //        folderItems.add(new MenuObject(MenuObject.SET_AS_START_DIRECTORY));
        folderItems.add(new MenuObject(MenuObject.SCAN));
        folderItems.add(new MenuObject(MenuObject.DELETE_ITEM));
        if (StorageFolderRealmHelper.getFolder(selectedItem.storage, selectedItem.data)
                .isHomeFolder()) {
            folderItems.add(new MenuObject(MenuObject.REMOVE_FROM_HOME));
        } else {
            folderItems.add(new MenuObject(MenuObject.ADD_TO_HOME));
        }
        if (StorageFolderRealmHelper.getFolder(selectedItem.storage, selectedItem.data)
                .isHiddenFolder()) {
            folderItems.add(new MenuObject(MenuObject.SHOW_FOLDER));
        } else {
            folderItems.add(new MenuObject(MenuObject.HIDE_FOLDER));
        }
        if (StorageFolderRealmHelper.getFolder(selectedItem.storage, selectedItem.data)
                .isExcludedFolder()) {
            folderItems.add(new MenuObject(MenuObject.INCLUDE_FOLDER));
        } else {
            folderItems.add(new MenuObject(MenuObject.EXCLUDE_FOLDER));
        }

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(MenuObject.ADD_TO_QUEUE));
        items.add(new MenuObject(MenuObject.ADD_TO_PLAYLIST));
        items.add(
                new MenuObject(
                        MenuObject.FAV,
                        (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_favs)
                                : getString(R.string.remove_from_favs)));
        items.add(new MenuObject(MenuObject.PLAY_NEXT));
        items.add(new MenuObject(MenuObject.GO_TO_ARTIST));
        items.add(new MenuObject(MenuObject.GO_TO_ALBUM));
        items.add(new MenuObject(MenuObject.SET_RINGTONE));
        items.add(new MenuObject(MenuObject.CUT));
        items.add(new MenuObject(MenuObject.PREVIEW_SONG));
        items.add(new MenuObject(MenuObject.EDIT_TAGS));
        items.add(new MenuObject(MenuObject.DETAILS));
        items.add(
                new MenuObject(
                        ADD_TO_LIBRARY,
                        (TrackRealmHelper.getLibraryList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_library)
                                : getString(R.string.remove_from_library)));
        items.add(new MenuObject(MenuObject.SHARE_ITEM));
        items.add(new MenuObject(MenuObject.PLAY_X_TIMES));
        items.add(new MenuObject(MenuObject.MOVE_TO_IGNORE));
        items.add(new MenuObject(MenuObject.DELETE_ITEM));

        final ArrayList<MenuObject> cloudfiles = new ArrayList<>();
        cloudfiles.add(new MenuObject(MenuObject.ADD_TO_QUEUE));
        cloudfiles.add(new MenuObject(MenuObject.ADD_TO_PLAYLIST));
        cloudfiles.add(
                new MenuObject(
                        MenuObject.FAV,
                        (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_favs)
                                : getString(R.string.remove_from_favs)));
        cloudfiles.add(new MenuObject(MenuObject.PLAY_NEXT));
        cloudfiles.add(new MenuObject(MenuObject.DETAILS));
        cloudfiles.add(
                new MenuObject(
                        ADD_TO_LIBRARY,
                        (TrackRealmHelper.getLibraryList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_library)
                                : getString(R.string.remove_from_library)));
        cloudfiles.add(new MenuObject(MenuObject.PLAY_X_TIMES));
        cloudfiles.add(new MenuObject(MenuObject.MOVE_TO_IGNORE));
        cloudfiles.add(new MenuObject(DOWNLOAD));
        cloudfiles.add(new MenuObject(MenuObject.DELETE_ITEM));

        MaterialMenuAdapter materialMenuAdapter = null;

        switch (storageAdapter.getStorageMode()) {
            case DRIVES:
                if (item.order == R.id.drives) {
                    materialMenuAdapter = new MaterialMenuAdapter(driveItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudFolderItems, this);
                }

                break;
            case LOCAL:
                if (item.folder) {
                    materialMenuAdapter = new MaterialMenuAdapter(folderItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(items, this);
                }
                break;
            case DRIVE:
                if (item.folder) {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudFolderItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudfiles, this);
                }
                break;
            case DROPBOX:
                if (item.folder) {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudFolderItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudfiles, this);
                }
                break;
            case BOX:
                if (item.folder) {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudFolderItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudfiles, this);
                }
                break;
            case ONEDRIVE:
                if (item.folder) {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudFolderItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudfiles, this);
                }
                break;
            case AMAZON:
                if (item.folder) {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudFolderItems, this);
                } else {
                    materialMenuAdapter = new MaterialMenuAdapter(cloudfiles, this);
                }
                break;
        }

        new MaterialDialog.Builder(this)
                .adapter(materialMenuAdapter, new LinearLayoutManager(this))
                .show();
    }

    @Override
    public void onItemClicked(int position) {
        scrollPosition=position;
        QueueItem item = storageAdapter.getItem(position);
        if (item == null) return;
        handler.postDelayed(
                () -> {
                    switch (storageAdapter.getStorageMode()) {
                        case DRIVES:
                            QueueItem drivesItem = CloudHelper.getDrivesItem();
                            folderMap.put(drivesItem.id, drivesItem);
                            storagePath.clear();
                            storagePath.add(drivesItem);
                            storagePath.add(item);
                            updateStoragePath();
                            openRootDrive(item);
                            break;
                        default:
                            if (storageAdapter.isMultiSelect()) {
                                if (!item.folder) {
                                    toggleSelection(position);
                                }
                            } else {
                                open(storageAdapter.getItem(position), position);
                            }
                            break;
                    }
                },
                getResources().getInteger(R.integer.ripple_duration_delay));
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.storageAdapter.isMultiSelect()) {
            QueueItem item = storageAdapter.getItem(position);
            if (item == null || item.folder) return false;

            this.startSupportActionMode(this);

            this.storageAdapter.setMultiSelect(true);

            toggleSelection(position);
            return true;
        }
        return false;
    }

    private void openRootDrive(QueueItem queueItem) {
        toggleRefresh(true);
        switch ((int) queueItem.album) {
            case CloudManager.GOOGLEDRIVE:
                storageAdapter.setStorageMode(CloudManager.StorageMode.DRIVE);
                driveApi = (DriveApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (driveApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    driveApi.getFolderItems(queueItem, this);
                }
                break;

            case CloudManager.DROPBOX:
                storageAdapter.setStorageMode(CloudManager.StorageMode.DROPBOX);
                dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (dropBoxApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    dropBoxApi.getFolderItems(queueItem, this);
                }
                break;

            case CloudManager.BOX:
                storageAdapter.setStorageMode(CloudManager.StorageMode.BOX);
                boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (boxApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    boxApi.getFolderItems(queueItem, this);
                }
                break;

            case CloudManager.ONEDRIVE:
                storageAdapter.setStorageMode(CloudManager.StorageMode.ONEDRIVE);
                oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (oneDriveApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    if (queueItem.data.equals("root")) {
                        oneDriveApi.getRootFolderItems(queueItem, this);
                    } else {
                        oneDriveApi.getFolderItems(queueItem, this);
                    }
                }
                break;

            case CloudManager.AMAZON:
                storageAdapter.setStorageMode(CloudManager.StorageMode.AMAZON);
                amazonApi = (AmazonApi) CloudManager.Instance().getCloudDrive(this, queueItem.storage);
                if (amazonApi == null) {
                    AppController.toast(this, getString(R.string.cloud_drive_not_connected));
                } else {
                    if (queueItem.data.equals("root")) {
                        amazonApi.getRootFolderItems(queueItem, this);
                    } else {
                        amazonApi.getFolderItems(queueItem, this);
                    }
                }
                break;

            case CloudManager.LOCAL:

                if (!queueItem.data.equalsIgnoreCase("root")) {
                    startPath = queueItem.getData();
                    File folder = new File(startPath);
                    currentPath = startPath;
//                    currentPath = folder.getParent();
                }

                storageAdapter.setStorageMode(LOCAL);
                storagePath.clear();
                QueueItem drivesItem = CloudHelper.getDrivesItem();
                folderMap.put(drivesItem.id, drivesItem);
                storagePath.add(drivesItem);
                QueueItem mainRootItem = CloudHelper.getMainRootItem(true);
                folderMap.put(mainRootItem.id, mainRootItem);
                storagePath.add(mainRootItem);
                loadLocal(false,0);
                updateStoragePath();
                toggleRefresh(false);

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(StorageActivity.this, selectedItem);
                } else {
                    AppController.toast(
                            this,
                            "Write settings permission wasn't provided. Muziko can't set default ringtone");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(
                () -> {
                    switch (item.id) {
                        case MenuObject.RENAME_CLOUD:
                            new MaterialDialog.Builder(this)
                                    .theme(Theme.LIGHT)
                                    .titleColorRes(R.color.black)
                                    .negativeColorRes(R.color.dialog_negetive_button)
                                    .title(getString(R.string.enter_cloud_account_name))
                                    .positiveText(R.string.save)
                                    .negativeText(R.string.cancel)
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .input(
                                            getString(R.string.rename_cloud_account),
                                            "",
                                            (clouddialog, input) -> {
                                                if (input.toString().length() > 0) {

                                                    if (CloudAccountRealmHelper.renameCloudAccount(
                                                            selectedItem.getId(),
                                                            input.toString())) {
                                                        loadStorageRoot(false);
                                                    }

                                                } else {
                                                    AppController.toast(
                                                            this,
                                                            getString(
                                                                    R.string
                                                                            .rename_cloud_account_error));
                                                }
                                            })
                                    .show();

                            break;

                        case MenuObject.REMOVE_CLOUD:

                            new MaterialDialog.Builder(this)
                                    .theme(Theme.LIGHT)
                                    .titleColorRes(R.color.normal_blue)
                                    .negativeColorRes(R.color.dialog_negetive_button)
                                    .positiveColorRes(R.color.normal_blue)
                                    .title("Remove cloud account")
                                    .content("Are you sure you want to remove this cloud account?")
                                    .positiveText("OK")
                                    .onPositive((innerDialog, which) -> {
                                        TrackRealmHelper.deleteCloudTracks((int) selectedItem.getId());
                                        CloudAccountRealmHelper.deleteCloudAccount(selectedItem.getId());
                                        StorageFolderRealmHelper.deleteCloudFolders(selectedItem.getId());

                                        CloudManager.Instance().removeDriveApi(selectedItem.artist_name);
                                        CloudManager.Instance().removeDropBoxApi(selectedItem.artist_name);
                                        CloudManager.Instance().removeBoxApi(selectedItem.artist_name);
                                        CloudManager.Instance().removeOneDriveApi(selectedItem.artist_name);

                                        for (int i = 0; i < connectedCloudDrives.size(); i++) {
                                            int connectedDrive = connectedCloudDrives.get(i);
                                            if (selectedItem.getId() == connectedDrive) {
                                                connectedCloudDrives.remove(i);
                                            }
                                        }
                                        loadStorageRoot(false);
                                        EventBus.getDefault().post(new RefreshEvent(1000));
                                        AppController.toast(this, getString(R.string.cloud_account_removed));
                                    })
                                    .negativeText("Cancel")
                                    .show();

                            break;

                        case MenuObject.ADD_TO_QUEUE: //add to q
                            if (selectedItem.isFolder()) {
                                if (getMediaFilesForFolder != null) {
                                    getMediaFilesForFolder.cancel(true);
                                    getMediaFilesForFolder = null;
                                }

                                getMediaFilesForFolder = new GetMediaFilesForFolder(StorageActivity.this, 0, selectedItem.data, selectedItemPosition);
                                getMediaFilesForFolder.execute();
                            } else {
                                PlayerConstants.QUEUE_TYPE = 0;
                                AppController.Instance().addToQueue(this, selectedItem, false);
                            }

                            break;

                        case MenuObject.ADD_TO_PLAYLIST: //add to p
                            if (selectedItem.isFolder()) {
                                if (getMediaFilesForFolder != null) {
                                    getMediaFilesForFolder.cancel(true);
                                    getMediaFilesForFolder = null;
                                }

                                getMediaFilesForFolder = new GetMediaFilesForFolder(StorageActivity.this, 2, selectedItem.data, selectedItemPosition);
                                getMediaFilesForFolder.execute();
                            } else {
                                AppController.Instance().addToPlaylist(this, selectedItem);
                            }
                            break;

                        case MenuObject.SET_AS_START_DIRECTORY: //set as start
                            PrefsManager.Instance().setStartFolderPath(selectedItem.data);
                            AppController.toast(
                                    StorageActivity.this,
                                    selectedItem.data + " is the new start directory");

                            break;

                        case MenuObject.SCAN: //scan
                            if (scanMediaFilesForPath != null) {
                                scanMediaFilesForPath.cancel(true);
                                scanMediaFilesForPath = null;
                            }

                            scanMediaFilesForPath =
                                    new ScanMediaFilesForPath(
                                            StorageActivity.this, selectedItem.data);
                            scanMediaFilesForPath.execute();
                            break;

                        case MenuObject.DELETE_ITEM: //remove
                            if (selectedItem.storage == 1 || selectedItem.storage == 2) {
                                if (selectedItem.folder) {
                                    if (getMediaFilesForFolder != null) {
                                        getMediaFilesForFolder.cancel(true);
                                        getMediaFilesForFolder = null;
                                    }

                                    getMediaFilesForFolder =
                                            new GetMediaFilesForFolder(
                                                    StorageActivity.this,
                                                    5,
                                                    selectedItem.data,
                                                    selectedItemPosition);
                                    getMediaFilesForFolder.execute();
                                } else {
                                    delete(selectedItemPosition, selectedItem);
                                }
                            } else {
                                cloudDelete(selectedItemPosition, selectedItem);
                            }

                            break;

                        case MenuObject.FAV: //add to f`
                            favorite(selectedItem);
                            break;

                        case MenuObject.PLAY_NEXT: //play next
                            AppController.Instance().addToQueue(StorageActivity.this, selectedItem, true);
                            break;

                        case MenuObject.GO_TO_ARTIST: //goto ar
                            AppController.Instance().gotoArtist(StorageActivity.this, selectedItem, null);
                            break;

                        case MenuObject.GO_TO_ALBUM: //goto al
                            AppController.Instance().gotoAlbum(StorageActivity.this, selectedItem, null);
                            break;

                        case MenuObject.SET_RINGTONE: //createRingtone
                            boolean permission;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                permission = Settings.System.canWrite(StorageActivity.this);
                            } else {
                                permission =
                                        ContextCompat.checkSelfPermission(
                                                StorageActivity.this,
                                                Manifest.permission.WRITE_SETTINGS)
                                                == PackageManager.PERMISSION_GRANTED;
                            }
                            if (!permission) {

                                if (android.os.Build.VERSION.SDK_INT
                                        >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    new MaterialDialog.Builder(StorageActivity.this)
                                            .theme(Theme.LIGHT)
                                            .titleColorRes(R.color.normal_blue)
                                            .negativeColorRes(R.color.dialog_negetive_button)
                                            .positiveColorRes(R.color.normal_blue)
                                            .title("Permission required")
                                            .content(
                                                    "Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.")
                                            .positiveText("Ok")
                                            .onPositive(
                                                    (mdialog, mwhich) -> {
                                                        if (Build.VERSION.SDK_INT
                                                                >= Build.VERSION_CODES.M) {
                                                            Intent intent =
                                                                    new Intent(
                                                                            Settings
                                                                                    .ACTION_MANAGE_WRITE_SETTINGS);
                                                            intent.setData(
                                                                    Uri.parse(
                                                                            "package:"
                                                                                    + getPackageName()));
                                                            startActivityForResult(
                                                                    intent,
                                                                    CODE_WRITE_SETTINGS_PERMISSION);
                                                        } else {
                                                            ActivityCompat.requestPermissions(
                                                                    StorageActivity.this,
                                                                    new String[]{
                                                                            Manifest.permission
                                                                                    .WRITE_SETTINGS
                                                                    },
                                                                    CODE_WRITE_SETTINGS_PERMISSION);
                                                        }
                                                    })
                                            .negativeText("Cancel")
                                            .show();
                                } else {
                                    SetRingtone createRingtone = new SetRingtone();
                                    createRingtone.open(StorageActivity.this, selectedItem);
                                }
                            } else {
                                SetRingtone createRingtone = new SetRingtone();
                                createRingtone.open(StorageActivity.this, selectedItem);
                            }
                            break;

                        case MenuObject.CUT: //cut
                            AppController.Instance().cutSong(selectedItem);
                            break;

                        case MenuObject.PREVIEW_SONG: //preview
                            PreviewSong previewSong = new PreviewSong();
                            previewSong.open(StorageActivity.this, selectedItem);
                            break;

                        case MenuObject.EDIT_TAGS: //edit
                            edit(selectedItemPosition, selectedItem);
                            break;

                        case MenuObject.DETAILS: //details
                            AppController.Instance().details(StorageActivity.this, selectedItem);
                            break;

                        case ADD_TO_LIBRARY:
                            toggleLibrary(selectedItemPosition, selectedItem);
                            break;

                        case MenuObject.SHARE_ITEM: //share
                            AppController.Instance().shareSong(this, selectedItem);
                            break;

                        case MenuObject.MOVE_TO_IGNORE: //negative
                            movetoNegative(selectedItemPosition, selectedItem);
                            break;

                        case MenuObject.DOWNLOAD: //download
                            if (networkState != NetworkInfo.State.CONNECTED) {
                                AppController.toast(this, getString(R.string.no_network_connection));
                                return;
                            }

                            DownloadFile downloadFile = new DownloadFile();
                            if (selectedItem.storage == CloudManager.FIREBASE) {
                                for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebaseFavsList()) {
                                    if (cloudTrack.getMd5().equals(selectedItem.md5)) {
                                        downloadFile.init(this, cloudTrack, MuzikoConstants.FirebaseFileMode.FAVS);
                                        break;
                                    }
                                }
                            } else {
                                downloadFile.init(this, selectedItem);
                            }
                            break;

                        case MenuObject.ADD_TO_HOME:
                            StorageFolderRealmHelper.toggleHomeFolder(selectedItem.storage, selectedItem.data, selectedItem.folder_path, selectedItem.title, true);
                            AppController.toast(this, getString(R.string.added_to_home_folders));

                            if (storageAdapter.getStorageMode() == DRIVES) {
                                storagePath.clear();
                                QueueItem drivesItem = CloudHelper.getDrivesItem();
                                folderMap.put(drivesItem.id, drivesItem);
                                storagePath.add(drivesItem);
                                loadStorageRoot(false);
                                updateStoragePath();
                            }
                            break;

                        case MenuObject.REMOVE_FROM_HOME:
                            StorageFolderRealmHelper.toggleHomeFolder(selectedItem.storage, selectedItem.data, selectedItem.folder_path, selectedItem.title, false);
                            AppController.toast(this, getString(R.string.removed_from_homer_folders));

                            if (storageAdapter.getStorageMode() == DRIVES) {
                                storagePath.clear();
                                QueueItem drivesItem = CloudHelper.getDrivesItem();
                                folderMap.put(drivesItem.id, drivesItem);
                                storagePath.add(drivesItem);
                                loadStorageRoot(false);
                                updateStoragePath();
                            }
                            break;

                        case MenuObject.HIDE_FOLDER:
                            StorageFolderRealmHelper.toggleHiddenFolder(selectedItem.storage, selectedItem.data, selectedItem.folder_path, selectedItem.title, true);
                            AppController.toast(this, getString(R.string.folder_hidden));
                            storageAdapter.notifyDataSetChanged();

                            break;

                        case MenuObject.SHOW_FOLDER:
                            StorageFolderRealmHelper.toggleHiddenFolder(selectedItem.storage, selectedItem.data, selectedItem.folder_path, selectedItem.title, false);
                            AppController.toast(this, getString(R.string.folder_unhidden));
                            storageAdapter.notifyDataSetChanged();

                            break;

                        case MenuObject.EXCLUDE_FOLDER:
                            StorageFolderRealmHelper.toggleExcludedFolder(selectedItem.storage, selectedItem.data, selectedItem.folder_path, selectedItem.title, true);
                            AppController.toast(this, getString(R.string.folder_excluded));
                            storageAdapter.notifyDataSetChanged();
                            EventBus.getDefault().post(new RefreshEvent(1000));
                            break;

                        case MenuObject.INCLUDE_FOLDER:
                            StorageFolderRealmHelper.toggleExcludedFolder(selectedItem.storage, selectedItem.data, selectedItem.folder_path, selectedItem.title, false);
                            AppController.toast(this, getString(R.string.folder_included));
                            storageAdapter.notifyDataSetChanged();
                            EventBus.getDefault().post(new RefreshEvent(1000));
                            break;
                    }

                    dialog.dismiss();
                },
                getResources().getInteger(R.integer.ripple_duration_delay));
    }

    @DebugLog
    private void toggleLibrary(final int pos, final QueueItem queue) {
        if (queue.isLibrary()) {
            LibraryEdit libraryEdit = new LibraryEdit(this, true, s -> storageAdapter.notifyItemChanged(pos));
            libraryEdit.execute(queue);
        } else {
            if (!FirebaseManager.Instance().isOverLimitNow(1)) {
                LibraryEdit libraryEdit = new LibraryEdit(this, s -> storageAdapter.notifyItemChanged(pos));
                libraryEdit.execute(queue);
            }
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles()) deleteRecursive(child);

            if (deleteFolder != null) {
                deleteFolder.cancel(true);
                deleteFolder = null;
            }
            filesToDelete.add(fileOrDirectory);
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }

    private void processFolderOption(int type, final String path, final int position) {

        switch (type) {
            case 0:
                PlayerConstants.QUEUE_TYPE = 0;
                AppController.Instance().addToQueue(StorageActivity.this, folderItems, true);
                break;
            case 1: //add to q
                PlayerConstants.QUEUE_TYPE = 0;
                AppController.Instance().addToQueue(StorageActivity.this, folderItems, false);
                break;

            case 2: //add to p
                AppController.Instance().addToPlaylist(StorageActivity.this, folderItems, true);
                break;

            case 5: //remove
                Utils.askDelete(
                        StorageActivity.this,
                        "Delete Songs",
                        String.format(
                                "This will delete song%s permanently from this device, do you want to proceed ?",
                                folderItems.size() != 1 ? "s" : ""),
                        () -> {
                            TrackDelete tr =
                                    new TrackDelete(
                                            StorageActivity.this,
                                            PlayerConstants.QUEUE_TYPE_TRACKS,
                                            () -> {
                                                storageAdapter.removeAll(folderItems);
                                                //						Utils.toast(FoldersActivity.this, String.format("Song%s deleted from device", folderItems.size() != 1 ? "s" : ""));
                                            });
                            tr.execute(folderItems);
                            deleteRecursive(new File(path));
                            deleteFolder =
                                    new DeleteFolder(filesToDelete, position);
                            deleteFolder.execute();
                        });

                break;
        }
    }

    private void movetoNegative(final int position, final QueueItem queueItem) {
        Utils.askDelete(
                this,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    ArrayList<QueueItem> queueItems;
                    queueItems = TrackRealmHelper.getTracksForAlbum(queueItem.title);
                    for (QueueItem queueItem1 : queueItems) {
                        TrackRealmHelper.movetoNegative(queueItem1);
                    }
                    storageAdapter.removeIndex(position);
                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0) actionMode.setTitle("");
            else actionMode.setTitle(String.format("%d song%s", count, count != 1 ? "s" : ""));
        }
    }

    private void open(final QueueItem queue, int position) {
        if (queue.folder) {
            if (storageAdapter.getStorageMode() == LOCAL) {
                currentPath = queue.data;
                loadLocal(true,0);
            } else {
                storagePath.add(queue);
                openRootDrive(queue);
            }
        } else {
            int folderCount = 0;
            ArrayList<QueueItem> list = new ArrayList<>();
            for (QueueItem item : storageAdapter.getList()) {
                if (!item.folder) {
                    if (item.data.equals(queue.data)) {
                        if (position == -1) position = list.size();
                    }
                    list.add(item);
                } else {
                    folderCount++;
                }
            }

            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_FOLDERS, (position - folderCount), list);
        }

        updateStoragePath();
    }

    private void delete(final int position, final QueueItem queue) {
        if (queue.folder) {
            Utils.askDelete(
                    this,
                    "Delete Folder",
                    "This will permanently delete all songs in the folder, do you want to proceed ?",
                    () -> {
                        FolderDelete df =
                                new FolderDelete(
                                        StorageActivity.this,
                                        () -> storageAdapter.removeIndex(position));
                        df.execute(queue.data);
                    });
        } else {
            Utils.askDelete(
                    this,
                    "Delete Song",
                    "This will delete song permanently from this device, do you want to proceed ?",
                    () -> {
                        TrackDelete tr =
                                new TrackDelete(
                                        StorageActivity.this,
                                        PlayerConstants.QUEUE_TYPE_TRACKS,
                                        () -> storageAdapter.removeIndex(position));

                        tr.execute(queue);
                    });
        }
    }

    private void cloudDelete(final int position, final QueueItem queue) {
        if (queue.folder) {
            Utils.askDelete(
                    this,
                    "Delete Folder",
                    "This will permanently delete all files in the folder, do you want to proceed ?",
                    () -> {
                        CloudFolderDelete cloudFolderDelete = new CloudFolderDelete(StorageActivity.this, () -> {
                            storageAdapter.removeIndex(position);
                        });
                        cloudFolderDelete.execute(queue);
                    });
        } else {
            Utils.askDelete(
                    this,
                    "Delete Song",
                    "This will delete song permanently from this device, do you want to proceed ?",
                    () -> {
                        TrackDelete tr = new TrackDelete(StorageActivity.this, PlayerConstants.QUEUE_TYPE_TRACKS, () -> {
                            storageAdapter.removeIndex(position);
                        });

                        tr.execute(queue);
                    });
        }
    }

    private void deleteItems(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(
                this,
                "Delete Songs",
                String.format(
                        "This will delete song%s permanently from this device, do you want to proceed ?",
                        queueItems.size() != 1 ? "s" : ""),
                () -> {
                    TrackDelete tr =
                            new TrackDelete(
                                    StorageActivity.this,
                                    PlayerConstants.QUEUE_TYPE_TRACKS,
                                    () -> {
                                        storageAdapter.removeAll(queueItems);
                                        AppController.toast(
                                                StorageActivity.this,
                                                String.format(
                                                        "Song%s deleted from device",
                                                        queueItems.size() != 1 ? "s" : ""));
                                    });
                    tr.execute(queueItems);
                });
    }

    private void edit(int position, final QueueItem queue) {
        AppController.Instance().editSong(this, TAG, position, queue);
    }

    private void favorite(final QueueItem queue) {
        if (isFaving) return;
        isFaving = true;

        FavoriteEdit fe =
                new FavoriteEdit(
                        this,
                        PlayerConstants.QUEUE_TYPE_TRACKS,
                        s -> {
                            isFaving = false;

                            storageAdapter.notifyDataSetChanged();
                        });
        fe.execute(queue);
    }

    private QueueItem getRootItem() {
        File file = new File(rootPath);

        QueueItem folderModel = new QueueItem();
        folderModel.id = 0;
        folderModel.album = 0;
        folderModel.title = file.getName();
        folderModel.date = 0L;
        folderModel.data = rootPath;
        folderModel.songs = TrackRealmHelper.getCount();
        folderModel.folder = true;

        return folderModel;
    }

    private QueueItem getParentItem(String path) {
        try {
            File file = new File(path);

            QueueItem folderModel = new QueueItem();
            folderModel.id = 0;
            folderModel.album = 0;
            folderModel.title = "..";
            folderModel.date = 0L;
            folderModel.data = file.getParent();
            folderModel.songs = 0;
            folderModel.folder = true;

            return folderModel;
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

        return null;
    }

    private QueueItem getFolderItem(File file) {
        String name = file.getName();
        String path = file.getAbsolutePath();

        QueueItem folderModel = new QueueItem();
        folderModel.id = 0;
        folderModel.album = 0;
        folderModel.title = name;
        folderModel.date = 0L;
        folderModel.data = path;
        folderModel.folder_path = file.getParentFile().getAbsolutePath();
        folderModel.songs = 0;
        folderModel.folder = true;

        return folderModel;
    }

    private QueueItem getFileItem(File file) {
        String name = file.getName();
        String path = file.getAbsolutePath();

        String newpath = path.replace("/sdcard0/", "/emulated/0/");
        String newpath2 = newpath.replace("/emulated/legacy/", "/emulated/0/");

        QueueItem fileModel = TrackRealmHelper.getTrack(newpath2);
        if (fileModel == null) {
            String lname = name.toLowerCase();
            if (!lname.endsWith(".mp3")) // || lname.endsWith(".wma") || lname.endsWith(".m4a"))
            {
                return null;
            }

            fileModel = new QueueItem();
            fileModel.id = 0;
            fileModel.album = 0;
            fileModel.title = name;
            fileModel.date = 0L;
            fileModel.data = file.getAbsolutePath();
            fileModel.songs = 0;
            fileModel.folder = false;

            return fileModel;
        } else {
            return fileModel;
        }
    }

    private void loadLocal(boolean scroll,int position) {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        storageAdapter.setShowArtwork(prefShowArtwork);

        ArrayList<QueueItem> plist = new ArrayList<>();
        ArrayList<QueueItem> slist = new ArrayList<>();

        if (currentPath.equals("/storage/emulated")) {
            currentPath = "/storage/emulated/0";
        }

        File file = new File(currentPath);
        File[] files = file.listFiles();
        if (files == null) {
            plist.add(getRootItem());
        } else {
            for (File item : files) {

                if (item.isHidden()) continue;

                if (item.isDirectory()) {
                    QueueItem queue = getFolderItem(item);
                    if (queue != null) {
                        plist.add(queue);
                    }
                } else {
                    QueueItem queue = getFileItem(item);
                    if (queue != null) {
                        slist.add(queue);
                    }
                }
            }
        }

        Collections.sort(plist, QueueItem.sortAZ);
        Collections.sort(slist, QueueItem.sortAZ);

        folderList.clear();
        folderList.addAll(plist);
        folderList.addAll(slist);

        storageAdapter.update();

        plist.clear();
        slist.clear();

        if (scroll && folderList.size() > 0) {
            recyclerView.scrollToPosition(position);
        }else if (folderList.size() > 0){
            recyclerView.smoothScrollToPosition(0);
        }
    }

    private void register() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_EXIT);
        filter.addAction(AppController.INTENT_CLEAR);
        filter.addAction(AppController.INTENT_TRACK_EDITED);
        filter.addAction(AppController.INTENT_TRACK_SEEKED);
        filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
        filter.addAction(AppController.INTENT_TRACK_REPEAT);
        filter.addAction(AppController.INTENT_QUEUE_STOPPED);
        filter.addAction(AppController.INTENT_QUEUE_CHANGED);
        filter.addAction(AppController.INTENT_QUEUE_CLEARED);

        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, filter);
    }

    private void unregister() {

        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
    }

    @Override
    public void onFoldersReturned(ArrayList<QueueItem> items) {
        if (loadingCancelled) {
            loadingCancelled = false;
            return;
        }

        for (QueueItem queueItem : items) {
            folderMap.put(queueItem.id, queueItem);
        }
        runOnUiThread(
                () -> {
                    toggleRefresh(false);
                    folderList.clear();
                    folderList.addAll(items);
                    storageAdapter.update();
                });
    }

    @Override
    public void onFoldersFailed() {
        runOnUiThread(
                () -> {
                    toggleRefresh(false);
                });

        AppController.toast(this, getString(R.string.problem_getting_folders));
    }

    private class DeleteFolder extends AsyncTask<Void, String, Void> {

        private final ArrayList<File> mFiles;
        private int position;

        public DeleteFolder(ArrayList<File> path, int position) {
            mFiles = path;
            this.position = position;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                for (File file : mFiles) {
                    DocumentFile targetDocument = SAFHelpers.getDocumentFile(file, false);
                    if (targetDocument != null) {
                        targetDocument.delete();
                    }
                }
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            folderList.remove(position);
            storageAdapter.notifyDataSetChanged();
            AppController.toast(StorageActivity.this, "Folder successfully deleted");
        }
    }

    private class GetMediaFilesForFolder extends AsyncTask<Void, String, ArrayList<QueueItem>> {

        private final Context context;
        private final List<File> mfiles = new ArrayList<>();
        private final ArrayList<QueueItem> queueItems = new ArrayList<>();
        private final String[] mExtensions;
        private final String mPath;
        private MaterialDialog scan;
        private int mType = 0;
        private int mPosition = 0;

        public GetMediaFilesForFolder(Context ctx, int type, String path, int position) {
            this.context = ctx;
            mType = type;
            mPath = path;
            mPosition = position;
            mExtensions = extensions;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            scan =
                    new MaterialDialog.Builder(context)
                            .theme(Theme.LIGHT)
                            .titleColorRes(R.color.normal_blue)
                            .negativeColorRes(R.color.dialog_negetive_button)
                            .positiveColorRes(R.color.normal_blue)
                            .title("Listing Files")
                            .progress(true, 0)
                            .progressIndeterminateStyle(true)
                            .negativeText("Cancel")
                            .onNegative((dialog, which) -> {
                            })
                            .build();

            scan.show();
        }

        @Override
        protected ArrayList<QueueItem> doInBackground(Void... params) {

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                try {

                    mfiles.addAll(FileUtils.listFiles(new File(mPath), mExtensions, true));

                    for (final File file : mfiles) {
                        QueueItem queueItem = TrackRealmHelper.getTrack(file.getAbsolutePath());
                        if (queueItem != null) {
                            queueItems.add(queueItem);
                        }
                    }

                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            }

            return queueItems;
        }

        @Override
        protected void onPostExecute(ArrayList<QueueItem> queueItems) {
            super.onPostExecute(queueItems);
            scan.dismiss();
            if (queueItems.size() == 0) {
                AppController.toast(StorageActivity.this, "No music files found");
            } else {
                folderItems.clear();
                folderItems = queueItems;
                processFolderOption(mType, mPath, mPosition);
            }
        }
    }

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                switch (action) {
                    case AppController.INTENT_TRACK_SEEKED:
                        mainUpdate();
                        break;
                    case AppController.INTENT_QUEUE_CHANGED:
                        mainUpdate();
                        break;
                    case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();
                        break;
                    case AppController.INTENT_TRACK_REPEAT:
                        mainUpdate();
                        break;
                    case AppController.INTENT_TRACK_SHUFFLE:
                        mainUpdate();
                        break;
                    case AppController.INTENT_QUEUE_CLEARED:
                        miniPlayer.layoutMiniPlayer();

                        break;
                    case AppController.INTENT_CLEAR:
                        finish();
                        break;
                    case AppController.INTENT_EXIT:
                        finish();
                        break;
                    case AppController.INTENT_TRACK_EDITED:
                        int index = intent.getIntExtra("index", -1);
                        String tag = intent.getStringExtra("tag");
                        QueueItem item = (QueueItem) intent.getSerializableExtra("item");
                        if (item != null) {
                            if (!isFinishing()) {
                                mainUpdate();
                            }
                        }
                        break;
                }
            }
        }
    }
}
