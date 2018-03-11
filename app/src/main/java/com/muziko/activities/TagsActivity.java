package com.muziko.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.CustomToolbar;
import com.muziko.database.AlbumArtRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.LyricsHelper;
import com.muziko.helpers.MD5;
import com.muziko.helpers.MediaStoreHelper;
import com.muziko.helpers.SAFHelpers;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.tag;

public class TagsActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = TagsActivity.class.getSimpleName();
    private QueueItem item;
    private RelativeLayout progressLayout;
    private CircleProgressBar progressBar;
    private ImageView coverArtImage;
    private TextView progress;
    private TextView titletext;
    private TextView subtitletext;
    private CoordinatorLayout coordinatorlayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private EditText editTitle;
    private EditText editArtist;
    private EditText editAlbum;
    private AutoCompleteTextView editGenre;
    private EditText editNumber;
    private EditText editYear;
    private EditText editLyrics;
    private TextView lyricsSyncedTextView;
    private com.github.clans.fab.FloatingActionButton fab;
    private MyID3 id3 = new MyID3();
    private MusicMetadataSet dataset;
    private MusicMetadata metadata;
    private String artistName;
    private String albumName;
    private String genreName;
    private TagsUpdater task = null;
    private boolean saveRunning = false;
    private File newfile;
    private boolean lyricsChanged = false;
    private boolean isLrc = false;
    private CustomToolbar toolbar;
    private boolean isFav;
    private boolean isLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStartActivity(false);

        setContentView(R.layout.activity_tags);
        FindViewsById();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.normal_blue));
        collapsingToolbarLayout.setStatusBarScrimColor(ContextCompat.getColor(this, R.color.normal_blue));

        item = (QueueItem) getIntent().getSerializableExtra("item");

        if (item == null) {
            AppController.toast(this, "Song not found!");
            finish();
            return;
        }

        fab.hide(false);
        fab.setOnClickListener(this);

        editTitle.addTextChangedListener(new TextWatcher() {

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

        editArtist.addTextChangedListener(new TextWatcher() {

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

        editNumber.addTextChangedListener(new TextWatcher() {

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

        editLyrics.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    fab.show(true);
                    lyricsChanged = true;
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });

        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {

                    if (isOpen) {
                        appBarLayout.setExpanded(false, true);
                    } else {
                        appBarLayout.setExpanded(true, true);
                    }
                });

        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
        LoadRunnable loadRunnable = new LoadRunnable();

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
//            pendingRunnable = runnable;
//            new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Grant SD card permissions").content(message).positiveText("OK").onPositive((dialog, which) -> triggerStorageAccessFramework()).negativeText("Cancel").show();
//
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?", loadRunnable);
        } else {
            loadRunnable.run();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (TrackRealmHelper.getTrack(item.data).isLibrary()) {
            isLibrary = true;
        }

        if (TrackRealmHelper.getTrack(item.data).isSync()) {
            isFav = true;
        }
        artistName = item.artist_name;
        albumName = item.album_name;
        genreName = item.genre_name;

        titletext.setText(item.title);
        subtitletext.setText(item.album_name);

        List<String> entries = new ArrayList<>();
        ArrayList<QueueItem> genres = new ArrayList<>(TrackRealmHelper.getGenres().values());
        for (QueueItem genre : genres) {
            entries.add(genre.title);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, entries);
        editGenre.setAdapter(adapter);
        editGenre.setThreshold(1);

        Utils.hideKeyboard(this, editTitle);
        editTitle.clearFocus();

        loadcoverArt();

        AsyncJob.doInBackground(() -> {

        });
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    private void FindViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        titletext = findViewById(R.id.titletext);
        subtitletext = findViewById(R.id.subtitletext);
        editTitle = findViewById(R.id.editSearch);
        progressLayout = findViewById(R.id.progressLayout);
        progress = findViewById(R.id.progress);
        progressBar = findViewById(R.id.progressBar);
        coverArtImage = findViewById(R.id.coverArtImage);
        editTitle = findViewById(R.id.editTitle);
        editArtist = findViewById(R.id.editArtist);
        editAlbum = findViewById(R.id.editAlbum);
        editGenre = findViewById(R.id.editGenre);
        editNumber = findViewById(R.id.editNumber);
        editYear = findViewById(R.id.editYear);
        editLyrics = findViewById(R.id.editLyrics);
        lyricsSyncedTextView = findViewById(R.id.lyricsSyncedTextView);
        fab = findViewById(R.id.fab);

    }

    private void loadcoverArt() {

        Picasso.with(this)
                .load("content://media/external/audio/albumart/" + item.album)
                .tag(tag)
                .error(R.mipmap.placeholder)
                .fit()
                .centerCrop()
                .into(coverArtImage);

        toolbar.setItemColor(Color.WHITE);
    }


    private void InvalidFileExtensionError(String extension) {
        new MaterialDialog.Builder(TagsActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).title("Invalid file format")
                .content("This is a ." + extension + " file. Only mp3 files are supported.").positiveText("OK").onPositive((dialog, which) -> {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(TagsActivity.this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("prefStoragePerms", false);
            editor.apply();
            finish();
        }).show();

    }

    private void GeneralTagReadError() {
        new MaterialDialog.Builder(TagsActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).title("Error Reading Tags")
                .content("Unable to read tags from this track").positiveText("OK").onPositive((dialog, which) -> {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(TagsActivity.this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("prefStoragePerms", false);
            editor.apply();
            finish();
        }).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_tags, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;

            case R.id.action_goto:
                AppController.Instance().search(editTitle.getText().toString());
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
                        new Intent(TagsActivity.this, ShareWifiActivity.class);
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

    private void showData() {
        String title = metadata.getSongTitle();
        if (title != null) editTitle.setText(title);

        String artist = metadata.getArtist();
        if (artist != null) editArtist.setText(artist);

        String album = metadata.getAlbum();
        if (album != null) editAlbum.setText(album);

        String genre = metadata.getGenre();
        if (genre != null) editGenre.setText(genre);

        Number number = metadata.getTrackNumber();
        if (number != null) editNumber.setText(number.toString());

        Object year = metadata.get(MusicMetadata.KEY_YEAR);
        if (year != null) {
            if (year instanceof Number)
                editYear.setText(year.toString());
            else if (year instanceof String)
                editYear.setText(((String) year));
        }


        if (item.lyrics != null) {
            if (item.lRC) {
                isLrc = true;
                lyricsSyncedTextView.setVisibility(View.VISIBLE);
                String staticLyrics = LyricsHelper.getStaticLyrics(item.data);
                editLyrics.setText(Utils.fromHtml(staticLyrics));
            } else {
                isLrc = false;
                lyricsSyncedTextView.setVisibility(View.GONE);
                editLyrics.setText(Utils.fromHtml(item.lyrics));
            }
        }
    }

    private MusicMetadataSet readData() {
        MusicMetadataSet data = null;
        try {
            File from = new File(item.data);
            data = id3.read(from);      //read metadata
        } catch (Exception ex) {
            AppController.toast(this, "Unable to read song metadata!");
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("prefStoragePerms", false);
            editor.apply();
            Crashlytics.logException(ex);
            finish();
        }

        return data;
    }

    private void save() {

        if (saveRunning) {
            return;
        }

        saveRunning = true;

        progressLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        item.title = editTitle.getText().toString().trim();

        item.artist_name = editArtist.getText().toString().trim();

        item.album_name = editAlbum.getText().toString().trim();

        item.track = Utils.getInt(editNumber.getText().toString().trim(), 0);
        if (editYear.getText() != null) {
            item.year = Utils.getInt(editYear.getText().toString().trim(), -1);
        }
        item.genre_name = editGenre.getText().toString().trim();

        if (lyricsChanged) {
            isLrc = false;

            String lyrics = Utils.toHtml(editLyrics.getText());

            lyrics = lyrics.replaceAll("<(.*?)\\>", " ");//Removes all items in brackets
            lyrics = lyrics.replaceAll("<(.*?)\\\n", " ");//Must be undeneath
            lyrics = lyrics.replaceFirst("(.*?)\\>", " ");//Removes any connected item to the last bracket
            lyrics = lyrics.replaceAll("&nbsp;", " ");
            lyrics = lyrics.replaceAll("&amp;", " ");

            // clean up junk from html
//			String removedStuff = lyrics.replace("dir=\"ltr\">", "");

            item.lyrics = lyrics;
        }

        if (item.title.length() == 0) {
            item.title = "Unknown";
        }

        if (item.artist_name.length() == 0) {
            item.artist_name = "Unknown Artist";
        }

        if (item.album_name.length() == 0) {
            item.album_name = "Unknown Album";
        }

        try {

            if (item.genre_name.length() > 0) {

                long genreid = MediaStoreHelper.checkGenre(this, item);
                metadata.remove("genre_id");
                metadata.put("genre_id", genreid);


            } else {
                item.genre_name = "Unknown Genre";
                metadata.remove("genre_id");
                metadata.put("genre_id", -1);

            }


            metadata.setSongTitle(item.title);
            metadata.setArtist(item.artist_name);
            metadata.setAlbum(item.album_name);

            metadata.setGenre(item.genre_name);
            metadata.setTrackNumber(item.track);

            if (item.year != -1) {
                metadata.put("year", item.year);
            }

        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        done();

    }

    private void done() {

        if (task != null) {
            task.cancel(true);
            task = null;
        }

        task = new TagsUpdater(this);
        task.execute();

        progressLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        saveRunning = false;
        EventBus.getDefault().post(new RefreshEvent(1000));

        finish();
    }

    private class LoadRunnable implements Runnable {
        @Override
        public void run() {

            File from = new File(item.data);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && item.storage == 2) {
//                boolean getperms = false;
//                if (!PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
//                    DocumentFile targetDocument = SAFHelpers.getDocumentFile(openedExternalFile, false);
//                    if (targetDocument == null) {
//                        getperms = true;
//                    }
//                }
//            }

            String extension = FilenameUtils.getExtension(from.getAbsolutePath());
            try {
                dataset = id3.read(from);      //read metadata
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }

            if (dataset != null) {
                metadata = (MusicMetadata) dataset.getSimplified();
            }

            // Create a fake result (MUST be final)
            final boolean result = true;

            // Send the result to the UI thread and show it on a Toast
            AsyncJob.doOnMainThread(() -> {
                if (metadata == null || dataset == null) {
                    if (extension.equals("mp3")) {
                        GeneralTagReadError();
                    } else {
                        InvalidFileExtensionError(extension);
                    }
                } else {
                    showData();
                }
            });
        }
    }

    public class TagsUpdater extends AsyncTask<String, String, Boolean> {
        private Context ctx;
        private ArrayList<QueueItem> list = new ArrayList<>();

        public TagsUpdater(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                newfile = new File(item.data);
                File tempfile = null;

                if (item.storage == 2) {
                    if (PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
                        return false;
                    }
                    tempfile = id3.update(newfile, dataset, metadata, true);
                    DocumentFile targetDocument = SAFHelpers.getDocumentFile(newfile, false);
                    if (targetDocument == null) {
                        return false;
                    }

                    OutputStream out = getContentResolver().openOutputStream(targetDocument.getUri());

                    try {
                        byte[] bytes = FileUtils.readFileToByteArray(tempfile);
                        out.write(bytes);
                        out.close();
                    } catch (IOException e) {
                        Crashlytics.logException(e);
                    }

                    tempfile.delete();
                } else {
                    id3.update(newfile, dataset, metadata, false);
                }

                TrackRealmHelper.updateTrackTags(item, lyricsChanged, isLrc);

                if (isLibrary) {
                    FirebaseManager.Instance().deleteLibrary(item);
                }

                if (isFav) {
                    FirebaseManager.Instance().deleteFav(item);
                }

                item.md5 = MD5.calculateMD5(new File(item.data));
                TrackRealmHelper.updateMD5Hash(item);

                if (isLibrary) {
                    TrackRealmHelper.toggleLibrary(item, isLibrary);
                    FirebaseManager.Instance().uploadLibrary(item);
                }

                if (isFav) {
                    TrackRealmHelper.toggleSync(item, true);
                    TrackRealmHelper.toggleFavorite(item, true);
                    FirebaseManager.Instance().uploadFav(item);
                }

                MyApplication.ignoreNextMediaScan = true;

                // Update queue list
                for (QueueItem queue : PlayerConstants.QUEUE_LIST) {
                    if (item.data.equals(queue.data)) {
                        queue.setTags(item);
                    }
                }
                // Update queue song
                if (PlayerConstants.QUEUE_SONG.data.equals(item.data)) {
                    PlayerConstants.QUEUE_SONG = item;
                }

                AlbumArtRealmHelper.updateStatus(item.artist_name, item.album_name);
                MediaStoreHelper.updateSongTags(ctx, item);
//                Utils.scanMedia(ctx, item.data);



                AppController.Instance().serviceDirty();

                Intent trackEditIntent = new Intent(AppController.INTENT_TRACK_EDITED);
                if (albumName.equals(item.album_name)) {
                    trackEditIntent.putExtra("tag", getIntent().getStringExtra("tag"));
                    trackEditIntent.putExtra("index", getIntent().getIntExtra("index", -1));
                    trackEditIntent.putExtra("item", item);
                }
                sendBroadcast(trackEditIntent);
                sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

                AppController.toast(TagsActivity.this, "MP3 tags saved!");

                return true;
            } catch (IOException e) {
                Crashlytics.logException(e);
                return false;
            } catch (Exception e) {
                Crashlytics.logException(e);
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progressBar.setVisibility(trackList.size() > 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                AppController.toast(ctx, "There was a problem saving the tags");
            }
        }
    }

}
