package com.muziko.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdView;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.GeneralAdapter;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.DownloadFile;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.FileHelper;
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
import com.muziko.objects.MenuObject;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.LibraryEdit;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;

import hugo.weaving.DebugLog;

import static android.R.attr.type;
import static com.muziko.MyApplication.networkState;
import static com.muziko.database.PlaylistRealmHelper.removeOneFromPlaylist;
import static com.muziko.manager.AppController.ACTION_FIREBASE_OVERLIMIT;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.objects.MenuObject.ADD_TO_LIBRARY;
import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.CUT;
import static com.muziko.objects.MenuObject.DELETE_FROM_PLAYLIST;
import static com.muziko.objects.MenuObject.DETAILS;
import static com.muziko.objects.MenuObject.DONT_SYNC_FAV_OR_PLAYLIST;
import static com.muziko.objects.MenuObject.DOWNLOAD;
import static com.muziko.objects.MenuObject.EDIT_TAGS;
import static com.muziko.objects.MenuObject.FAV;
import static com.muziko.objects.MenuObject.GO_TO_ALBUM;
import static com.muziko.objects.MenuObject.GO_TO_ARTIST;
import static com.muziko.objects.MenuObject.MOVE_TO_IGNORE;
import static com.muziko.objects.MenuObject.PLAY_NEXT;
import static com.muziko.objects.MenuObject.PLAY_X_TIMES;
import static com.muziko.objects.MenuObject.PREVIEW_SONG;
import static com.muziko.objects.MenuObject.SET_RINGTONE;
import static com.muziko.objects.MenuObject.SHARE_ITEM;

