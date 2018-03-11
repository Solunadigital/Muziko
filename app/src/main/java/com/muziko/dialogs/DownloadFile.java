package com.muziko.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
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
import com.muziko.adapter.DownloadFileAdapter;
import com.muziko.callbacks.CloudFolderCallbacks;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.SAFHelpers;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import static android.content.ContentValues.TAG;
import static com.muziko.manager.CloudManager.StorageMode.LOCAL;

public class DownloadFile implements View.OnClickListener, BasicRecyclerItemListener, RecyclerItemListener, CloudFolderCallbacks {
    private final LinkedHashMap<Long, QueueItem> folderMap = new LinkedHashMap<>();
    private final ArrayList<QueueItem> folderList = new ArrayList<>();
    private final ArrayList<QueueItem> storagePath = new ArrayList<>();
    private final String rootPath = "/";
    private final WeakHandler handler = new WeakHandler();
    private Context mContext;
    private DownloadFileAdapter downloadFilesAdapter;
    private Button cancelButton;
    private Button downloadButton;
    private ImageButton backButton;
    private ImageButton addButton;
    private AlertDialog downloadDialog;
    private RecyclerView mRecyclerView;
    private HorizontalScrollView scrollview;
    private LinearLayout folderlayout;
    private RelativeLayout progressLayout;
    private QueueItem queueItem;
    private int currentElement;
    private TextView textView;
    private String currentPath = "";
    private String[] paths = currentPath.split("/");
    private final View.OnClickListener storageHeaderClick = v -> {
        try {
            if (v.getId() == R.id.root) {
                downloadFilesAdapter.setStorageMode(CloudManager.StorageMode.DRIVES);
                storagePath.clear();
                currentPath = "/storage/";
                updateStoragePath();
                loadLocal(false);
            } else {
                String path = "";
                for (int j = 0; j <= v.getId(); j++) {
                    path = path + paths[j] + "/";
                }
                if (path.equals("/storage/emulated/")) {
                    path = "/storage/emulated/0";
                }
                currentPath = path;
                loadLocal(false);
                updateStoragePath();
            }

        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    };
    private CloudTrack cloudTrack;
    private MuzikoConstants.FirebaseFileMode firebaseFileMode;
    private String playlistHash;

    private void loadFolderPathRecursive(QueueItem queueItem) {
        storagePath.add(queueItem);
        for (QueueItem folderItem : folderMap.values()) {
            if (folderItem.id == queueItem.order) {
                loadFolderPathRecursive(folderItem);
            }
        }
    }

    private void loadLocal(boolean scroll) {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        downloadFilesAdapter.setShowArtwork(prefShowArtwork);

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

        downloadFilesAdapter.update();

        plist.clear();
        slist.clear();

        if (scroll && folderList.size() > 0) {
            mRecyclerView.smoothScrollToPosition(0);
        }
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

    private void openRootDrive(QueueItem queueItem) {
        toggleRefresh(true);

        if (!queueItem.data.equalsIgnoreCase("root")) {
            File folder = new File(queueItem.getData());
            currentPath = folder.getPath();
        }

        downloadFilesAdapter.setStorageMode(LOCAL);
        loadLocal(false);
        updateStoragePath();
        toggleRefresh(false);


        if (downloadFilesAdapter.getStorageMode() == CloudManager.StorageMode.DRIVES) {
            addButton.setVisibility(View.INVISIBLE);
            downloadButton.setVisibility(View.INVISIBLE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.VISIBLE);
        }
    }

    public void init(Context context, QueueItem queueItem) {
        mContext = context;
        this.queueItem = queueItem;
        load();
    }

    public void init(Context context, CloudTrack cloudTrack, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = context;
        this.cloudTrack = cloudTrack;
        this.firebaseFileMode = firebaseFileMode;
        load();
    }

    public void init(Context context, CloudTrack cloudTrack, String playlistHash, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = context;
        this.cloudTrack = cloudTrack;
        this.playlistHash = playlistHash;
        this.firebaseFileMode = firebaseFileMode;
        load();
    }

    public void load() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_download_file, null, false);

        findViewsById(view);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        folderList.clear();
        downloadFilesAdapter = new DownloadFileAdapter(mContext, folderList, PlayerConstants.QUEUE_TYPE_FOLDERS, prefShowArtwork, TAG, this);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(mContext);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new PicassoScrollListener(mContext, TAG));
        mRecyclerView.setLayoutManager(layoutList);
        mRecyclerView.setAdapter(downloadFilesAdapter);

        backButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);

        currentPath = "/storage/";
        updateStoragePath();
        loadLocal(false);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setView(view);
        downloadDialog = dialogBuilder.create();
        downloadDialog.show();
    }

    private void findViewsById(View view) {
        scrollview = view.findViewById(R.id.scrollview);
        folderlayout = view.findViewById(R.id.folderlayout);
        mRecyclerView = view.findViewById(R.id.itemList);
        progressLayout = view.findViewById(R.id.progressLayout);
        cancelButton = view.findViewById(R.id.cancelButton);
        downloadButton = view.findViewById(R.id.downloadButton);
        addButton = view.findViewById(R.id.addButton);
        backButton = view.findViewById(R.id.backButton);
    }

    private void close() {

        if (downloadDialog != null) {
            downloadDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == cancelButton) {
            close();
        } else if (v == backButton) {
            int backOne = currentElement - 1;
            if (backOne > 1) {
                String path = "";
                final String[] paths = currentPath.split("/");
                if (paths.length > 0) {
                    for (int j = 0; j < backOne; j++) {
                        path = path + paths[j] + "/";
                    }
                }
                currentPath = path;
                loadLocal(false);
                updateStoragePath();
            }
        } else if (v == addButton) {
            addFolder();
        } else if (v == downloadButton) {
            if (queueItem != null) {
                CloudManager.Instance().downloadTrack(queueItem, currentPath);
            } else {

                switch (firebaseFileMode) {
                    case LIBRARY:
                        FirebaseManager.Instance().downloadLibrary(cloudTrack);
                        break;
                    case FAVS:
                        FirebaseManager.Instance().downloadFav(cloudTrack, currentPath);
                        break;
                    case PLAYLISTS:
                        FirebaseManager.Instance().downloadPlaylistTrack(cloudTrack, playlistHash, currentPath);
                        break;
                }

            }

            close();
        }
    }

    private void addFolder() {
        MaterialDialog createDialog =
                new MaterialDialog.Builder(mContext).theme(Theme.LIGHT).titleColorRes(R.color.black).negativeColorRes(R.color.dialog_negetive_button)
                        .title(R.string.new_string).positiveText(R.string.ok).negativeText(R.string.cancel_caps).inputType(InputType.TYPE_CLASS_TEXT)
                        .input(mContext.getString(R.string.folder_name), "", (dialog, input) -> {
                            if (input.toString().length() > 0) {
                                if (paths.length > 1) {
                                    if (!currentPath.endsWith("/")) {
                                        currentPath += "/";
                                    }

                                    String parentdir = currentPath + input.toString();
                                    File directory = new File(parentdir);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileHelper.isOnExtSdCard(mContext, directory)) {
                                        DocumentFile targetDocument = null;
                                        try {
                                            targetDocument = SAFHelpers.getDocumentFile(directory, true);
                                        } catch (Exception e) {
                                            Crashlytics.logException(e);
                                        }

                                        if (targetDocument != null) {
                                            return;
                                        }
                                    } else {
                                        if (!directory.exists()) {
                                            directory.mkdirs();
                                        }
                                    }

                                    loadLocal(true);
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

            textView.setOnClickListener(storageHeaderClick);
            folderlayout.addView(textView);

            scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
        }

        if (downloadFilesAdapter.getStorageMode() == LOCAL) {
            paths = currentPath.split("/");

            for (int i = 0; i < paths.length; i++) {
                String element = paths[i];

                currentElement = paths.length;

                textView = new TextView(mContext);
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
                textView.setTextColor(Color.BLACK);
                textView.setTag(String.valueOf(i));
                textView.setTextSize(18f);
                textView.setBackgroundColor(Color.TRANSPARENT); // hex color 0xAARRGGBB
                textView.setPadding(5, 5, 5, 5); // in pixels (left, top, right, bottom)

                textView.setId(i);
                textView.setOnClickListener(storageHeaderClick);
                folderlayout.addView(textView);

                scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
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
        QueueItem item = downloadFilesAdapter.getItem(position);
        if (item == null) return;
        handler.postDelayed(() -> {
            switch (downloadFilesAdapter.getStorageMode()) {
                case DRIVES:
                    storagePath.clear();
                    folderMap.put(item.id, item);
                    updateStoragePath();
                    openRootDrive(item);
                    break;
                default:
                    open(downloadFilesAdapter.getItem(position));
                    break;
            }
        }, mContext.getResources().getInteger(R.integer.ripple_duration_delay));
    }

    private void open(final QueueItem queue) {
        if (queue.folder) {
            currentPath = queue.data;
            loadLocal(true);
        } else {
            int pos = -1;
            ArrayList<QueueItem> list = new ArrayList<>();
            for (QueueItem item : downloadFilesAdapter.getList()) {
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
            downloadFilesAdapter.update();
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
