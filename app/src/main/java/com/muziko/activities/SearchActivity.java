package com.muziko.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SearchAdapter;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.DownloadFile;
import com.muziko.dialogs.PlayFrom;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.dialogs.ShareRingtone;
import com.muziko.dialogs.UploadFile;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.SearchRecyclerListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.LibraryEdit;
import com.muziko.tasks.TrackDelete;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.manager.MuzikoConstants.UPLOAD_SETTLE_DELAY;
import static com.muziko.objects.MenuObject.ADD_TO_LIBRARY;
import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.CUT;
import static com.muziko.objects.MenuObject.DELETE_ITEM;
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
import static com.muziko.objects.MenuObject.SEND;
import static com.muziko.objects.MenuObject.SEND_AUDIO_CLIP;
import static com.muziko.objects.MenuObject.SET_RINGTONE;
import static com.muziko.objects.MenuObject.SET_START_TIME;
import static com.muziko.objects.MenuObject.SHARE_ITEM;
import static com.muziko.objects.MenuObject.UPLOAD;

/**
 * Created by dev on 12/09/2016.
 */
public class SearchActivity extends BaseActivity implements SearchRecyclerListener, SearchView.OnQueryTextListener, MaterialMenuAdapter.Callback {
    private final String TAG = SearchActivity.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final ArrayList<QueueItem> searchList = new ArrayList<>();
    private final ArrayList<QueueItem> trackList = new ArrayList<>();
    private final ArrayList<QueueItem> artistList = new ArrayList<>();
    private final ArrayList<QueueItem> albumList = new ArrayList<>();
    private String searchString = "";
    private boolean firstLoad = false;
    private MainReceiver mainReceiver;
    private MenuItem menuItemSearch;
    private SearchAdapter searchAdapter = null;
    private RecyclerView searchRecyclerView;
    private boolean isFaving = false;
    private CoordinatorLayout coordinatorlayout;
    private QueueItem selectedItem;
    private int selectedPosition;
    private ArrayList<QueueItem> selectedItems = new ArrayList<>();
    private MaterialMenuAdapter.Callback onSubMenuObjectItemSelected =
            (dialog, index, item) -> handler.postDelayed(
                    () -> {
                        switch (index) {
                            case 0: //send

                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                                        if (networkState == NetworkInfo.State.CONNECTED) {
                                            Intent registerIntent = new Intent(this, RegisterActivity.class);
                                            startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                        } else {
                                            AppController.toast(this, getString(R.string.no_internet_for_register));
                                        }

                                    } else {
                                        AppController.Instance().sendTracks(this, selectedItems);
                                    }
                                } else {
                                    if (networkState == NetworkInfo.State.CONNECTED) {
                                        Intent registerIntent = new Intent(this, RegisterActivity.class);
                                        startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                    } else {
                                        AppController.toast(this, getString(R.string.no_internet_for_register));
                                    }
                                }

                                break;

                            case 1: //send wifi

                                AppController.Instance().sendTracksWifi(this, selectedItems);

                                break;
                        }

