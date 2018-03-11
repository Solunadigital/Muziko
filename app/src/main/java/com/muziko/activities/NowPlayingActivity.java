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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
//import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.cleveroad.fanlayoutmanager.FanLayoutManager;
import com.cleveroad.fanlayoutmanager.FanLayoutManagerSettings;
import com.cleveroad.fanlayoutmanager.callbacks.FanChildDrawingOrderCallback;
import com.crashlytics.android.Crashlytics;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.clans.fab.FloatingActionButton;
import com.github.florent37.expectanim.ExpectAnim;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.CoverArtAdapter;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.Lyrics;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.NonFocusableNestedScrollView;
import com.muziko.controls.ScrollingLyricsView;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.PlayFrom;
import com.muziko.dialogs.SetRingtone;
import com.muziko.dialogs.ShareRingtone;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.LyricsTextFactory;
import com.muziko.helpers.SAFHelpers;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.CoverArtRecyclerListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.ImageManager;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.service.SongService;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.LyricsDownloader;
import com.muziko.widgets.QueueWidget;
import com.muziko.widgets.StandardWidget;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tr4android.support.extension.drawable.MediaControlDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;
import io.techery.properratingbar.ProperRatingBar;
import io.techery.properratingbar.RatingListener;

import static com.github.florent37.expectanim.core.Expectations.centerHorizontalInParent;
import static com.github.florent37.expectanim.core.Expectations.centerInParent;
import static com.github.florent37.expectanim.core.Expectations.outOfScreen;
import static com.github.florent37.expectanim.core.Expectations.toHaveBackgroundAlpha;
import static com.github.florent37.expectanim.core.Expectations.topOfParent;
import static com.muziko.MyApplication.networkState;
import static com.muziko.R.id.covertArtList;
import static com.muziko.R.id.lyrics;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_STORAGE_ACCESS;
import static io.realm.log.RealmLog.clear;

