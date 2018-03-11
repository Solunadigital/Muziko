package com.muziko.dialogs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.muziko.R;
import com.muziko.activities.StorageAddAccountActivity;
import com.muziko.adapter.UploadFileAdapter;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.cloud.Amazon.AmazonApi;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.CloudHelper;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.cloud.OneDrive.OneDriveApi;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.models.CloudAccount;
import com.muziko.tasks.CloudUploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import static android.content.ContentValues.TAG;
import static com.muziko.manager.CloudManager.cloudUploaderList;

public class UploadFile implements View.OnClickListener, BasicRecyclerItemListener, RecyclerItemListener, CloudFolderCallbacks {
    private final LinkedHashMap<Long, QueueItem> folderMap = new LinkedHashMap<>();
    private final ArrayList<QueueItem> folderList = new ArrayList<>();
    private final ArrayList<QueueItem> storagePath = new ArrayList<>();
    private final WeakHandler handler = new WeakHandler();
    private Context mContext;
    private UploadFileAdapter uploadFileAdapter;
    private Button cancelButton;
    private Button uploadButton;
    private ImageButton backButton;
    private ImageButton addButton;
    private boolean isBusy = false;
    private boolean isAdding = false;
    private boolean override = false;
    private boolean showOverride = false;
    private AlertDialog uploadDialog;
    private RecyclerView mRecyclerView;
    private HorizontalScrollView scrollview;
    private LinearLayout folderlayout;
    private RelativeLayout progressLayout;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private QueueItem queueItem;
    private BoxApi boxApi;
    private OneDriveApi oneDriveApi;
    private AmazonApi amazonApi;
    private int currentElement;
    private TextView textView;
    private RelativeLayout emptyCloudLayout;
    private final View.OnClickListener storageHeaderClick = v -> {
        try {

            CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(v.getId());
            if (cloudAccount != null) {
                if (v.getTag().equals("root") || v.getTag().equals("") || v.getTag().equals("0")) {
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
                uploadFileAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
                storagePath.clear();
                QueueItem drivesItem = CloudHelper.getDrivesItem();
                folderMap.put(drivesItem.id, drivesItem);
                storagePath.add(drivesItem);
                loadStorageRoot(false);
                updateStoragePath();
            } else {
                QueueItem queueItem = folderMap.get((long) v.getId());
                if (queueItem != null) {
                    storagePath.clear();
                    loadFolderPathRecursive(queueItem);
                    Collections.reverse(storagePath);
                    updateStoragePath();
                    openRootDrive(queueItem);
                }
            }

        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    };
    private Button emptyCloudButton;

    private void loadFolderPathRecursive(QueueItem queueItem) {
        storagePath.add(queueItem);
        for (QueueItem folderItem : folderMap.values()) {
            if (folderItem.id == queueItem.order) {
                loadFolderPathRecursive(folderItem);
            }
        }
    }

    private void loadStorageRoot(boolean scroll) {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        uploadFileAdapter.setShowArtwork(prefShowArtwork);

        folderList.clear();
        ArrayList<CloudAccount> cloudAccounts = CloudAccountRealmHelper.getCloudAccounts();
        for (CloudAccount cloudAccount : cloudAccounts) {
            QueueItem queue = CloudHelper.getCloudItem(cloudAccount);
            folderMap.put(queue.id, queue);
            folderList.add(queue);
        }

        if (uploadFileAdapter.getStorageMode() == CloudManager.StorageMode.DRIVES && cloudAccounts.size() == 0) {
            emptyCloudLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

        uploadFileAdapter.update();

        if (scroll && folderList.size() > 0) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    private void openRootDrive(QueueItem queueItem) {
        toggleRefresh(true);
        switch ((int) queueItem.album) {
            case CloudManager.GOOGLEDRIVE:
                uploadFileAdapter.setStorageMode(CloudManager.StorageMode.DRIVE);
                driveApi = (DriveApi) CloudManager.Instance().getCloudDrive(mContext, queueItem.storage);
                if (driveApi == null) {
                    AppController.toast(mContext, mContext.getString(R.string.cloud_drive_not_connected));
                } else {
                    driveApi.getFolderItems(queueItem, this);
                }
                break;

            case CloudManager.DROPBOX:
                uploadFileAdapter.setStorageMode(CloudManager.StorageMode.DROPBOX);
                dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(mContext, queueItem.storage);
                if (dropBoxApi == null) {
                    AppController.toast(mContext, mContext.getString(R.string.cloud_drive_not_connected));
                } else {
                    dropBoxApi.getFolderItems(queueItem, this);
                }
                break;

            case CloudManager.BOX:
                uploadFileAdapter.setStorageMode(CloudManager.StorageMode.BOX);
                boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(mContext, queueItem.storage);
                if (boxApi == null) {
                    AppController.toast(mContext, mContext.getString(R.string.cloud_drive_not_connected));
                } else {
                    boxApi.getFolderItems(queueItem, this);
                }
                break;

            case CloudManager.ONEDRIVE:
                uploadFileAdapter.setStorageMode(CloudManager.StorageMode.ONEDRIVE);
                oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(mContext, queueItem.storage);
                if (oneDriveApi == null) {
                    AppController.toast(mContext, mContext.getString(R.string.cloud_drive_not_connected));
                } else {
                    handler.postDelayed(() -> {
                        if (queueItem.data.equals("root")) {
                            oneDriveApi.getRootFolderItems(queueItem, this);
                        } else {
                            oneDriveApi.getFolderItems(queueItem, this);
                        }
                    }, 2000);
                }
                break;

            case CloudManager.AMAZON:
                uploadFileAdapter.setStorageMode(CloudManager.StorageMode.AMAZON);
                amazonApi = (AmazonApi) CloudManager.Instance().getCloudDrive(mContext, queueItem.storage);
                if (amazonApi == null) {
                    AppController.toast(mContext, mContext.getString(R.string.cloud_drive_not_connected));
                } else {
                    handler.postDelayed(() -> {
                        if (queueItem.data.equals("root")) {
                            amazonApi.getRootFolderItems(queueItem, this);
                        } else {
                            amazonApi.getFolderItems(queueItem, this);
                        }
                    }, 2000);
                }
                break;
        }


        if (uploadFileAdapter.getStorageMode() == CloudManager.StorageMode.DRIVES) {
            addButton.setVisibility(View.INVISIBLE);
            uploadButton.setVisibility(View.INVISIBLE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
        }
    }


    public void load(Context context, QueueItem queueItem) {
        mContext = context;
        this.queueItem = queueItem;

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_upload_file, null, false);

        findViewsById(view);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        folderList.clear();
        uploadFileAdapter = new UploadFileAdapter(mContext, folderList, PlayerConstants.QUEUE_TYPE_FOLDERS, prefShowArtwork, TAG, this);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(mContext);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new PicassoScrollListener(mContext, TAG));
        mRecyclerView.setLayoutManager(layoutList);
        mRecyclerView.setAdapter(uploadFileAdapter);

        emptyCloudButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);

        loadStorageRoot(false);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setView(view);
        uploadDialog = dialogBuilder.create();
        uploadDialog.show();
    }

    private void findViewsById(View view) {
        emptyCloudLayout = view.findViewById(R.id.emptyCloudLayout);
        emptyCloudButton = view.findViewById(R.id.emptyCloudButton);
        scrollview = view.findViewById(R.id.scrollview);
        folderlayout = view.findViewById(R.id.folderlayout);
        mRecyclerView = view.findViewById(R.id.itemList);
        progressLayout = view.findViewById(R.id.progressLayout);
        cancelButton = view.findViewById(R.id.cancelButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        addButton = view.findViewById(R.id.addButton);
        backButton = view.findViewById(R.id.backButton);
    }

    private void close() {

        if (uploadDialog != null) {
            uploadDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == emptyCloudButton) {
            Intent intent = new Intent(mContext, StorageAddAccountActivity.class);
            mContext.startActivity(intent);
            close();
        } else if (v == cancelButton) {
            close();
        } else if (v == backButton) {
            if (storagePath.size() > 1) {
                QueueItem currentItem = storagePath.get(storagePath.size() - 2);
                if (currentItem.id == R.id.drives) {
                    storagePath.remove(storagePath.size() - 1);
                    uploadFileAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
                    loadStorageRoot(false);
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
        } else if (v == addButton) {
            addFolder();
        } else if (v == uploadButton) {
            QueueItem currentItem = null;
            if (storagePath.size() > 1) {
                currentItem = storagePath.get(storagePath.size() - 1);
            }

            CloudAccount cloudAccount = CloudAccountRealmHelper.getCloudAccount(currentItem.storage);
            switch (cloudAccount.getCloudProvider()) {
                case (CloudManager.GOOGLEDRIVE):
                    driveApi =
                            (DriveApi)
                                    CloudManager.Instance()
                                            .getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    CloudUploadTask driveUploadTask =
                            new CloudUploadTask(
                                    mContext, driveApi.getDriveService(), queueItem, currentItem);
                    cloudUploaderList.put(queueItem.folder_path, driveUploadTask);
                    driveUploadTask.execute();

                    break;

                case (CloudManager.DROPBOX):
                    dropBoxApi =
                            (DropBoxApi)
                                    CloudManager.Instance()
                                            .getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    CloudUploadTask dropBoxUploadTask =
                            new CloudUploadTask(
                                    mContext,
                                    dropBoxApi.getDropBoxService(),
                                    queueItem,
                                    currentItem);
                    cloudUploaderList.put(queueItem.folder_path, dropBoxUploadTask);
                    dropBoxUploadTask.execute();

                    break;

                case (CloudManager.BOX):
                    boxApi =
                            (BoxApi)
                                    CloudManager.Instance()
                                            .getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    CloudUploadTask boxUploadTask =
                            new CloudUploadTask(
                                    mContext, boxApi.getBoxService(), queueItem, currentItem);
                    cloudUploaderList.put(queueItem.folder_path, boxUploadTask);
                    boxUploadTask.execute();

                    break;

                case (CloudManager.ONEDRIVE):
                    oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    CloudUploadTask oneDriveUploadTask = new CloudUploadTask(mContext, oneDriveApi.getOneDriveClient(), queueItem, currentItem);
                    cloudUploaderList.put(queueItem.folder_path, oneDriveUploadTask);
                    oneDriveUploadTask.execute();

                    break;

                case (CloudManager.AMAZON):
                    amazonApi = (AmazonApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                    CloudUploadTask amazonUploadTask = new CloudUploadTask(mContext, amazonApi.getOneDriveClient(), queueItem, currentItem);
                    cloudUploaderList.put(queueItem.folder_path, amazonUploadTask);
                    amazonUploadTask.execute();

                    break;
            }

            close();
        }
    }

    private void addFolder() {
        MaterialDialog createDialog =
                new MaterialDialog.Builder(mContext)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.black)
                        .negativeColorRes(R.color.dialog_negetive_button)
                        .title(R.string.new_string)
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel_caps)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(
                                mContext.getString(R.string.folder_name),
                                "",
                                (dialog, input) -> {
                                    if (input.toString().length() > 0) {
                                        QueueItem currentItem = null;
                                        if (storagePath.size() > 1) {
                                            currentItem = storagePath.get(storagePath.size() - 1);
                                        }
                                        QueueItem finalCurrentItem = currentItem;
                                        CloudAccount cloudAccount =
                                                CloudAccountRealmHelper.getCloudAccount(
                                                        currentItem.storage);
                                        switch (cloudAccount.getCloudProvider()) {
                                            case (CloudManager.GOOGLEDRIVE):
                                                driveApi =
                                                        (DriveApi)
                                                                CloudManager.Instance()
                                                                        .getCloudDrive(mContext,
                                                                                cloudAccount
                                                                                        .getCloudAccountId());
                                                AsyncJob.doInBackground(
                                                        () -> {
                                                            String newFolder1 =
                                                                    driveApi.newFolder(
                                                                            finalCurrentItem
                                                                                    .folder_path,
                                                                            input.toString());
                                                            final boolean result = true;
                                                            AsyncJob.doOnMainThread(
                                                                    () -> {
                                                                        if (result) {
                                                                            AppController.toast(
                                                                                    mContext,
                                                                                    "New folder created");
                                                                            loadFolder(
                                                                                    finalCurrentItem);
                                                                        }
                                                                    });
                                                        });

                                                break;

                                            case (CloudManager.DROPBOX):
                                                dropBoxApi = (DropBoxApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                                                AsyncJob.doInBackground(
                                                        () -> {
                                                            String newFolder1 = dropBoxApi.newFolder(finalCurrentItem.folder_path, input.toString());
                                                            final boolean result = true;
                                                            AsyncJob.doOnMainThread(
                                                                    () -> {
                                                                        if (result) {
                                                                            AppController.toast(mContext, "New folder created");
                                                                            loadFolder(finalCurrentItem);
                                                                        }
                                                                    });
                                                        });

                                                break;

                                            case (CloudManager.BOX):
                                                boxApi = (BoxApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                                                AsyncJob.doInBackground(
                                                        () -> {
                                                            String newFolder1 = boxApi.newFolder(finalCurrentItem.folder_path, input.toString());
                                                            final boolean result = true;
                                                            AsyncJob.doOnMainThread(
                                                                    () -> {
                                                                        if (result) {
                                                                            AppController.toast(mContext, "New folder created");
                                                                            loadFolder(finalCurrentItem);
                                                                        }
                                                                    });
                                                        });
                                                break;

                                            case (CloudManager.ONEDRIVE):
                                                oneDriveApi = (OneDriveApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                                                AsyncJob.doInBackground(
                                                        () -> {
                                                            String newFolder1 = oneDriveApi.newFolder(finalCurrentItem.folder_path, input.toString());
                                                            final boolean result = true;
                                                            AsyncJob.doOnMainThread(
                                                                    () -> {
                                                                        if (result) {
                                                                            AppController.toast(mContext, "New folder created");
                                                                            loadFolder(finalCurrentItem);
                                                                        }
                                                                    });
                                                        });

                                                break;

                                            case (CloudManager.AMAZON):
                                                amazonApi = (AmazonApi) CloudManager.Instance().getCloudDrive(mContext, cloudAccount.getCloudAccountId());
                                                AsyncJob.doInBackground(
                                                        () -> {
                                                            String newFolder1 = amazonApi.newFolder(finalCurrentItem.folder_path, input.toString());
                                                            final boolean result = true;
                                                            AsyncJob.doOnMainThread(
                                                                    () -> {
                                                                        if (result) {
                                                                            AppController.toast(mContext, "New folder created");
                                                                            loadFolder(finalCurrentItem);
                                                                        }
                                                                    });
                                                        });

                                                break;
                                        }

                                    } else {
                                        AppController.toast(
                                                mContext,
                                                mContext.getString(R.string.enter_folder_name));
                                    }
                                })
                        .build();

        createDialog.show();
    }

    private void updateStoragePath() {

        folderlayout.removeAllViews();

        for (int i = 0; i < storagePath.size(); i++) {
            QueueItem queueItem = storagePath.get(i);

            currentElement = storagePath.size();

            textView = new TextView(mContext);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            if (i != storagePath.size() - 1) {
                textView.setText(queueItem.title + " > ");
                textView.setAlpha(0.7f);
            } else {
                textView.setText(queueItem.title);
            }
            textView.setTextColor(Color.BLACK);
            textView.setTag(queueItem.data);
            textView.setTextSize(18f);
            textView.setBackgroundColor(Color.TRANSPARENT); // hex color 0xAARRGGBB
            textView.setPadding(5, 5, 5, 5);// in pixels (left, top, right, bottom)

            textView.setId((int) queueItem.id);

            if (queueItem.folder_path.equals("cloud")) {
                switch ((int) queueItem.album) {
                    case CloudManager.GOOGLEDRIVE:
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.drive_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.DROPBOX:
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dropbox_blue_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.BOX:
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.box_blue_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.ONEDRIVE:
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.onedrive_blue_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                    case CloudManager.AMAZON:
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.amazon_small, 0, 0, 0);
                        textView.setCompoundDrawablePadding(3);
                        break;
                }
            }
            textView.setOnClickListener(storageHeaderClick);
            folderlayout.addView(textView);

            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        }
    }


    @Override
    public void onDragTouched(RecyclerView.ViewHolder view) {

    }

    @Override
    public void onMenuClicked(int position) {

    }

    @Override
    public boolean onItemLongClicked(int position) {
        return false;
    }

    @Override
    public void onItemClicked(int position) {
        QueueItem item = uploadFileAdapter.getItem(position);
        if (item == null) return;
        handler.postDelayed(() -> {
            switch (uploadFileAdapter.getStorageMode()) {
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
                    open(uploadFileAdapter.getItem(position));
                    break;
            }
        }, mContext.getResources().getInteger(R.integer.ripple_duration_delay));
    }

    private void loadFolder(QueueItem currentFolder) {
        storagePath.clear();
        loadFolderPathRecursive(currentFolder);
        Collections.reverse(storagePath);
        updateStoragePath();
        openRootDrive(currentFolder);
    }

    private void open(final QueueItem queue) {
        if (queue.folder) {
            storagePath.add(queue);
            openRootDrive(queue);
        } else {
            int pos = -1;
            ArrayList<QueueItem> list = new ArrayList<>();
            for (QueueItem item : uploadFileAdapter.getList()) {
                if (!item.folder) {
                    if (item.data.equals(queue.data)) {
                        if (pos == -1) pos = list.size();

                    }
                    list.add(item);
                }
            }

            if (pos < 0) pos = 0;

            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_FOLDERS, pos, list);
        }

        updateStoragePath();
    }

    private void toggleRefresh(boolean refresh) {

        if (refresh) {
            progressLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            progressLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFoldersReturned(ArrayList<QueueItem> items) {
        for (QueueItem queueItem : items) {
            folderMap.put(queueItem.id, queueItem);
        }

        AsyncJob.doOnMainThread(() -> {
            toggleRefresh(false);
            folderList.clear();
            folderList.addAll(items);
            uploadFileAdapter.update();
        });
    }

    @Override
    public void onFoldersFailed() {
        AsyncJob.doOnMainThread(() -> {
            toggleRefresh(false);
        });

        AppController.toast(mContext, mContext.getString(R.string.problem_getting_folders));
    }
}