public class PlaylistSongsActivity extends BaseActivity
        implements RecyclerItemListener,
        ActionMode.Callback,
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        MaterialMenuAdapter.Callback {
    private final WeakHandler handler = new WeakHandler();
    private String TAG = PlaylistSongsActivity.class.getName();
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Toolbar toolbar;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private boolean alreadyResumed = false;
    private FastScrollRecyclerView recyclerView;
    private CoordinatorLayout coordinatorlayout;
    private String mTitle = "";
    private long mId = 0;
    //	private PlaylistQueueLoader task;
    private PlaylistQueueSaver taskSaver;
    private ArrayList<QueueItem> fullSongList;
    private ArrayList<QueueItem> songList;
    private boolean listChanged = false;
    private MenuItem menuItemSearch;
    private MenuItem menuItemView;
    private MenuItem menuItemAdd;
    private MenuItem menuItemClear;
    private MenuItem menuItemStorage;
    private MenuItem menuItemStorageAll;
    private MenuItem menuItemStorageInternal;
    private MenuItem menuItemStorageSD;
    private MenuItem menuItemgridone;
    private MenuItem menuItemgridtwo;
    private MenuItem menuItemgridthree;
    private MenuItem menuItemgridfour;
    private AdView mAdView;
    private GeneralAdapter adapter;
    private PlaylistSongsItemTouchHelper touchCallback = new PlaylistSongsItemTouchHelper();
    private ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
    private NpaLinearLayoutManager layoutList;
    private NpaGridLayoutManager layoutGrid2;
    private NpaGridLayoutManager layoutGrid3;
    private NpaGridLayoutManager layoutGrid4;
    private ActionMode actionMode = null;
    private PlaylistSongsReceiver receiver;
    private boolean isSaving = false;
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private ImageButton advancedSearch;
    private int advancedSearchInset;
    private BoxApi boxApi;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private boolean isAddingToLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        Intent intent = this.getIntent();

        mId = intent.getLongExtra(MyApplication.ARG_ID, 0);
        mTitle = intent.getStringExtra(MyApplication.ARG_TITLE);
        if (mId == 0 || mTitle == null) {

            mId = PrefsManager.Instance().getLastPlaylist();
            mTitle = PrefsManager.Instance().getLastPlaylistTitle();

        } else {
            PrefsManager.Instance().setLastPlaylist(mId);
            PrefsManager.Instance().setLastPlaylistTitle(mTitle);
        }

        if (mId == 0 || mTitle == null) {
            AppController.toast(PlaylistSongsActivity.this, "Playlist not found!");
            finish();
        }

        setContentView(R.layout.activity_playlist_songs);
        toolbar = findViewById(R.id.toolbar);
        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
        toolbar.setTitle(mTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        layoutList = new NpaLinearLayoutManager(this);
        layoutGrid2 = new NpaGridLayoutManager(this, 2);
        layoutGrid3 = new NpaGridLayoutManager(this, 3);
        layoutGrid4 = new NpaGridLayoutManager(this, 4);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        songList = new ArrayList<>();
        adapter =
                new GeneralAdapter(
                        this, songList, PlayerConstants.QUEUE_TYPE_PLAYLIST_SONGS, prefShowArtwork, TAG, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new PicassoScrollListener(this, TAG));
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(adapter);

        touchHelper.attachToRecyclerView(recyclerView);

        setupMainPlayer();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mainUpdate();

        register();
        alreadyResumed = true;

        reload();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            if (Settings.System.canWrite(this)) {
                SetRingtone createRingtone = new SetRingtone();
                createRingtone.open(PlaylistSongsActivity.this, selectedItem);
            } else {
                AppController.toast(
                        this, "Write settings permission wasn't provided. Muziko can't set default ringtone");
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (slidingUpPanelLayout != null
                && (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            miniPlayer.close();
        } else if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();

            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
                return;
            }
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {

        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        if (listChanged) {
            save();
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
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) contentlayout.getLayoutParams();
        params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
        contentlayout.requestLayout();
    }

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void findViewsById() {

        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        recyclerView = findViewById(R.id.itemList);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();

        alreadyResumed = true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist_songs_menu, menu);

        menuItemView = menu.findItem(R.id.playlistsong_view);
        menuItemSearch = menu.findItem(R.id.playlistsong_search);
        menuItemAdd = menu.findItem(R.id.playlistsong_add);
        menuItemClear = menu.findItem(R.id.playlistsong_clear);

        menuItemStorage = menu.findItem(R.id.storage_filter);
        menuItemStorageAll = menu.findItem(R.id.player_storage_all);
        menuItemStorageInternal = menu.findItem(R.id.player_storage_internal);
        menuItemStorageSD = menu.findItem(R.id.player_storage_sd);

        menuItemgridone = menu.findItem(R.id.grid_one);
        menuItemgridtwo = menu.findItem(R.id.grid_two);
        menuItemgridthree = menu.findItem(R.id.grid_three);
        menuItemgridfour = menu.findItem(R.id.grid_four);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menuItemClear != null) {
            menuItemClear.setVisible(adapter.getItemCount() != 0);
        }

        menuItemView.setVisible(false);
        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();
            AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
            Resources resources = getResources();
            advancedSearch = advancedSearchButton.addButton(this, resources, searchView);
            advancedSearch.setOnClickListener(this);
            ActionBar.LayoutParams searchviewParams =
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint("Search song or artist");
            searchView.setOnQueryTextListener(this);
            searchView.setOnSearchClickListener(this);
            searchView.setOnCloseListener(this);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;

            case R.id.player_storage_all:
                PrefsManager.Instance().setStorageViewType(0);
                loadPlaylistSongs(mId);
                return true;

            case R.id.player_storage_internal:
                PrefsManager.Instance().setStorageViewType(1);
                loadPlaylistSongs(mId);
                return true;

            case R.id.player_storage_sd:
                PrefsManager.Instance().setStorageViewType(2);
                loadPlaylistSongs(mId);
                return true;

            case R.id.playlistsong_add:
                addSongs();
                return true;

            case R.id.playlistsong_clear:
                new MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .negativeColorRes(R.color.dialog_negetive_button)
                        .positiveColorRes(R.color.normal_blue)
                        .title("Clear Playlist")
                        .content("Are you sure you want to clear the playlist?")
                        .positiveText("Clear")
                        .onPositive(
                                (dialog, which) -> {
                                    PlaylistSongRealmHelper.deleteByPlaylist(mId);
                                    AppController.toast(PlaylistSongsActivity.this, "Playlist cleared!");

                                    Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
                                    //intent.putExtra("id", mId);
                                    sendBroadcast(intent);

                                    reload();
                                })
                        .negativeText("Cancel")
                        .show();

                return true;

            case R.id.playlistsong_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.playlistsong_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(PlaylistSongsActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.playlistsong_exit:
                AppController.Instance().exit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
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
        } else {
            advancedSearch.setVisibility(View.VISIBLE);
            toolbar.setContentInsetStartWithNavigation(0);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.playlist_songs_multiselect, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<QueueItem> list = adapter.getSelectedItems();
        if (list.size() > 0) {
            // Handle presses on the action bar items
            switch (item.getItemId()) {
                case R.id.play:
                    AppController.Instance().clearAddToQueue(this, list);
                    break;

                case R.id.share:
                    AppController.Instance().shareSongs(list);
                    break;

                case R.id.add_to_queue:
                    AppController.Instance().addToQueue(this, list, false);
                    break;

                case R.id.play_next:
                    AppController.Instance().addToQueue(this, list, true);
                    break;

                case R.id.add_to_playlist:
                    AppController.Instance().addToPlaylist(this, list, false);
                    break;

                case R.id.delete:
                    deleteItems(list);
                    break;

                case R.id.multi_tag_edit:
                    AppController.Instance().multiTagEdit(this, list);
                    break;

                case R.id.trash:
                    movetoNegative(list);
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

    @Override
    public void onDragTouched(RecyclerView.ViewHolder viewHolder) {
        if (touchHelper != null) touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onMenuClicked(final int position) {
        final QueueItem item = adapter.getItem(position);
        if (item == null) return;

        selectedItem = item;
        selectedItemPosition = position;

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(ADD_TO_QUEUE));
        items.add(new MenuObject(ADD_TO_PLAYLIST));
        items.add(
                new MenuObject(
                        FAV,
                        (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_favs)
                                : getString(R.string.remove_from_favs)));
        items.add(new MenuObject(PLAY_NEXT));
        items.add(new MenuObject(GO_TO_ARTIST));
        items.add(new MenuObject(GO_TO_ALBUM));
        items.add(new MenuObject(SET_RINGTONE));
        items.add(new MenuObject(CUT));
        items.add(new MenuObject(PREVIEW_SONG));
        items.add(new MenuObject(EDIT_TAGS));
        items.add(new MenuObject(DETAILS));
        items.add(new MenuObject(MenuObject.SHARE_ITEM));
        items.add(new MenuObject(PLAY_X_TIMES));
        items.add(new MenuObject(MOVE_TO_IGNORE));
        items.add(new MenuObject(DELETE_FROM_PLAYLIST));

        final ArrayList<MenuObject> cloudItems = new ArrayList<>();
        cloudItems.add(new MenuObject(PLAY_NEXT));
        cloudItems.add(new MenuObject(PLAY_X_TIMES));
        cloudItems.add(new MenuObject(ADD_TO_QUEUE));
        cloudItems.add(new MenuObject(ADD_TO_PLAYLIST));
        cloudItems.add(
                new MenuObject(
                        FAV,
                        (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_favs)
                                : getString(R.string.remove_from_favs)));
        cloudItems.add(new MenuObject(DOWNLOAD));
        cloudItems.add(new MenuObject(DETAILS));
        items.add(
                new MenuObject(
                        ADD_TO_LIBRARY,
                        (TrackRealmHelper.getLibraryList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_library)
                                : getString(R.string.remove_from_library)));
        cloudItems.add(new MenuObject(MOVE_TO_IGNORE));
        cloudItems.add(new MenuObject(DELETE_FROM_PLAYLIST));

        final ArrayList<MenuObject> firebaseItems = new ArrayList<>();
        if (item.storage == CloudManager.FIREBASE && FirebaseManager.Instance().isOverLimit()) {
            if (item.isSync()) {
                firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
            }
        } else {
            firebaseItems.add(new MenuObject(PLAY_NEXT));
            firebaseItems.add(new MenuObject(PLAY_X_TIMES));
            firebaseItems.add(new MenuObject(ADD_TO_QUEUE));
            firebaseItems.add(new MenuObject(ADD_TO_PLAYLIST));
            firebaseItems.add(new MenuObject(DOWNLOAD));
            firebaseItems.add(new MenuObject(DETAILS));
            firebaseItems.add(new MenuObject(MOVE_TO_IGNORE));
            if (item.isSync()) {
                firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
            }
        }

        MaterialMenuAdapter materialMenuAdapter = new MaterialMenuAdapter(items, this);
        switch (item.storage) {
            case 0:
                materialMenuAdapter = new MaterialMenuAdapter(items, this);
                break;
            case 1:
                materialMenuAdapter = new MaterialMenuAdapter(items, this);
                break;
            case 2:
                materialMenuAdapter = new MaterialMenuAdapter(items, this);
                break;
            case CloudManager.FIREBASE:
                materialMenuAdapter = new MaterialMenuAdapter(firebaseItems, this);
                break;
            default:
                materialMenuAdapter = new MaterialMenuAdapter(cloudItems, this);
                break;
        }

        new MaterialDialog.Builder(this)
                .adapter(materialMenuAdapter, new LinearLayoutManager(this))
                .show();
    }

    @Override
    public void onItemClicked(int position) {

        QueueItem queueItem = adapter.getItem(position);
        if (queueItem.storage == CloudManager.FIREBASE && FirebaseManager.Instance().isOverLimitNow(0))
            return;

        if (this.adapter.isMultiSelect()) {
            toggleSelection(position);
        } else {
            //long type = PlayerConstants.QUEUE_TYPE_PLAYLIST_SONGS + id;

            //if (PlayerConstants.QUEUE_TYPE != type) {
            AppController.Instance()
                    .play(PlayerConstants.QUEUE_TYPE_PLAYLIST_SONGS, position, adapter.getList());
      /*
      } else {
          PlayerConstants.QUEUE_INDEX = position;
          PlayerConstants.QUEUE_TIME = 0;

          Application.servicePlay(getActivity());
      }*/
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.adapter.isMultiSelect()) {
            PlaylistSongsActivity.this.startSupportActionMode(this);

            this.adapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(
                () -> {
                    switch (item.id) {
                        case ADD_TO_QUEUE: //add to q
                            PlayerConstants.QUEUE_TYPE = 0;
                            AppController.Instance().addToQueue(PlaylistSongsActivity.this, selectedItem, false);
                            break;

                        case ADD_TO_PLAYLIST: //add to p
                            AppController.Instance().addToPlaylist(PlaylistSongsActivity.this, selectedItem);
                            break;

                        case FAV: //add to f`
                            favorite(selectedItemPosition, selectedItem);
                            break;

                        case PLAY_NEXT: //play next
                            AppController.Instance().addToQueue(PlaylistSongsActivity.this, selectedItem, true);
                            break;

                        case GO_TO_ARTIST: //goto ar
                            AppController.Instance().gotoArtist(PlaylistSongsActivity.this, selectedItem, null);
                            break;

                        case GO_TO_ALBUM: //goto al
                            AppController.Instance().gotoAlbum(PlaylistSongsActivity.this, selectedItem, null);
                            break;

                        case SET_RINGTONE: //createRingtone
                            boolean permission;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                permission = Settings.System.canWrite(PlaylistSongsActivity.this);
                            } else {
                                permission =
                                        ContextCompat.checkSelfPermission(
                                                PlaylistSongsActivity.this, Manifest.permission.WRITE_SETTINGS)
                                                == PackageManager.PERMISSION_GRANTED;
                            }
                            if (!permission) {

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    new MaterialDialog.Builder(PlaylistSongsActivity.this)
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
                                                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                                            intent.setData(Uri.parse("package:" + getPackageName()));
                                                            startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                                                        } else {
                                                            ActivityCompat.requestPermissions(
                                                                    PlaylistSongsActivity.this,
                                                                    new String[]{Manifest.permission.WRITE_SETTINGS},
                                                                    CODE_WRITE_SETTINGS_PERMISSION);
                                                        }
                                                    })
                                            .negativeText("Cancel")
                                            .show();
                                } else {
                                    SetRingtone createRingtone = new SetRingtone();
                                    createRingtone.open(PlaylistSongsActivity.this, selectedItem);
                                }
                            } else {
                                SetRingtone createRingtone = new SetRingtone();
                                createRingtone.open(PlaylistSongsActivity.this, selectedItem);
                            }
                            break;

                        case CUT: //cut
                            AppController.Instance().cutSong(selectedItem);
                            break;

                        case PREVIEW_SONG: //preview
                            PreviewSong previewSong = new PreviewSong();
                            previewSong.open(PlaylistSongsActivity.this, selectedItem);
                            break;

                        case EDIT_TAGS: //edit
                            AppController.Instance()
                                    .editSong(this, "PlaylistSongsActivity", selectedItemPosition, selectedItem);
                            break;

                        case DETAILS: //details
                            AppController.Instance().details(PlaylistSongsActivity.this, selectedItem);
                            break;

                        case ADD_TO_LIBRARY:
                            toggleLibrary(selectedItemPosition, selectedItem);
                            break;

                        case SHARE_ITEM: //share
                            AppController.Instance().shareSong(this, selectedItem);
                            break;

                        case PLAY_X_TIMES: //remove
                            AppController.Instance().removeAfter(PlaylistSongsActivity.this, selectedItem);
                            break;

                        case MOVE_TO_IGNORE: //negative
                            movetoNegative(selectedItemPosition, selectedItem);
                            break;

                        case DELETE_FROM_PLAYLIST: //remove
                            delete(selectedItemPosition, selectedItem);
                            break;

                        case DOWNLOAD:
                            if (networkState != NetworkInfo.State.CONNECTED) {
                                AppController.toast(this, getString(R.string.no_network_connection));
                                return;
                            }
                            if (FileHelper.fileExists(selectedItem)) {
                                AppController.toast(this, getString(R.string.file_exists));
                                return;
                            }

                            DownloadFile downloadFile = new DownloadFile();
                            if (selectedItem.storage == CloudManager.FIREBASE) {
                                for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebasePlaylistTracksList()) {
                                    if (cloudTrack.getMd5().equals(selectedItem.md5)) {
                                        PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(mId);
                                        downloadFile.init(this, cloudTrack, playlistItem.hash, MuzikoConstants.FirebaseFileMode.PLAYLISTS);
                                        break;
                                    }
                                }
                            } else {
                                downloadFile.init(this, selectedItem);
                            }

                            break;
                    }

                    dialog.dismiss();
                },
                600);
    }

    @DebugLog
    private void toggleLibrary(final int pos, final QueueItem queue) {
        if (queue.isLibrary()) {
            LibraryEdit libraryEdit = new LibraryEdit(this, true, s -> adapter.notifyItemChanged(pos));
            libraryEdit.execute(queue);
        } else {
            if (!FirebaseManager.Instance().isOverLimitNow(1)) {
                LibraryEdit libraryEdit = new LibraryEdit(this, s -> adapter.notifyItemChanged(pos));
                libraryEdit.execute(queue);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(PlaylistSongsActivity.this, selectedItem);
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

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(
                this,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    TrackRealmHelper.movetoNegative(queue);
                    adapter.removeIndex(position);
                });
    }

    private void favorite(final ArrayList<QueueItem> queueItems) {

        for (int i = 0; i < queueItems.size(); i++) {
            QueueItem queueItem = queueItems.get(i);
            TrackRealmHelper.addFavorite(queueItem.data);
        }

        AppController.toast(this, "Songs added to Favorites");
        sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
        if (type != PlayerConstants.QUEUE_TYPE_TRACKS) {
            sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
            sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
        }
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
                    reload();
                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {

        reload();
    }

    @DebugLog
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFirebaseRefreshEvent(FirebaseRefreshEvent event) {

        reload();
    }

    private void delete(final int position, final QueueItem queue) {
        Utils.askDelete(
                PlaylistSongsActivity.this,
                "Delete From Playlist",
                "Are you sure you want to delete this song from this playlist ?",
                () -> {

                    if (PlaylistSongRealmHelper.deleteByData(queue.data)) {

                        removeOneFromPlaylist(queue);
                        adapter.removeIndex(position);
                        PlayerConstants.QUEUE_TYPE = 0;

                        Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
                        //intent.putExtra("id", queue.id);
                        sendBroadcast(intent);
                        AppController.toast(PlaylistSongsActivity.this, "Song deleted from playlist");
                        PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(mId);
                        FirebaseManager.Instance().deletePlaylistTrack(queue, playlistItem.hash);

                    } else {
                        AppController.toast(PlaylistSongsActivity.this, "Unable to delete from playlist");
                    }
                });
    }

    private void deleteItems(final ArrayList<QueueItem> list) {
        Utils.askDelete(
                PlaylistSongsActivity.this,
                "Delete From Playlist",
                String.format(
                        "Are you sure you want to delete song%s from this playlist ?",
                        list.size() != 1 ? "s" : ""),
                () -> {
                    ArrayList<QueueItem> del = new ArrayList<>();
                    for (QueueItem item : list) {
                        if (item == null) continue;

                        if (PlaylistSongRealmHelper.delete(item.id)) {

                            PlaylistRealmHelper.removeOneFromPlaylist(item);
                            del.add(item);
                        }
                    }

                    adapter.removeAll(del);

                    PlayerConstants.QUEUE_TYPE = 0;

                    Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
                    //intent.putExtra("id", mId);
                    sendBroadcast(intent);

                    AppController.toast(
                            PlaylistSongsActivity.this,
                            String.format("Song%s deleted from playlist", del.size() != 1 ? "s" : ""));

                    del.clear();
                });
    }

    private void favorite(final int position, QueueItem queue) {
        FavoriteEdit fe =
                new FavoriteEdit(
                        PlaylistSongsActivity.this,
                        PlayerConstants.QUEUE_TYPE_FAVORITES,
                        s -> adapter.notifyItemChanged(position));
        fe.execute(queue);
    }

    private void addSongs() {
        PlayerConstants.QUEUE_TYPE = 0;

        Intent in = new Intent(PlaylistSongsActivity.this, SearchSongsActivity.class);
        in.putExtra(MyApplication.ARG_ID, mId);
        in.putExtra(MyApplication.ARG_TITLE, mTitle);
        startActivity(in);
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0) actionMode.setTitle("");
            else actionMode.setTitle(String.format("%d song%s", count, count != 1 ? "s" : ""));
        }
    }

    private void reload() {

        loadPlaylistSongs(mId);
    }

    private void unsave() {
        if (taskSaver != null) {
            taskSaver.cancel(true);
            taskSaver = null;
        }
    }

    private void save() {
        if (isSaving) return;

        unsave();

        taskSaver = new PlaylistQueueSaver(PlaylistSongsActivity.this);
        taskSaver.execute();
    }

    private void register() {
        receiver = new PlaylistSongsReceiver();

        IntentFilter playlistsongsfilter = new IntentFilter();
        playlistsongsfilter.addAction(AppController.INTENT_PLAYLIST_CHANGED);
        playlistsongsfilter.addAction(AppController.INTENT_TRACK_EDITED);
        registerReceiver(receiver, playlistsongsfilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_EXIT);
        filter.addAction(AppController.INTENT_CLEAR);
        filter.addAction(AppController.INTENT_TRACK_EDITED);
        filter.addAction(AppController.INTENT_TRACK_SEEKED);
        filter.addAction(AppController.INTENT_QUEUE_STOPPED);
        filter.addAction(AppController.INTENT_TRACK_SHUFFLE);
        filter.addAction(AppController.INTENT_TRACK_REPEAT);
        filter.addAction(AppController.INTENT_QUEUE_CHANGED);
        filter.addAction(AppController.INTENT_QUEUE_CLEARED);
        filter.addAction(AppController.ACTION_FIREBASE_OVERLIMIT);

        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, filter);
    }

    private void unregister() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
    }

    private void loadPlaylistSongs(long id) {
        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        adapter.setShowArtwork(prefShowArtwork);

        songList.clear();
        adapter.add(PlaylistSongRealmHelper.loadAllByPlaylist(0, id));
        adapter.notifyDataSetChanged();
        adapter.setStorage(0);
    }

    public class PlaylistQueueSaver extends AsyncTask<Void, int[], Boolean> {
        private final Context ctx;

        public PlaylistQueueSaver(Context context) {
            this.ctx = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            PlaylistSongRealmHelper.deleteByPlaylist(mId);

            Collection<QueueItem> list = adapter.getList();
            int counter = 0;
            ArrayList<PlaylistQueueItem> playlistQueueItems = new ArrayList<>();

            for (QueueItem queue : list) {
                PlaylistQueueItem item = new PlaylistQueueItem();
                item.copyQueue(queue);
                item.playlist = mId;
                playlistQueueItems.add(item);
                //				if (item.insert(item) > 0) {
                //					counter++;
                //				}
            }

            counter = PlaylistSongRealmHelper.insertList(playlistQueueItems, false);
            listChanged = false;
            return counter > 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isSaving = true;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);

            Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
            //intent.putExtra("id", mId);
            ctx.sendBroadcast(intent);

            isSaving = false;
        }
    }

    private class PlaylistSongsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    if (action.equals(AppController.INTENT_PLAYLIST_CHANGED)) {
                        long id = intent.getLongExtra("id", 0);
                        if (id == mId) {
                            reload();
                        }
                    } else if (action.equals(AppController.INTENT_TRACK_EDITED)) {
                        int index = intent.getIntExtra("index", -1);
                        String tag = intent.getStringExtra("tag");
                        QueueItem item = (QueueItem) intent.getSerializableExtra("item");
                        if (item != null
                                && tag != null
                                && tag.equals("PlaylistSongsActivity")
                                && index >= 0
                                && index < adapter.getItemCount()) {
                            adapter.set(item);
                        } else {
                            reload();
                        }
                    }
                }
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
        }
    }

    private class PlaylistSongsItemTouchHelper extends ItemTouchHelper.Callback {
        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(
                    ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

        //and in your imlpementaion of
        public boolean onMove(
                RecyclerView recyclerView,
                RecyclerView.ViewHolder viewHolder,
                RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            if (adapter.moveTo(from, to)) {
                listChanged = true;
            }
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
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
                            mainUpdate();
                        }
                        break;

                    case ACTION_FIREBASE_OVERLIMIT:
                        if (adapter.isFirebaseOverlimit() != FirebaseManager.Instance().isOverLimit()) {
                            adapter.updateFirebaseOverlimit(FirebaseManager.Instance().isOverLimit());
                        }
                        break;
                }
            }
        }
    }
}