public class NowPlayingActivity extends BaseActivity
        implements View.OnClickListener,
        View.OnLongClickListener,
        Lyrics.Callback,
        SeekBar.OnSeekBarChangeListener,
        CoverArtRecyclerListener {
    private final String TAG = NowPlayingActivity.class.getSimpleName();
    TransitionDrawable blurredTransitionDrawable;
    Drawable blurredDrawable;
    private PercentRelativeLayout mainPlayerLayout;
    private WeakHandler handler = new WeakHandler();
    private CircleProgressBar progressBar;
    private RelativeLayout lyriclayout;
    private NonFocusableNestedScrollView lyricsScrollView;
    private TextSwitcher switcher;
    private ScrollingLyricsView lrcView;
    private RelativeLayout errorLayout;
    private TextView bugtext;
    private RelativeLayout innerLayout;
    private TextView texttrack;
    private TextView textartist;
    private TextView texttrackLyrics;
    private TextView textartistLyrics;
    private CircleProgressBar bufferProgressBar;
    private FrameLayout playLayout;
    private ImageView buttonPlayMain;
    private ImageButton buttonPrevMain;
    private ImageButton buttonNextMain;
    private ImageButton buttonCut;
    private ImageButton buttonEqualizer;
    private ImageButton buttonSleep;
    private ImageButton buttonPlaylist;
    private ImageButton buttonRepeat;
    private ImageButton buttonShuffle;
    private ImageButton buttonDelete;
    private SeekBar seekBarMini;
    private TextView startText;
    private TextView endText;
    private FloatingActionButton closeFab;
    private ProperRatingBar ratingBar;
    private Toolbar toolbar;
    private LinearLayout mainLayout;
    private ImageView coverArtImage;
    private boolean isSeekTouchCompact = false;
    private boolean adapterIsUpdating = false;
    private MainReceiver mainReceiver;
    private Lyrics mLyrics;
    private boolean lyricsShown = false;
    private Thread mLrcThread;
    private MenuItem menu_lyrics;
    private MenuItem menuFav;
    private MenuItem menuRingtone;
    private MenuItem menuAuClip;
    private MenuItem menuStartFrom;
    private MenuItem menuTagEdit;
    private QueueItem selectedItem;
    private MediaControlDrawable mediaControlDrawable;
    private ScrollingLyrics scrollingLyrics = null;
    private boolean isFaving = false;
    private RecyclerView coverArtRecyclerView;
    private FanLayoutManager coverArtFanLayoutManager;
    private boolean showingCoverArtScroller;
    private TapTargetSequence sequence;
    private int currentQueuePosition = -1;
    private File openedExternalFile;

    private Target target =
            new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // loading of the bitmap was a success
                    if (showingCoverArtScroller) {

                        setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
                        blurredAlbumArt.execute(bitmap);

                    } else {
                        coverArtImage.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    // loading of the bitmap failed
                    coverArtImage.setImageResource(R.mipmap.placeholder);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
    private boolean buffering;
    private CoverArtAdapter coverArtAdapter;

    @DebugLog
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_now_playing);
        findViewsById();

        new ExpectAnim()
                .expect(closeFab)
                .toBe(outOfScreen(Gravity.TOP), centerHorizontalInParent())
                .toAnimation()
                .setDuration(0)
                .start();

        new ExpectAnim()
                .expect(coverArtRecyclerView)
                .toBe(outOfScreen(Gravity.BOTTOM), toHaveBackgroundAlpha(0f))
                .toAnimation()
                .setDuration(0)
                .start();

        toolbar.setTitle(R.string.now_playing);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        float densityScale = getResources().getDisplayMetrics().density;
        mediaControlDrawable =
                new MediaControlDrawable.Builder(this)
                        .setColor(Color.WHITE)
                        .setPadding(8 * densityScale)
                        .setInitialState(MediaControlDrawable.State.PLAY)
                        .build();
        buttonPlayMain.setImageDrawable(mediaControlDrawable);

        buttonRepeat.setOnClickListener(this);
        buttonShuffle.setOnClickListener(this);
        buttonPlayMain.setOnClickListener(this);
        buttonPrevMain.setOnClickListener(this);
        buttonNextMain.setOnClickListener(this);
        buttonCut.setOnClickListener(this);
        buttonEqualizer.setOnClickListener(this);
        buttonSleep.setOnClickListener(this);
        buttonPlaylist.setOnClickListener(this);
        coverArtImage.setOnClickListener(this);
        closeFab.setOnClickListener(this);

        buttonPrevMain.setOnLongClickListener(this);

        seekBarMini.setVisibility(View.VISIBLE);

        seekBarMini.setOnSeekBarChangeListener(this);
        seekBarMini.setMax(Integer.parseInt(PlayerConstants.QUEUE_SONG.duration));
        seekBarMini.setProgress(0);
        endText.setText(Utils.getDuration(Long.parseLong(PlayerConstants.QUEUE_SONG.duration)));

        ratingBar.setRating(PlayerConstants.QUEUE_SONG.rating);
        RatingListener ratingListener =
                ratingBar1 -> {
                    PlayerConstants.QUEUE_SONG.rating = ratingBar1.getRating();
                    TrackRealmHelper.updateRating(PlayerConstants.QUEUE_SONG);
                };
        ratingBar.setListener(ratingListener);

        FanLayoutManagerSettings coverArtFanLayoutManagerSettings =
                FanLayoutManagerSettings.newBuilder(this)
                        .withFanRadius(true)
                        .withAngleItemBounce(0)
                        .withViewHeightDp(180)
                        .withViewWidthDp(180)
                        .build();

        coverArtFanLayoutManager = new FanLayoutManager(this, coverArtFanLayoutManagerSettings);
        coverArtRecyclerView.setLayoutManager(coverArtFanLayoutManager);
        coverArtRecyclerView.setItemAnimator(new DefaultItemAnimator());
        coverArtRecyclerView.setHasFixedSize(true);
        coverArtAdapter =
                new CoverArtAdapter(this, PlayerConstants.QUEUE_LIST, this);
        coverArtRecyclerView.setAdapter(coverArtAdapter);
        coverArtRecyclerView.setChildDrawingOrderCallback(
                new FanChildDrawingOrderCallback(coverArtFanLayoutManager));

        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                buttonRepeat.setImageResource((R.drawable.repeat_icon));
                break;

            case PlayerConstants.REPEAT_ALL:
                buttonRepeat.setImageResource((R.drawable.repeat_icon_blue));
                break;

            case PlayerConstants.REPEAT_ONE:
                buttonRepeat.setImageResource((R.drawable.repeatone_icon_blue));
        }

        if (PrefsManager.Instance().getPlayShuffle(this)) {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon_blue));
        } else {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon));
        }

        updateUI();

        switcher.setKeepScreenOn(true);
        lrcView.setKeepScreenOn(true);

        seekBarMini.setProgress(seekBarMini.getProgress() + 1);
        seekBarMini.getThumb().mutate().setAlpha(0);

        scrollingLyrics = new ScrollingLyrics();

        EventBus.getDefault().register(this);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            PrefsManager.Instance().setExternalFile(getIntent().getData().getPath());
            check();
        }
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();
        if (!PrefsManager.Instance().getDontShowNowPlayingTutorial()) {
            showTutorial();
        }
        register();
    }

    @DebugLog
    @Override
    public void onDestroy() {
        scrollingLyrics.stop();
        if (mLrcThread != null) {
            if (mLrcThread.isAlive()) {
                mLrcThread.interrupt();
            }
        }
        EventBus.getDefault().unregister(this);
        sequence = null;
        super.onDestroy();
    }

    @DebugLog
    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            if (Settings.System.canWrite(this)) {
                SetRingtone createRingtone = new SetRingtone();
                createRingtone.open(NowPlayingActivity.this, selectedItem);
            } else {
                AppController.toast(
                        this,
                        "Write settings permission wasn't provided. Muziko can't set default ringtone");
            }
        }

        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = data.getData();

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

                File openedExternalFile = null;
                if (!PrefsManager.Instance().getExternalFile().isEmpty()) {
                    openedExternalFile = new File(PrefsManager.Instance().getExternalFile());
                    PrefsManager.Instance().setExternalFile(null);
                }

                if (openedExternalFile != null) {
                    DocumentFile targetDocument =
                            SAFHelpers.getDocumentFile(openedExternalFile, false);
                    if (targetDocument == null) {
                        return;
                    }

                    MediaHelper.Instance().loadMusicFromTrack(
                            openedExternalFile.getAbsolutePath(), true);
                    QueueItem existingTrack =
                            TrackRealmHelper.getTrack(openedExternalFile.getAbsolutePath());
                    if (existingTrack == null) {
                        AppController.toast(this, "Could not read data from track");
                        finish();

                    } else {
                        ArrayList<QueueItem> songList = new ArrayList<>();
                        songList.add(existingTrack);
                        AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, songList);
                        updateNowPlaying(0);
                    }
                }

            } else {
                new MaterialDialog.Builder(NowPlayingActivity.this)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .positiveColorRes(R.color.normal_blue)
                        .title("External storage permission not granted")
                        .content("Unable to open this track")
                        .positiveText("OK")
                        .onPositive(
                                (dialog, which) -> {
                                    SharedPreferences settings =
                                            PreferenceManager.getDefaultSharedPreferences(
                                                    NowPlayingActivity.this);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putBoolean("prefStoragePerms", false);
                                    editor.apply();
                                    finish();
                                })
                        .show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onPause() {
        unregister();
        super.onPause();
    }

    @DebugLog
    private void openExternalFile() {
        openedExternalFile = new File(PrefsManager.Instance().getExternalFile());
        QueueItem existingTrack = TrackRealmHelper.getTrack(openedExternalFile.getAbsolutePath());
        if (existingTrack != null) {
            ArrayList<QueueItem> songList = new ArrayList<>();
            songList.add(existingTrack);
            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, songList);
            updateNowPlaying(0);
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                boolean getperms = false;
                if (!PrefsManager.Instance().getStoragePermsURi().isEmpty()) {
                    DocumentFile targetDocument =
                            SAFHelpers.getDocumentFile(openedExternalFile, false);
                    if (targetDocument == null) {
                        getperms = true;
                    }
                }

                if (PrefsManager.Instance().getStoragePermsURi().isEmpty() || getperms) {
                    new MaterialDialog.Builder(this)
                            .theme(Theme.LIGHT)
                            .titleColorRes(R.color.normal_blue)
                            .negativeColorRes(R.color.dialog_negetive_button)
                            .positiveColorRes(R.color.normal_blue)
                            .title("Grant SD card permissions")
                            .content("Do you want to grant access to external storage?")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> triggerStorageAccessFramework())
                            .negativeText("Cancel")
                            .onNegative((dialog, which) -> finish())
                            .show();

                } else {
                    MediaHelper.Instance().loadMusicFromTrack(
                            openedExternalFile.getAbsolutePath(), true);
                    existingTrack = TrackRealmHelper.getTrack(openedExternalFile.getAbsolutePath());
                    if (existingTrack == null) {
                        AppController.toast(this, "Could not read data from track");
                        finish();

                    } else {
                        ArrayList<QueueItem> songList = new ArrayList<>();
                        songList.add(existingTrack);
                        AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, songList);
                        updateNowPlaying(0);
                    }
                }
            } else {
                MediaHelper.Instance().loadMusicFromTrack(openedExternalFile.getAbsolutePath(), true);
                existingTrack = TrackRealmHelper.getTrack(openedExternalFile.getAbsolutePath());
                if (existingTrack == null) {
                    AppController.toast(this, "Could not read data from track");
                    finish();

                } else {
                    ArrayList<QueueItem> songList = new ArrayList<>();
                    songList.add(existingTrack);
                    AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, songList);
                    updateNowPlaying(0);
                }
            }
        }
    }

    @DebugLog
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }

    @DebugLog
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main_now_player, menu);
        menu_lyrics = menu.findItem(R.id.lyrics);
        menuFav = menu.findItem(R.id.action_favourite);
        menuRingtone = menu.findItem(R.id.action_ringtone);
        menuAuClip = menu.findItem(R.id.action_send_auclip);
        menuStartFrom = menu.findItem(R.id.action_start_from);
        menuTagEdit = menu.findItem(R.id.action_edit);
        return super.onCreateOptionsMenu(menu);
    }

    @DebugLog
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (PlayerConstants.QUEUE_SONG.storage == 1 || PlayerConstants.QUEUE_SONG.storage == 2) {
            menuRingtone.setVisible(true);
            menuAuClip.setVisible(true);
            menuStartFrom.setVisible(true);
            menuTagEdit.setVisible(true);
        } else {
            menuRingtone.setVisible(false);
            menuAuClip.setVisible(false);
            menuStartFrom.setVisible(false);
            menuTagEdit.setVisible(false);
        }
        menuFav.setTitle(
                TrackRealmHelper.getFavoritesList().indexOf(PlayerConstants.QUEUE_SONG.data) == -1
                        ? getString(R.string.add_to_favs)
                        : getString(R.string.remove_from_favs));

        return super.onPrepareOptionsMenu(menu);
    }

    @DebugLog
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.volume:
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_SAME,
                        AudioManager.FLAG_SHOW_UI);
                return true;

            case lyrics:
                if (!lyricsShown) {
                    lyricsShown = true;
                    menu_lyrics.setIcon(
                            ContextCompat.getDrawable(this, R.drawable.lyrics_icon_blue));
                    showLyrics();
                } else {
                    lyricsShown = false;
                    menu_lyrics.setIcon(ContextCompat.getDrawable(this, R.drawable.lyrics_icon));
                    hideLyrics();
                }
                return true;

            case R.id.action_goto_artist:
                AppController.Instance().gotoArtist(this, PlayerConstants.QUEUE_SONG, null);
                return true;

            case R.id.action_goto_album:
                AppController.Instance().gotoAlbum(this, PlayerConstants.QUEUE_SONG, null);
                return true;

            case R.id.action_favourite:
                favorite(PlayerConstants.QUEUE_SONG);
                return true;

            case R.id.action_start_from:
                PlayFrom playFrom = new PlayFrom();
                playFrom.open(this, PlayerConstants.QUEUE_SONG);
                return true;

            case R.id.action_edit:
                QueueItem queueItem = TrackRealmHelper.getTrack(PlayerConstants.QUEUE_SONG.data);
                AppController.Instance().editSong(this, TAG, 0, queueItem);
                return true;

            case R.id.action_details:
                AppController.Instance().details(this, PlayerConstants.QUEUE_SONG);
                return true;

            case R.id.action_ringtone:
                selectedItem = PlayerConstants.QUEUE_SONG;

                boolean permission;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permission = Settings.System.canWrite(this);
                } else {
                    permission =
                            ContextCompat.checkSelfPermission(
                                    this, Manifest.permission.WRITE_SETTINGS)
                                    == PackageManager.PERMISSION_GRANTED;
                }
                if (!permission) {

                    if (android.os.Build.VERSION.SDK_INT
                            >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        new MaterialDialog.Builder(this)
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
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                Intent intent =
                                                        new Intent(
                                                                Settings
                                                                        .ACTION_MANAGE_WRITE_SETTINGS);
                                                intent.setData(
                                                        Uri.parse("package:" + getPackageName()));
                                                startActivityForResult(
                                                        intent, CODE_WRITE_SETTINGS_PERMISSION);
                                            } else {
                                                ActivityCompat.requestPermissions(
                                                        NowPlayingActivity.this,
                                                        new String[]{
                                                                Manifest.permission.WRITE_SETTINGS
                                                        },
                                                        CODE_WRITE_SETTINGS_PERMISSION);
                                            }
                                        })
                                .negativeText("Cancel")
                                .show();
                    } else {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(NowPlayingActivity.this, selectedItem);
                    }
                } else {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(NowPlayingActivity.this, selectedItem);
                }
                return true;

            case R.id.action_send_auclip:
                ShareRingtone shareRingtone = new ShareRingtone();
                shareRingtone.open(this, PlayerConstants.QUEUE_SONG);
                return true;

            case R.id.action_help:
                showTutorial();
                return true;

            case R.id.action_mediascan:
                //				MyApplication.scanMedia(this, coordinatorlayout);
                return true;

            default:
                return false;
        }
    }

    @DebugLog
    private void findViewsById() {
        toolbar = findViewById(R.id.nowPlayingToolbar);
        mainLayout = findViewById(R.id.mainLayout);
        coverArtImage = findViewById(R.id.coverArtImage);
        lyriclayout = findViewById(R.id.lyriclayout);
        lyricsScrollView = findViewById(R.id.lyricsScrollView);
        switcher = findViewById(R.id.switcher);
        switcher.setFactory(new LyricsTextFactory(this));
        lrcView = findViewById(R.id.lrc_view);
        errorLayout = findViewById(R.id.error_msg);
        bugtext = findViewById(R.id.bugtext);
        innerLayout = findViewById(R.id.innerlayout);
        texttrack = findViewById(R.id.texttrack);
        textartist = findViewById(R.id.textartist);
        texttrackLyrics = findViewById(R.id.texttrackLyrics);
        textartistLyrics = findViewById(R.id.textartistLyrics);
        buttonRepeat = findViewById(R.id.buttonRepeat);
        buttonShuffle = findViewById(R.id.buttonShuffle);
        bufferProgressBar = findViewById(R.id.bufferProgressBar);
        playLayout = findViewById(R.id.playLayout);
        buttonPlayMain = findViewById(R.id.buttonPlayMain);
        buttonPrevMain = findViewById(R.id.buttonPrevMain);
        buttonNextMain = findViewById(R.id.buttonNextMain);
        buttonCut = findViewById(R.id.buttonCut);
        buttonEqualizer = findViewById(R.id.buttonEqualizer);
        buttonSleep = findViewById(R.id.buttonSleep);
        buttonPlaylist = findViewById(R.id.buttonPlaylist);
        seekBarMini = findViewById(R.id.seekBarMini);
        ratingBar = findViewById(R.id.ratingBar);
        coverArtRecyclerView = findViewById(covertArtList);
        mainPlayerLayout = findViewById(R.id.mainPlayerLayout);
        progressBar = findViewById(R.id.progressBar);
        buttonDelete = findViewById(R.id.buttonDelete);
        startText = findViewById(R.id.startText);
        endText = findViewById(R.id.endText);
        closeFab = findViewById(R.id.closeFab);
    }

    @DebugLog
    private void showTutorial() {

        sequence =
                new TapTargetSequence(this)
                        .targets(
                                TapTarget.forView(
                                        findViewById(R.id.ratingBar),
                                        "Tap the rating bar",
                                        "To keep track of your favourite songs")
                                        .id(1)
                                        .drawShadow(true) // Whether to draw a drop shadow or not
                                        .cancelable(
                                                true) // Whether tapping outside the outer circle dismisses the view
                                        .tintTarget(true) // Whether to tint the target view's color
                                        .transparentTarget(
                                                true) // Specify whether the target is transparent (displays the content underneath)
                                        .targetRadius(30),
                                //						TapTarget.forToolbarMenuItem(toolbar, R.id.lyrics, "Tap the lyrics icon", "To sing along with the lyrics to your songs")
                                //								.id(2)
                                //								.drawShadow(true)                   // Whether to draw a drop shadow or not
                                //								.cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                                //								.tintTarget(true)                   // Whether to tint the target view's color
                                //								.transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                //								.targetRadius(30),
                                TapTarget.forView(
                                        findViewById(R.id.coverArtImage),
                                        "Tap the cover art",
                                        "To browse the list of tracks in the queue")
                                        .id(3)
                                        .drawShadow(true) // Whether to draw a drop shadow or not
                                        .cancelable(
                                                false) // Whether tapping outside the outer circle dismisses the view
                                        .tintTarget(true) // Whether to tint the target view's color
                                        .transparentTarget(
                                                false) // Specify whether the target is transparent (displays the content underneath)
                                        .icon(
                                                ContextCompat.getDrawable(
                                                        this,
                                                        R.drawable
                                                                .ic_library_music_black_24dp)) // Specify a custom drawable to draw as the target
                                        .targetRadius(30))
                        .listener(
                                new TapTargetSequence.Listener() {
                                    // This listener will tell us when interesting(tm) events happen in regards
                                    // to the sequence
                                    @Override
                                    public void onSequenceFinish() {
                                        // Yay
                                    }

                                    @Override
                                    public void onSequenceStep(
                                            TapTarget lastTarget, boolean targetClicked) {
                                        if (lastTarget.id() == 3) {
                                            showCovertArtScroller(true);
                                            PrefsManager.Instance().setDontShowNowPlayingTutorial(
                                                    true);
                                        }
                                    }

                                    @Override
                                    public void onSequenceCanceled(TapTarget lastTarget) {
                                        // Boo
                                    }
                                });

        sequence.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {
        if (!event.isClose() && !buffering) {
            buffering = true;
            bufferProgressBar.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.GONE);
            seekBarMini.setEnabled(false);
        } else {
            buffering = false;
            bufferProgressBar.setVisibility(View.GONE);
            playLayout.setVisibility(View.VISIBLE);
            seekBarMini.setEnabled(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressEvent event) {
        updateProgress(event.getProgress(), event.getDuration());
    }

    @DebugLog
    private void favorite(final QueueItem queue) {
        if (isFaving) return;
        isFaving = true;

        FavoriteEdit fe =
                new FavoriteEdit(this, PlayerConstants.QUEUE_TYPE_TRACKS, s -> isFaving = false);
        fe.execute(queue);
    }

    @DebugLog
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    openExternalFile();
                } else {
                    // Permission Denied
                    AppController.toast(this, "Some permissions were denied");
                    finish();
                }
            }
            break;
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(NowPlayingActivity.this, selectedItem);
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

    @DebugLog
    private void toggleRepeat(boolean change) {
        if (change) {
            int repeat = PrefsManager.Instance().getPlayRepeat();
            repeat++;
            if (repeat >= PlayerConstants.REPEAT_TOTAL) repeat = 0;

            PrefsManager.Instance().setPlayRepeat(repeat);

            StandardWidget standardWidget = new StandardWidget();
            standardWidget.onUpdate(this);

            QueueWidget queueWidget = new QueueWidget();
            queueWidget.onUpdate(this);

            AppController.Instance().serviceNotification(SongService.NOTIFICATION_REPEAT);
        }

        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                if (lyricsShown) {
                    buttonRepeat.setImageResource((R.drawable.repeat_icon_white));
                } else {
                    buttonRepeat.setImageResource((R.drawable.repeat_icon));
                }
                AppController.toast(this, "Repeat Off");
                break;

            case PlayerConstants.REPEAT_ALL:
                buttonRepeat.setImageResource((R.drawable.repeat_icon_blue));
                AppController.toast(this, "Repeat All");
                break;

            case PlayerConstants.REPEAT_ONE:
                buttonRepeat.setImageResource((R.drawable.repeatone_icon_blue));
                AppController.toast(this, "Repeat One");
                break;
        }
    }

    @DebugLog
    private void toggleShuffle(boolean change) {
        if (change) {
            PrefsManager.Instance().setPlayShuffle(!PrefsManager.Instance().getPlayShuffle(this));

            StandardWidget standardWidget = new StandardWidget();
            standardWidget.onUpdate(this);

            QueueWidget queueWidget = new QueueWidget();
            queueWidget.onUpdate(this);

            AppController.Instance().serviceNotification(SongService.NOTIFICATION_SHUFFLE);
        }

        if (PrefsManager.Instance().getPlayShuffle(this)) {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon_blue));
            AppController.toast(this, getString(R.string.shuffle_on));
        } else {
            if (lyricsShown) {
                buttonShuffle.setImageResource((R.drawable.shuffle_icon_white));
            } else {
                buttonShuffle.setImageResource((R.drawable.shuffle_icon));
            }
            AppController.toast(this, getString(R.string.shuffle_off));
        }
    }

    @DebugLog
    private void updatePlayButton() {
        Log.i(TAG, String.valueOf(PlayerConstants.QUEUE_STATE));
        if (MuzikoExoPlayer.Instance().isPlaying()) {
            PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PLAYING;
        }

        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
            mediaControlDrawable.setMediaControlState(MediaControlDrawable.State.PAUSE);
        } else {
            mediaControlDrawable.setMediaControlState(MediaControlDrawable.State.PLAY);
        }
    }

    private void updateProgress(int position, int duration) {

        if (PlayerConstants.QUEUE_SONG.storage != 1 && PlayerConstants.QUEUE_SONG.storage != 2) {
            if (position == 0) {
                if (!buffering) {
                    buffering = true;
                    bufferProgressBar.setVisibility(View.VISIBLE);
                    playLayout.setVisibility(View.GONE);
                }
            } else {
                if (buffering) {
                    buffering = false;
                    bufferProgressBar.setVisibility(View.GONE);
                    playLayout.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (buffering) {
                buffering = false;
                bufferProgressBar.setVisibility(View.GONE);
                playLayout.setVisibility(View.VISIBLE);
            }
        }

        if (!isSeekTouchCompact) {
            seekBarMini.setMax(duration);
            seekBarMini.setProgress(position);
        }

        startText.setText(Utils.getDuration(position));
        endText.setText(Utils.getDuration(duration));
    }

    @DebugLog
    private void updateNowPlaying(int position) {

        if (PlayerConstants.QUEUE_LIST.size() == 0 && position >= 0
                || currentQueuePosition == position) {
            return;
        }
        currentQueuePosition = position;

        QueueItem queueItem = PlayerConstants.QUEUE_LIST.get(position);

        texttrack.setText(queueItem.title);
        texttrackLyrics.setText(queueItem.title);
        textartist.setText(queueItem.artist_name);
        textartistLyrics.setText(queueItem.artist_name);
        ratingBar.setRating(queueItem.rating);
        endText.setText(Utils.getDuration(Long.parseLong(PlayerConstants.QUEUE_SONG.duration)));

        if (queueItem.storage != 1 && queueItem.storage != 2 && PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_PLAYING) {
            seekBarMini.setEnabled(false);
        } else {
            seekBarMini.setEnabled(true);
        }
        updateCoverArt(position);

        fetchLyrics(queueItem);
    }

    @DebugLog
    private void updateCoverArt(int position) {
        if (PlayerConstants.QUEUE_LIST.size() > position) {
            QueueItem queue = PlayerConstants.QUEUE_LIST.get(position);

            switch ((int) queue.album) {
                case CloudManager.GOOGLEDRIVE:
                    Picasso.with(this)
                            .load(R.drawable.drive_large)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
                    break;

                case CloudManager.DROPBOX:
                    Picasso.with(this)
                            .load(R.drawable.dropbox_large)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
                    break;

                case CloudManager.BOX:
                    Picasso.with(this)
                            .load(R.drawable.box_large)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
                    break;

                case CloudManager.ONEDRIVE:
                    Picasso.with(this)
                            .load(R.drawable.onedrive_large)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
                    break;

                case CloudManager.AMAZON:
                    Picasso.with(this)
                            .load(R.drawable.amazon_large)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
                    break;

                case CloudManager.FIREBASE:
                    Picasso.with(this)
                            .load(R.drawable.firebase_large)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
                    break;

                default:
                    Picasso.with(this)
                            .load("content://media/external/audio/albumart/" + queue.album)
                            .placeholder(R.mipmap.placeholder)
                            .error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .centerCrop()
                            .into(target);
            }
        }
    }

    @DebugLog
    private void updateSlider(int position) {

        updateNowPlaying(position);
    }

    @DebugLog
    private void updateUI() {

        if (adapterIsUpdating) {
            adapterIsUpdating = false;
        }

        updatePlayButton();
        updateProgress(PlayerConstants.QUEUE_TIME, Integer.parseInt(PlayerConstants.QUEUE_SONG.duration));
        fetchLyrics(PlayerConstants.QUEUE_SONG);
        updateSlider(PlayerConstants.QUEUE_INDEX);
        invalidateOptionsMenu();
    }

    @DebugLog
    private void updateLyricsUI(Lyrics lyrics) {

        this.mLyrics = lyrics;

        if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT) {

            String stringLyrics = lyrics.getText();
            Spanned htmlLyrics = Utils.fromHtml(stringLyrics);

            if (!lyrics.isLRC()) {
                switcher.setVisibility(View.VISIBLE);
                lrcView.setVisibility(View.GONE);
                if (true) switcher.setText(htmlLyrics);
                else switcher.setCurrentText(htmlLyrics);
            } else {
                switcher.setVisibility(View.GONE);
                lrcView.setVisibility(View.VISIBLE);
                lrcView.setOriginalLyrics(lyrics);
                lrcView.setSourceLrc(lyrics.getText());
                updateScrollingLyricsView();
            }

            errorLayout.setVisibility(View.INVISIBLE);
            lyricsScrollView.post(
                    () -> {
                        lyricsScrollView.scrollTo(
                                0, 0); //only useful when coming from localLyricsFragment
                        lyricsScrollView.smoothScrollTo(0, 0);
                    });
        } else {
            switcher.setText("");
            switcher.setVisibility(View.INVISIBLE);
            lrcView.setVisibility(View.INVISIBLE);
            errorLayout.setVisibility(View.VISIBLE);
            int message;

            if (lyrics.getFlag() == Lyrics.ERROR || networkState != NetworkInfo.State.CONNECTED) {
                message = R.string.connection_error;
            } else {
                message = R.string.no_lyrics;
            }
            bugtext.setText(message);
        }

        stopLyricsProgress();
    }

    @DebugLog
    private void showLyrics() {
        //		vusikView.setVisibility(View.GONE);
        mainPlayerLayout.setBackgroundResource(R.color.lyrics_background);
        mainLayout.setBackgroundResource(R.color.lyrics_background);
        toolbar.setBackgroundResource(R.color.lyrics_background);

        lyriclayout.setVisibility(View.VISIBLE);
        ratingBar.setVisibility(View.GONE);
        texttrack.setVisibility(View.GONE);
        textartist.setVisibility(View.GONE);

        buttonCut.setImageResource(R.drawable.cut_white);
        buttonEqualizer.setImageResource(R.drawable.equalizer_white);
        buttonSleep.setImageResource(R.drawable.set_timer_button_white);
        buttonPlaylist.setImageResource(R.drawable.add_playlist_white);
        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                buttonRepeat.setImageResource((R.drawable.repeat_icon_white));
                break;

            case PlayerConstants.REPEAT_ALL:
                buttonRepeat.setImageResource((R.drawable.repeat_icon_blue));
                break;

            case PlayerConstants.REPEAT_ONE:
                buttonRepeat.setImageResource((R.drawable.repeatone_icon_blue));
        }

        if (PrefsManager.Instance().getPlayShuffle(this)) {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon_blue));
        } else {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon_white));
        }
    }

    @DebugLog
    private void hideLyrics() {
        //		vusikView.setVisibility(View.VISIBLE);
        mainPlayerLayout.setBackgroundResource(R.color.transparent);
        mainLayout.setBackgroundResource(R.color.white);
        toolbar.setBackgroundResource(R.color.lyrics_toolbar_background);

        lyriclayout.setVisibility(View.GONE);
        ratingBar.setVisibility(View.VISIBLE);
        texttrack.setVisibility(View.VISIBLE);
        textartist.setVisibility(View.VISIBLE);

        buttonCut.setImageResource(R.drawable.cut);
        buttonEqualizer.setImageResource(R.drawable.equalizer);
        buttonSleep.setImageResource(R.drawable.set_timer_button);
        buttonPlaylist.setImageResource(R.drawable.add_playlist);
        switch (PrefsManager.Instance().getPlayRepeat()) {
            case PlayerConstants.REPEAT_OFF:
                buttonRepeat.setImageResource((R.drawable.repeat_icon));
                break;

            case PlayerConstants.REPEAT_ALL:
                buttonRepeat.setImageResource((R.drawable.repeat_icon_blue));
                break;

            case PlayerConstants.REPEAT_ONE:
                buttonRepeat.setImageResource((R.drawable.repeatone_icon_blue));
        }

        if (PrefsManager.Instance().getPlayShuffle(this)) {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon_blue));
        } else {
            buttonShuffle.setImageResource((R.drawable.shuffle_icon));
        }
    }

    @DebugLog
    private void startLyricsProgress() {
        runOnUiThread(
                () -> {
                    progressBar.setVisibility(View.VISIBLE);
                    innerLayout.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.GONE);
                });
    }

    @DebugLog
    private void stopLyricsProgress() {
        runOnUiThread(
                () -> {
                    progressBar.setVisibility(View.GONE);
                    innerLayout.setVisibility(View.VISIBLE);
                });
    }

    @DebugLog
    private void fetchLyrics(QueueItem queueItem) {

        Lyrics lyrics;

        // try from database
        lyrics = TrackRealmHelper.getLyricsforTrack(queueItem.data);

        if (lyrics == null) {
            if (networkState == NetworkInfo.State.CONNECTED) {

                startLyricsProgress();

                LyricsDownloader.setProvidersLRC();

                boolean positionAvailable = MuzikoExoPlayer.Instance().getCurrentPosition() != -1;

                new LyricsDownloader(
                        this, positionAvailable, queueItem.artist_name, queueItem.title)
                        .start();

            } else {
                lyrics = new Lyrics(Lyrics.ERROR);
                lyrics.setArtist(queueItem.artist_name);
                lyrics.setTitle(queueItem.title);
                updateLyricsUI(lyrics);
            }
        } else {
            updateLyricsUI(lyrics);
        }
    }

    @DebugLog
    private void fetchCurrentLyrics() {

        Lyrics lyrics = TrackRealmHelper.getLyricsforTrack(PlayerConstants.QUEUE_SONG.data);
        if (lyrics != null) {
            stopLyricsProgress();

            if (lyrics.isLRC()) {
                updateScrollingLyricsView();
            }
        } else {
            fetchLyrics(PlayerConstants.QUEUE_SONG);
        }
    }

    private void updateScrollingLyricsView() {
        if (mLrcThread == null || !mLrcThread.isAlive()) {
            mLrcThread = new Thread(scrollingLyrics);
            mLrcThread.start();
        }
    }

    @DebugLog
    @Override
    public void onClick(View v) {
        if (v == buttonNextMain) {
            adapterIsUpdating = true;
            AppController.Instance().serviceNext();

        } else if (v == buttonPrevMain) {
            adapterIsUpdating = true;
            AppController.Instance().servicePrev();

        } else if (v == buttonPlayMain) {
            AppController.Instance().serviceToggle();
        } else if (v == buttonCut) {

            AppController.Instance().cutSong(TrackRealmHelper.getTrack(PlayerConstants.QUEUE_SONG.data));

        } else if (v == buttonEqualizer) {
            Intent activityIntent = new Intent(NowPlayingActivity.this, EqualizerActivity.class);
            startActivity(activityIntent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        } else if (v == buttonSleep) {
            Intent activityIntent = new Intent(NowPlayingActivity.this, SleepActivity.class);
            startActivity(activityIntent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

        } else if (v == buttonPlaylist) {
            AppController.Instance().addToPlaylist(
                    this, TrackRealmHelper.getTrack(PlayerConstants.QUEUE_SONG.data));

        } else if (v == buttonRepeat) {
            toggleRepeat(true);

        } else if (v == buttonShuffle) {
            toggleShuffle(true);

        } else if (v == buttonDelete) {
            new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .titleColorRes(R.color.normal_blue)
                    .negativeColorRes(R.color.dialog_negetive_button)
                    .positiveColorRes(R.color.normal_blue)
                    .title("Clear Queue")
                    .content("Are you sure you want to clear the queue?")
                    .positiveText("Clear")
                    .onPositive((dialog, which) -> clear())
                    .negativeText("Cancel")
                    .show();

        } else if (v == coverArtImage) {
            showCovertArtScroller(true);
        } else if (v == closeFab) {
            showCovertArtScroller(false);
        }
    }

    @DebugLog
    private void showCovertArtScroller(boolean show) {

        if (!show) {
            showingCoverArtScroller = false;

            updateCoverArt(currentQueuePosition);

            new ExpectAnim()
                    .expect(coverArtRecyclerView)
                    .toBe(outOfScreen(Gravity.BOTTOM), toHaveBackgroundAlpha(0f))
                    .expect(closeFab)
                    .toBe(
                            outOfScreen(Gravity.TOP),
                            centerHorizontalInParent(),
                            toHaveBackgroundAlpha(0f))
                    .toAnimation()
                    .setDuration(500)
                    .start();

        } else {
            showingCoverArtScroller = true;

            updateCoverArt(currentQueuePosition);

            new ExpectAnim()
                    .expect(closeFab)
                    .toBe(
                            topOfParent().withMarginDp(56),
                            centerHorizontalInParent(),
                            toHaveBackgroundAlpha(1f))
                    .expect(coverArtRecyclerView)
                    .toBe(centerInParent(true, true), toHaveBackgroundAlpha(1f))
                    .toAnimation()
                    .setDuration(100)
                    .start();

            coverArtFanLayoutManager.scrollToPosition(PlayerConstants.QUEUE_INDEX);
        }
    }

    @DebugLog
    @Override
    public void onLyricsDownloaded(Lyrics lyrics) {
        updateLyricsUI(lyrics);

        if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT) {
            TrackRealmHelper.saveLyrics(lyrics);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

        if (isSeekTouchCompact) {

            startText.setText(Utils.getDuration(progress));
        }
    }

    @DebugLog
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekTouchCompact = true;
    }

    @DebugLog
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekTouchCompact = false;
        AppController.Instance().serviceSeek(seekBar.getProgress());
    }

    @DebugLog
    @Override
    public boolean onLongClick(View v) {
        if (v == buttonPrevMain) {

            QueueItem queueItem = TrackRealmHelper.getSecondMostRecentlyPlayed();

            if (queueItem == null) {
                AppController.toast(this, "No last played song to play");
            } else {
                int index = -1;
                for (int i = 0; i < PlayerConstants.QUEUE_LIST.size(); i++) {

                    if (PlayerConstants.QUEUE_LIST.get(i).data.equals(queueItem.data)) {
                        index = i;
                        break;
                    }
                }

                if (index < 0) {
                    PlayerConstants.QUEUE_LIST.add(PlayerConstants.QUEUE_INDEX + 1, queueItem);
                    PlayerConstants.QUEUE_INDEX = PlayerConstants.QUEUE_INDEX + 1;

                    AppController.Instance().servicePlay(false);
                    AppController.Instance().serviceDirty();

                } else {
                    AppController.Instance().play(
                            PlayerConstants.QUEUE_TYPE_QUEUE,
                            index,
                            PlayerConstants.QUEUE_LIST);
                }
            }
        }
        return true;
    }

    @DebugLog
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

    @DebugLog
    private void unregister() {

        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
    }

    @DebugLog
    @Override
    public void onItemClicked(int itemPosition, View view) {
        if (coverArtFanLayoutManager.getSelectedItemPosition() != itemPosition) {
            showCovertArtScroller(false);
            updateNowPlaying(itemPosition);
            handler.postDelayed(
                    () ->
                            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_QUEUE, itemPosition, coverArtAdapter.getList()), getResources().getInteger(R.integer.ripple_duration_delay));

        }
    }

    @DebugLog
    private void check() {
        // Here, thisActivity is the current activity
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read Storage");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                Utils.alertNoDismiss(
                        this,
                        getString(R.string.app_name),
                        message,
                        () -> {
                            // No explanation needed, we can request the permission.
                            ActivityCompat.requestPermissions(
                                    NowPlayingActivity.this,
                                    permissionsList.toArray(new String[permissionsList.size()]),
                                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                        });

                return;
            }
            ActivityCompat.requestPermissions(
                    NowPlayingActivity.this,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            openExternalFile();
        }
    }

    @DebugLog
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    private class ScrollingLyrics implements Runnable {
        private boolean threadCancelled;

        public void stop() {
            threadCancelled = true;
        }

        @Override
        public void run() {
            while (!threadCancelled) {
                try {
                    boolean ran = false;

                    long position = MuzikoExoPlayer.Instance().getCurrentPosition();

                    if (position == -1) {
                        final Lyrics staticLyrics = lrcView.getStaticLyrics();
                        runOnUiThread(() -> updateLyricsUI(staticLyrics));
                        return;
                    } else {
                        final long finalPosition = position;
                        runOnUiThread(() -> lrcView.changeCurrent(finalPosition));
                    }

                    while (PlayerConstants.QUEUE_SONG.title.equalsIgnoreCase(
                            mLyrics.getOriginalTrack())
                            && PlayerConstants.QUEUE_SONG.artist_name.equalsIgnoreCase(
                            mLyrics.getOriginalArtist())
                            && MuzikoExoPlayer.Instance().isPlaying()) {
                        if (threadCancelled) return;
                        ran = true;
                        position = MuzikoExoPlayer.Instance().getCurrentPosition();
                        long startTime = System.currentTimeMillis();
                        long distance = System.currentTimeMillis() - startTime;
                        if (MuzikoExoPlayer.Instance().isPlaying()) position += distance;
                        final long finalPosition = position;
                        runOnUiThread(() -> lrcView.changeCurrent(finalPosition));

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Crashlytics.logException(e);
                        }
                    }
                    if (MuzikoExoPlayer.Instance().isPlaying() && ran && mLyrics.isLRC())
                        fetchCurrentLyrics();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Crashlytics.logException(e);
                }
            }
        }
    }

    @DebugLog
    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            blurredDrawable = null;
            try {
                blurredDrawable =
                        ImageManager.Instance().createBlurredImageFromBitmap(
                                loadedImage[0], 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return blurredDrawable;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (coverArtImage.getDrawable() != null) {
                    blurredTransitionDrawable =
                            new TransitionDrawable(
                                    new Drawable[]{coverArtImage.getDrawable(), result});
                    coverArtImage.setImageDrawable(blurredTransitionDrawable);
                    blurredTransitionDrawable.startTransition(30);

                } else {
                    coverArtImage.setImageDrawable(result);
                }
            }
        }
    }

    @DebugLog
    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                switch (action) {
                    case AppController.INTENT_TRACK_SEEKED:
                        updateUI();
                        break;
                    case AppController.INTENT_TRACK_REPEAT:
                        toggleRepeat(false);
                        break;
                    case AppController.INTENT_TRACK_SHUFFLE:
                        toggleShuffle(false);
                        break;
                    case AppController.INTENT_QUEUE_CHANGED:
                        updateUI();
                        break;
                    case AppController.INTENT_QUEUE_STOPPED:
                        break;
                    case AppController.INTENT_QUEUE_CLEARED:
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
                            updateUI();
                        }
                        break;
                }
            }
        }
    }
}
