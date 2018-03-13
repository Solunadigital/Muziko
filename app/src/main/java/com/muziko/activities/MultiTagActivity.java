package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.AlbumArtRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.MediaStoreHelper;
import com.muziko.helpers.SAFHelpers;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.muziko.tageditor.common.ID3WriteException;
import com.muziko.tageditor.metadata.MusicMetadata;
import com.muziko.tageditor.metadata.MusicMetadataSet;
import com.muziko.tageditor.myid3.MyID3;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiTagActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = MultiTagActivity.class.getSimpleName();
    private final int FILE_SELECT_CODE = 390;
    final private int REQUEST_CODE_STORAGE_ACCESS = 890;
    private final ArrayList<QueueItem> multiTagList = new ArrayList<>();
    private final ArrayList<MusicMetadataSet> multiTagMetaDataSetList = new ArrayList<>();
    private final ArrayList<MusicMetadata> multiTagMetaDataList = new ArrayList<>();

    private final ArrayList<MusicMetadataSet> finalMultiTagMetaDataSetList = new ArrayList<>();
    private final ArrayList<MusicMetadata> finalMultiTagMetaDataList = new ArrayList<>();
    private final WeakHandler handler = new WeakHandler();
    private final MyID3 id3 = new MyID3();
    private MediaScannerConnection scanner = null;
    private CoordinatorLayout coordinatorlayout;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MiniPlayer miniPlayer;
    private Toolbar toolbar;
    private EditText editArtist;
    private EditText editAlbum;
    private AutoCompleteTextView editGenre;
    private EditText editYear;
    private com.github.clans.fab.FloatingActionButton fab;
    private TagsUpdater task = null;
    private boolean saveRunning = false;
    private boolean saved = false;
    private ActionBar actionBar;
    private MainReceiver mainReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStartActivity(false);

        setContentView(R.layout.activity_multi_tag);

        findViewsById();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));
        }

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        if (MyApplication.multiTagList == null || MyApplication.multiTagList.size() == 0) {
            AppController.toast(this, "Songs not found!");
            finish();
            return;
        }

        fab.setOnClickListener(this);
        fab.hide(false);

        List<String> entries = new ArrayList<>();
        ArrayList<QueueItem> genres = new ArrayList<>(TrackRealmHelper.getGenres().values());
        for (QueueItem genre : genres) {
            entries.add(genre.title);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);
        editGenre.setAdapter(adapter);
        editGenre.setThreshold(1);


        Utils.hideKeyboard(this, editArtist);
        editArtist.clearFocus();
        setupMainPlayer();


        LoadSongsRunnable songsRunnable = new LoadSongsRunnable();
        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
        if (storageInfoList.size() > 1) {
            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?", songsRunnable);
        } else {
            songsRunnable.run();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mainUpdate();
        register();
        multiTagList.clear();
        multiTagMetaDataSetList.clear();
        finalMultiTagMetaDataSetList.clear();
        finalMultiTagMetaDataList.clear();

        actionBar.setTitle(MyApplication.multiTagList.size() + " tracks");

        loadSongs();

        editArtist.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.show(true);
                }

            }


            public void afterTextChanged(Editable s) {

            }
        });

        editAlbum.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.show(true);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        editGenre.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.show(true);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        editYear.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.show(true);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (slidingUpPanelLayout != null &&
                (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            miniPlayer.close();
            miniPlayer.open();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        unregister();
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {

        if (miniPlayer != null) {
            miniPlayer.showBufferingMessage(event.getMessage(), event.isClose());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(NetworkEvent networkEvent) {

        if (miniPlayer != null) {
            miniPlayer.updateUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {

        miniPlayer.updateProgress(event.getProgress(), event.getDuration());
    }

    private void onLayoutChanged(Float bottomMargin) {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);

        Resources resources = getResources();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentlayout.getLayoutParams();
        params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
        contentlayout.requestLayout();
    }

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();

    }

    private void findViewsById() {

        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        toolbar = findViewById(R.id.toolbar);
        editArtist = findViewById(R.id.editArtist);
        editAlbum = findViewById(R.id.editAlbum);
        editGenre = findViewById(R.id.editGenre);
        editYear = findViewById(R.id.editYear);
        fab = findViewById(R.id.fab);
    }

    private void loadSongs() {
        ArrayList<QueueItem> removelist = new ArrayList<>();

        for (int i = 0; i < MyApplication.multiTagList.size(); i++) {

            QueueItem multiQueueItem = MyApplication.multiTagList.get(i);
            MusicMetadataSet itemDataset = readData(multiQueueItem);

            if (itemDataset == null) {
                AppController.toast(this, "Unable to read song metadata from " + multiQueueItem.data);
                removelist.add(multiQueueItem);
                continue;
            }

            MusicMetadata itemMetadata = (MusicMetadata) itemDataset.getSimplified();

            if (itemMetadata == null) {
                AppController.toast(this, "Unable to read song metadata from " + multiQueueItem.data);
                removelist.add(multiQueueItem);
                continue;
            }

            multiTagMetaDataSetList.add(itemDataset);
            multiTagMetaDataList.add(itemMetadata);

        }

        for (QueueItem removeItem : removelist) {
            MyApplication.multiTagList.remove(removeItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_multi_tags, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;

            case R.id.action_goto:
                AppController.Instance().search(editAlbum.getText().toString());
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.action_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(MultiTagActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.action_exit:
                AppController.Instance().exit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {

        if (view == fab) {

            save();

        }
    }

    private MusicMetadataSet readData(QueueItem queueItem) {
        MusicMetadataSet data = null;
        try {
            File from = new File(queueItem.data);
            data = id3.read(from);      //read metadata
        } catch (Exception ex) {
            String fileNameWithoutExt = FilenameUtils.getBaseName(queueItem.data);
            AppController.toast(this, "Unable to read song metadata - " + fileNameWithoutExt);
            Crashlytics.logException(ex);
        }

        return data;
    }

    private void save() {
        saved = false;

        multiTagList.clear();

        if (saveRunning) {
            return;
        }

        saveRunning = true;

        for (int i = 0; i < MyApplication.multiTagList.size(); i++) {
            MusicMetadataSet itemDataset;
            MusicMetadata itemMetadata;
            byte[] bitmap = null;

            QueueItem queueItem = MyApplication.multiTagList.get(i);
            itemMetadata = multiTagMetaDataList.get(i);
            itemDataset = multiTagMetaDataSetList.get(i);


            if (!editArtist.getText().toString().isEmpty()) {
                queueItem.artist_name = editArtist.getText().toString();
                itemMetadata.setArtist(editArtist.getText().toString());
                saved = true;
            }

            if (!editAlbum.getText().toString().trim().isEmpty()) {
                queueItem.album_name = editAlbum.getText().toString().trim();
                itemMetadata.setAlbum(editAlbum.getText().toString().trim());
                saved = true;
            }

            if (!editGenre.getText().toString().trim().isEmpty()) {
                queueItem.genre_name = editGenre.getText().toString().trim();
                itemMetadata.setGenre(editGenre.getText().toString().trim());

                saved = true;

                long genreid = MediaStoreHelper.checkGenre(this, queueItem);

                itemMetadata.remove("genre_id");
                itemMetadata.put("genre_id", genreid);


            }

            if (!editYear.getText().toString().trim().isEmpty()) {
                queueItem.year = Utils.getInt(editYear.getText().toString().trim(), -1);
                itemMetadata.put("year", Utils.getInt(editYear.getText().toString().trim(), -1));
                saved = true;
            }

            multiTagList.add(queueItem);
            finalMultiTagMetaDataSetList.add(itemDataset);
            finalMultiTagMetaDataList.add(itemMetadata);

        }

        done();
    }

    private void done() {

        if (saved) {

            MyApplication.ignoreNextMediaScan = true;

            if (task != null) {
                task.cancel(true);
                task = null;
            }

            task = new TagsUpdater(this);
            task.execute();
        } else {

            saveRunning = false;

        }
    }

    private void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_TRACK_SEEKED);
        filter.addAction(AppController.INTENT_QUEUE_CHANGED);
        filter.addAction(AppController.INTENT_QUEUE_STOPPED);
        filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
        filter.addAction(AppController.INTENT_TRACK_REPEAT);
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

    private class LoadSongsRunnable implements Runnable {
        @Override
        public void run() {
            ArrayList<QueueItem> removelist = new ArrayList<>();

            for (int i = 0; i < MyApplication.multiTagList.size(); i++) {

                QueueItem multiQueueItem = MyApplication.multiTagList.get(i);
                MusicMetadataSet itemDataset = readData(multiQueueItem);

                if (itemDataset == null) {
                    AppController.toast(MultiTagActivity.this, "Unable to read song metadata from " + multiQueueItem.data);
                    removelist.add(multiQueueItem);
                    continue;
                }

                MusicMetadata itemMetadata = (MusicMetadata) itemDataset.getSimplified();

                if (itemMetadata == null) {
                    AppController.toast(MultiTagActivity.this, "Unable to read song metadata from " + multiQueueItem.data);
                    removelist.add(multiQueueItem);
                    continue;
                }

                multiTagMetaDataSetList.add(itemDataset);
                multiTagMetaDataList.add(itemMetadata);

            }

            for (QueueItem removeItem : removelist) {
                MyApplication.multiTagList.remove(removeItem);
            }
        }
    }

    public class TagsUpdater extends AsyncTask<String, String, Boolean> {
        private final Context ctx;
        private File dst;
        private MaterialDialog materialDialog;
        private boolean running = true;
        private int tagsCount = 0;

        public TagsUpdater(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                for (int i = 0; i < multiTagList.size(); i++) {

                    if (!running) break;
                    final QueueItem queueItem = multiTagList.get(i);

                    publishProgress(org.apache.commons.lang3.StringUtils.abbreviate(multiTagList.get(i).title, 30));

                    MusicMetadataSet itemDataset = multiTagMetaDataSetList.get(i);
                    MusicMetadata itemMetadata = multiTagMetaDataList.get(i);

                    File newfile = new File(queueItem.data);
                    File tempfile;
                    id3.update(dst, itemDataset, itemMetadata, false);

                    if (queueItem.storage == 0) {
                        if (PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
                            break;
                        }
                        tempfile = id3.update(newfile, itemDataset, itemMetadata, true);
                        DocumentFile targetDocument = SAFHelpers.getDocumentFile(newfile, false);
                        if (targetDocument == null) {
                            break;
                        }

                        OutputStream out = getContentResolver().openOutputStream(targetDocument.getUri());

                        try {
                            byte[] bytes = FileUtils.readFileToByteArray(tempfile);
                            out.write(bytes);
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        tempfile.delete();
                    } else {
                        id3.update(newfile, itemDataset, itemMetadata, false);
                    }

                    AlbumArtRealmHelper.updateStatus(queueItem.artist_name, queueItem.album_name);

                    MediaStoreHelper.updateSongTags(ctx, queueItem);
                    Utils.scanMedia(ctx, queueItem.data);

                    TrackRealmHelper.updateMultiTrackTags(queueItem);

                    if (TrackRealmHelper.getTrack(queueItem.data).isLibrary()) {
                        FirebaseManager.Instance().deleteLibraryForUpdate(queueItem);
                    }

                    if (TrackRealmHelper.getTrack(queueItem.data).isSync()) {
                        FirebaseManager.Instance().deleteFavForUpdate(queueItem);
                    }

                    // Update queue list
                    for (QueueItem queue : PlayerConstants.QUEUE_LIST) {
                        if (queueItem.data.equals(queue.data)) {
                            queue.setTags(queueItem);
                        }
                    }

                    // Update queue song
                    if (PlayerConstants.QUEUE_SONG.data.equals(queueItem.data)) {
                        PlayerConstants.QUEUE_SONG = queueItem;
                    }
                }

                return true;
            }
         /*   catch (ID3WriteException ex) {
                Crashlytics.logException(ex);
                return false;
            }*/
            catch (IOException ex) {
                Crashlytics.logException(ex);
                return false;
            } catch (Exception ex) {
                Crashlytics.logException(ex);
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            materialDialog = new MaterialDialog.Builder(MultiTagActivity.this)
                    .title("Saving Tags")
                    .content("Saving")
                    .progress(false, multiTagList.size(), true)
                    .autoDismiss(false)
                    .negativeText("Cancel").onNegative((dialog, which) -> {
                        running = false;
                        dialog.dismiss();
                    })
                    .neutralText("Background").onNeutral((dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            saveRunning = false;
            materialDialog.dismiss();

            if (!result) {
                AppController.toast(ctx, "There was a problem saving the tags");
            } else {

                if (running) {
                    AppController.toast(MultiTagActivity.this, "MP3 tags saved.");
                } else {
                    AppController.toast(MultiTagActivity.this, "Cancelled. MP3 tags for " + tagsCount + " songs were saved.");

                }

                sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));

                sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));

                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

                AppController.Instance().serviceDirty();

                EventBus.getDefault().post(new RefreshEvent(1000));

            }


        }

        @Override
        protected void onProgressUpdate(String... values) {

            tagsCount++;
            materialDialog.setContent(values[0]);
            materialDialog.incrementProgress(1);

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
                }
            }
        }
    }
}