                        dialog.dismiss();
                    },
                    getResources().getInteger(R.integer.ripple_duration_delay));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStartActivity(false);

        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setTitle("Muziko");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        searchString = MyApplication.searchString;
        String mainTitle = "";
        if (!searchString.isEmpty()) {
            mainTitle = searchString;
        } else {
            mainTitle = "Search";
        }

        toolbar.setTitle(mainTitle);

        isFaving = false;
        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        searchAdapter = new SearchAdapter(this, searchList, PlayerConstants.QUEUE_TYPE_TRACKS, prefShowArtwork, MyApplication.TRACKS, this);
        searchAdapter.setFilterCount(0);
        searchRecyclerView.setItemAnimator(new DefaultItemAnimator());
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.addOnScrollListener(new PicassoScrollListener(this, TAG));
        searchRecyclerView.setLayoutManager(new NpaLinearLayoutManager(this));
        searchRecyclerView.setAdapter(searchAdapter);

    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        unregister();
        super.onDestroy();
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            if (Settings.System.canWrite(this)) {
                SetRingtone createRingtone = new SetRingtone();
                createRingtone.open(SearchActivity.this, selectedItem);
            } else {
                AppController.toast(this, "Write settings permission wasn't provided. Muziko can't set default ringtone");
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {

        super.onPause();

        unregister();
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
        load();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void findViewsById() {
        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);

    }

    private void load() {


        trackList.addAll(TrackRealmHelper.getTracks(0).values());
        artistList.addAll(TrackRealmHelper.getArtists().values());
        albumList.addAll(TrackRealmHelper.getAlbums().values());

        searchList.clear();

        if (trackList.size() > 0) {
            QueueItem trackheader = new QueueItem();
            trackheader.type = MyApplication.TRACKHEADER;
            searchList.add(trackheader);
        }

        for (QueueItem track : trackList) {
            track.type = MyApplication.TRACKS;
            searchList.add(track);
        }


        if (artistList.size() > 0) {
            QueueItem artistheader = new QueueItem();
            artistheader.type = MyApplication.ARTISTHEADER;
            searchList.add(artistheader);
        }

        for (QueueItem artist : artistList) {
            artist.type = MyApplication.ARTISTS;
            searchList.add(artist);
        }

        if (albumList.size() > 0) {
            QueueItem albumheader = new QueueItem();
            albumheader.type = MyApplication.ALBUMHEADER;
            searchList.add(albumheader);
        }

        for (QueueItem album : albumList) {
            album.type = MyApplication.ALBUMS;
            searchList.add(album);
        }


        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search, menu);

        menuItemSearch = menu.findItem(R.id.action_search);
        menuItemSearch.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();
            ActionBar.LayoutParams searchviewParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Search your library...");
            MenuItemCompat.expandActionView(menuItemSearch);
            searchView.setOnQueryTextListener(this);

            if (!searchString.isEmpty()) {
                firstLoad = true;
                searchView.setQuery(searchString, true);
                searchView.clearFocus();
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
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
                        new Intent(SearchActivity.this, ShareWifiActivity.class);
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
    public boolean onQueryTextSubmit(String query) {
        firstLoad = false;
        handler.postDelayed(() -> searchAdapter(searchString), 1000);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchString = newText;

        if (!firstLoad) {
            handler.postDelayed(() -> searchAdapter(searchString), 500);
        }
        return false;
    }

    private void movetoIgnore(final int position, final QueueItem queue) {
        Utils.askDelete(this, "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

            TrackRealmHelper.movetoNegative(queue);
            searchAdapter.removeIndex(position);
            EventBus.getDefault().post(new RefreshEvent(1000));
        });
    }

    @Override
    public void onItemMenuClicked(final QueueItem queueItem, final int position) {

        final QueueItem item = searchAdapter.getItem(position);
        if (item == null) return;

        if (queueItem.type.equals(MyApplication.TRACKS)) {

            selectedItem = item;
            selectedPosition = position;
            selectedItems.clear();
            selectedItems.add(item);

            final ArrayList<MenuObject> items = new ArrayList<>();
            items.add(new MenuObject(PLAY_NEXT));
            items.add(new MenuObject(PLAY_X_TIMES));
            items.add(new MenuObject(ADD_TO_QUEUE));
            items.add(new MenuObject(ADD_TO_PLAYLIST));
            items.add(
                    new MenuObject(
                            FAV,
                            (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                    ? getString(R.string.add_to_favs)
                                    : getString(R.string.remove_from_favs)));
            items.add(new MenuObject(GO_TO_ALBUM));
            items.add(new MenuObject(GO_TO_ARTIST));
            items.add(new MenuObject(SET_RINGTONE));
            items.add(new MenuObject(SEND_AUDIO_CLIP));
            items.add(new MenuObject(SET_START_TIME));
            items.add(new MenuObject(PREVIEW_SONG));
            items.add(new MenuObject(MOVE_TO_IGNORE));
            items.add(new MenuObject(SEND));
            items.add(new MenuObject(SHARE_ITEM));
            items.add(new MenuObject(CUT));
            items.add(new MenuObject(EDIT_TAGS));
            items.add(new MenuObject(DETAILS));
            items.add(new MenuObject(UPLOAD));
            items.add(
                    new MenuObject(
                            ADD_TO_LIBRARY,
                            (TrackRealmHelper.getLibraryList().indexOf(item.data) == -1)
                                    ? getString(R.string.add_to_library)
                                    : getString(R.string.remove_from_library)));
            items.add(new MenuObject(DELETE_ITEM));

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
            cloudItems.add(new MenuObject(MOVE_TO_IGNORE));
            cloudItems.add(new MenuObject(DOWNLOAD));
            cloudItems.add(new MenuObject(DETAILS));
            cloudItems.add(new MenuObject(DELETE_ITEM));

            final ArrayList<MenuObject> firebaseItems = new ArrayList<>();
            if (item.storage == CloudManager.FIREBASE && FirebaseManager.Instance().isOverLimit()) {
                if (item.isLibrary()) {
                    firebaseItems.add(new MenuObject(ADD_TO_LIBRARY, getString(R.string.remove_from_library)));
                }
                if (item.isSync()) {
                    firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
                }
            } else {
                firebaseItems.add(new MenuObject(PLAY_NEXT));
                firebaseItems.add(new MenuObject(PLAY_X_TIMES));
                firebaseItems.add(new MenuObject(ADD_TO_QUEUE));
                firebaseItems.add(new MenuObject(ADD_TO_PLAYLIST));
                firebaseItems.add(
                        new MenuObject(
                                FAV,
                                (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1)
                                        ? getString(R.string.add_to_favs)
                                        : getString(R.string.remove_from_favs)));
                firebaseItems.add(new MenuObject(MOVE_TO_IGNORE));
                firebaseItems.add(new MenuObject(DOWNLOAD));
                firebaseItems.add(new MenuObject(DETAILS));
                if (item.isLibrary()) {
                    firebaseItems.add(new MenuObject(ADD_TO_LIBRARY, getString(R.string.remove_from_library)));
                }
                if (item.isSync()) {
                    firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
                }
            }

            MaterialMenuAdapter materialMenuAdapter = null;
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


        } else {

            final CharSequence[] items = {"Add To Queue", "Add To Playlist", "Play Next", "Share", "Move to Ignore", "Delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(items, (dialog, which) -> {
                switch (which) {
                    case 0:     //add to q
                        AppController.Instance().actionItem(SearchActivity.this, position, queueItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_QUEUE);
                        break;

                    case 1:     //add to p
                        AppController.Instance().actionItem(SearchActivity.this, position, queueItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_SAVE);
                        break;

                    case 2:     //play next
                        AppController.Instance().actionItem(SearchActivity.this, position, queueItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_NEXT);
                        break;

                    case 3:     //share
                        AppController.Instance().actionItem(SearchActivity.this, position, queueItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_SHARE);
                        break;

                    case 4:     //negative
                        movetoIgnore(position, queueItem);
                        break;

                    case 5:     //remove
                        AppController.Instance().actionItem(SearchActivity.this, position, queueItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_DELETE);
                        break;

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onItemClicked(final QueueItem queueItem, int position) {

        if (queueItem == null) return;
        Intent playerlistIntent = new Intent(this, PlayerListActivity.class);

        switch (queueItem.type) {
            case MyApplication.TRACKS:
                AppController.Instance().play(0, position, searchAdapter.getList());
                break;
            case MyApplication.ARTISTS:
                playerlistIntent.putExtra(MyApplication.ARG_ID, queueItem.id);
                playerlistIntent.putExtra(MyApplication.ARG_ART, queueItem.album);
                playerlistIntent.putExtra(MyApplication.ARG_NAME, queueItem.title);
                playerlistIntent.putExtra(MyApplication.ARG_DATA, queueItem.title);
                playerlistIntent.putExtra(MyApplication.ARG_TYPE, PlayerConstants.QUEUE_TYPE_ARTISTS);
                playerlistIntent.putExtra(MyApplication.ARG_DURATION, queueItem.duration);
                playerlistIntent.putExtra(MyApplication.ARG_SONGS, queueItem.songs);
                ActivityCompat.startActivity(this, playerlistIntent, null);
                break;
            case MyApplication.ALBUMS:
                playerlistIntent.putExtra(MyApplication.ARG_ID, queueItem.id);
                playerlistIntent.putExtra(MyApplication.ARG_ART, queueItem.album);
                playerlistIntent.putExtra(MyApplication.ARG_NAME, queueItem.title);
                playerlistIntent.putExtra(MyApplication.ARG_TYPE, PlayerConstants.QUEUE_TYPE_ALBUMS);
                playerlistIntent.putExtra(MyApplication.ARG_DATA, queueItem.title);
                playerlistIntent.putExtra(MyApplication.ARG_DURATION, queueItem.duration);
                playerlistIntent.putExtra(MyApplication.ARG_SONGS, queueItem.songs);
                ActivityCompat.startActivity(this, playerlistIntent, null);
                break;
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        return false;
    }

    @Override
    public void onLoaded() {

        toggleVisibility();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(SearchActivity.this, selectedItem);
                } else {
                    AppController.toast(this, "Write settings permission wasn't provided. Muziko can't set default ringtone");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void delete(final int position, final QueueItem queue) {

        DeleteTrackRunnable deleteTrackRunnable = new DeleteTrackRunnable(position, queue);
        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
        if (storageInfoList.size() > 1) {
            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?", deleteTrackRunnable);
        } else {
            deleteTrackRunnable.run();
        }
    }

    private void deleteItems(final ArrayList<QueueItem> queueItems) {

        DeleteTracksRunnable deleteTracksRunnable = new DeleteTracksRunnable(queueItems);
        List<StorageUtils.StorageInfo> storageInfoList = StorageUtils.getStorageList();
        if (storageInfoList.size() > 1) {
            checkStoragePermissions("Do you want to grant SD card permissions so tags can be changed?", deleteTracksRunnable);
        } else {
            deleteTracksRunnable.run();
        }
    }

    private void favorite(final int position, QueueItem queue) {
        if (isFaving) return;
        isFaving = true;

        FavoriteEdit fe = new FavoriteEdit(this, 0, s -> {
            isFaving = false;

            searchAdapter.notifyItemChanged(position);
        });
        fe.execute(queue);
    }

    private void searchAdapter(String search) {

        searchAdapter.search(search);
    }

    private void toggleVisibility() {

        if (searchAdapter.getFilterCount() == 0) {
            searchRecyclerView.setVisibility(View.GONE);
        } else {
            searchRecyclerView.setVisibility(View.VISIBLE);
        }

    }

    private void register() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_EXIT);
        filter.addAction(AppController.INTENT_CLEAR);

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
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(
                () -> {
                    switch (item.id) {
                        case ADD_TO_QUEUE: //add to q
                            PlayerConstants.QUEUE_TYPE = 0;
                            AppController.Instance().addToQueue(this, selectedItem, false);
                            break;

                        case ADD_TO_PLAYLIST: //add to p
                            AppController.Instance().addToPlaylist(this, selectedItem);
                            break;

                        case FAV: //add to f`
                            favorite(selectedPosition, selectedItem);
                            break;

                        case PLAY_NEXT: //play next
                            AppController.Instance().addToQueue(this, selectedItem, true);
                            break;

                        case SET_START_TIME: //play from
                            PlayFrom playFrom = new PlayFrom();
                            playFrom.open(this, selectedItem);
                            break;

                        case GO_TO_ARTIST: //goto ar
                            AppController.Instance().gotoArtist(this, selectedItem, null);
                            break;

                        case GO_TO_ALBUM: //goto al
                            AppController.Instance().gotoAlbum(this, selectedItem, null);
                            break;

                        case SEND:
                            final ArrayList<MenuObject> subMenuItems = new ArrayList<>();
                            subMenuItems.add(new MenuObject(MenuObject.SEND_CONTACTS));
                            subMenuItems.add(new MenuObject(MenuObject.SEND_WIFI));

                            MaterialMenuAdapter MaterialMenuAdapter =
                                    new MaterialMenuAdapter(subMenuItems, onSubMenuObjectItemSelected);

                            new MaterialDialog.Builder(this)
                                    .adapter(MaterialMenuAdapter, new LinearLayoutManager(this))
                                    .show();

                            break;

                        case SET_RINGTONE: //createRingtone
                            boolean permission;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                permission = Settings.System.canWrite(this);
                            } else {
                                permission =
                                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                            }
                            if (!permission) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                                                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                                            intent.setData(
                                                                    Uri.parse("package:" + getPackageName()));
                                                            startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                                                        } else {
                                                            ActivityCompat.requestPermissions(
                                                                    this,
                                                                    new String[]{Manifest.permission.WRITE_SETTINGS},
                                                                    CODE_WRITE_SETTINGS_PERMISSION);
                                                        }
                                                    })
                                            .negativeText("Cancel")
                                            .show();

                                } else {
                                    SetRingtone createRingtone = new SetRingtone();
                                    createRingtone.open(this, selectedItem);
                                }
                            } else {
                                SetRingtone createRingtone = new SetRingtone();
                                createRingtone.open(this, selectedItem);
                            }
                            break;

                        case SEND_AUDIO_CLIP: //share ringtone
                            ShareRingtone shareRingtone = new ShareRingtone();
                            shareRingtone.open(this, selectedItem);
                            break;

                        case CUT: //cut
                            AppController.Instance().cutSong(selectedItem);
                            break;

                        case PREVIEW_SONG: //preview
                            PreviewSong previewSong = new PreviewSong();
                            previewSong.open(this, selectedItem);
                            break;

                        case EDIT_TAGS: //edit
                            AppController.Instance().editSong(this, TAG, selectedPosition, selectedItem);
                            break;

                        case DETAILS: //details
                            AppController.Instance().details(this, selectedItem);
                            break;

                        case SHARE_ITEM: //share
                            AppController.Instance().shareSong(this, selectedItem);
                            break;

                        case PLAY_X_TIMES: //remove
                            AppController.Instance().removeAfter(this, selectedItem);
                            break;

                        case MOVE_TO_IGNORE: //negative
                            movetoIgnore(selectedPosition, selectedItem);
                            break;

                        case DELETE_ITEM: //remove
                            delete(selectedPosition, selectedItem);

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
                            downloadFile.init(this, selectedItem);

                            break;

                        case UPLOAD:
                            if (networkState != NetworkInfo.State.CONNECTED) {
                                AppController.toast(this, getString(R.string.no_network_connection));
                                return;
                            }
                            UploadFile uploadFile = new UploadFile();
                            uploadFile.load(this, selectedItem);
                            break;

                        case ADD_TO_LIBRARY:
                            toggleLibrary(selectedPosition, selectedItem);
                            break;

                        case DONT_SYNC_FAV_OR_PLAYLIST: //dont sync
                            toggleSync(selectedItem, false);
                            break;
                    }

                    dialog.dismiss();
                },
                getResources().getInteger(R.integer.ripple_duration_delay));
    }

    @DebugLog
    private void toggleLibrary(final int pos, final QueueItem queue) {
        if (queue.isLibrary()) {
            LibraryEdit libraryEdit = new LibraryEdit(this, true, new LibraryEdit.LibraryEditListener() {
                @Override
                public void onLibraryEdited(boolean s) {
                    searchAdapter.notifyDataSetChanged();
                }
            });
            libraryEdit.execute(queue);
        } else {
            if (!FirebaseManager.Instance().isOverLimitNow(1)) {
                LibraryEdit libraryEdit = new LibraryEdit(this, new LibraryEdit.LibraryEditListener() {
                    @Override
                    public void onLibraryEdited(boolean s) {
                        searchAdapter.notifyItemChanged(pos);
                    }
                });
                libraryEdit.execute(queue);
            }
        }
    }

    private void toggleSync(final QueueItem queue, boolean sync) {

        Utils.askDelete(this, sync ? getString(R.string.sync_fav) : getString(R.string.dont_sync_fav), sync ? getString(R.string.sync_fav_confirm) : getString(R.string.dont_sync_fav_confirm), () -> {
            TrackRealmHelper.toggleSync(queue, sync);
            AppController.toast(this, sync ? getString(R.string.song_synced) : getString(R.string.song_not_synced));
            if (sync) {
                handler.postDelayed(() -> {
                    FirebaseManager.Instance().checkforFavsTransfers();
                }, UPLOAD_SETTLE_DELAY);
            } else {
                FirebaseManager.Instance().deleteFav(queue);
            }
        });
    }

    private class DeleteTrackRunnable implements Runnable {

        private int position;
        private QueueItem queueItem;

        public DeleteTrackRunnable(int position, QueueItem queueItem) {
            this.position = position;
            this.queueItem = queueItem;
        }

        @Override
        public void run() {
            Utils.askDelete(SearchActivity.this, "Delete Song", "This will delete song permanently from this device, do you want to proceed ?", () -> {
                TrackDelete tr = new TrackDelete(SearchActivity.this, PlayerConstants.QUEUE_TYPE_TRACKS, () -> searchAdapter.removeIndex(position));

                tr.execute(queueItem);
            });
        }
    }

    private class DeleteTracksRunnable implements Runnable {

        private ArrayList<QueueItem> queueItems;

        public DeleteTracksRunnable(ArrayList<QueueItem> queueItems) {
            this.queueItems = queueItems;
        }

        @Override
        public void run() {
            Utils.askDelete(SearchActivity.this, "Delete Songs", String.format("This will delete song%s permanently from this device, do you want to proceed ?", queueItems.size() != 1 ? "s" : ""), () -> {
                TrackDelete tr = new TrackDelete(SearchActivity.this, PlayerConstants.QUEUE_TYPE_TRACKS, () -> {
                    searchAdapter.removeAll(queueItems);
                    AppController.toast(SearchActivity.this, String.format("Song%s deleted from device", queueItems.size() != 1 ? "s" : ""));
                });
                tr.execute(queueItems);
            });
        }
    }

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                if (action.equals(AppController.INTENT_CLEAR)) {

                    finish();
                } else if (action.equals(AppController.INTENT_EXIT)) {

                    finish();
                }
            }
        }
    }
}
