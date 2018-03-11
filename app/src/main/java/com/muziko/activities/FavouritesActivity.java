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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
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
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.DownloadFile;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.events.BufferingEvent;
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
import com.muziko.tasks.LibraryEdit;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import hugo.weaving.DebugLog;

import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.AppController.ACTION_FIREBASE_OVERLIMIT;
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
import static com.muziko.objects.MenuObject.GO_TO_ALBUM;
import static com.muziko.objects.MenuObject.GO_TO_ARTIST;
import static com.muziko.objects.MenuObject.MOVE_TO_IGNORE;
import static com.muziko.objects.MenuObject.PLAY_NEXT;
import static com.muziko.objects.MenuObject.PLAY_X_TIMES;
import static com.muziko.objects.MenuObject.PREVIEW_SONG;
import static com.muziko.objects.MenuObject.SET_RINGTONE;
import static com.muziko.objects.MenuObject.SYNC_FAV_OR_PLAYLIST;

public class FavouritesActivity extends BaseActivity
        implements RecyclerItemListener,
        ActionMode.Callback,
        SearchView.OnQueryTextListener,
        MaterialMenuAdapter.Callback {
    private final WeakHandler handler = new WeakHandler();
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private String TAG = FavouritesActivity.class.getName();
    private MenuItem menuItemView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Toolbar toolbar;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private boolean alreadyResumed = false;
    private FastScrollRecyclerView recyclerView;
    private CoordinatorLayout coordinatorlayout;
    private RelativeLayout mainLayout;
    private RelativeLayout emptyLayout;
    private MenuItem menuItemSearch;
    private MenuItem menuItemClear;
    private MenuItem menuItemStorage;
    private MenuItem menuItemStorageAll;
    private MenuItem menuItemStorageInternal;
    private MenuItem menuItemStorageSD;
    private MenuItem menuItemFavourite;
    private MenuItem menuItemgridone;
    private MenuItem menuItemgridtwo;
    private MenuItem menuItemgridthree;
    private MenuItem menuItemgridfour;
    private ArrayList<QueueItem> favoriteList;
    private FavouriteSaver taskSaver;
    private boolean favChanged = false;
    private boolean isSaving = false;
    private FavouritesReceiver receiver;
    private GeneralAdapter adapter;
    private FavoritesItemTouchHelper touchCallback = new FavoritesItemTouchHelper();
    private ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
    private NpaLinearLayoutManager layoutList;
    private NpaGridLayoutManager layoutGrid2;
    private NpaGridLayoutManager layoutGrid3;
    private NpaGridLayoutManager layoutGrid4;
    private ActionMode actionMode = null;
    private BoxApi boxApi;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;
    private boolean isAddingToLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(true);
        setContentView(R.layout.activity_favourites);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.favourites);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        layoutList = new NpaLinearLayoutManager(this);
        layoutGrid2 = new NpaGridLayoutManager(this, 2);
        layoutGrid3 = new NpaGridLayoutManager(this, 3);
        layoutGrid4 = new NpaGridLayoutManager(this, 4);

        emptyLayout.setVisibility(View.GONE);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        favoriteList = new ArrayList<>();
        favoriteList.addAll(TrackRealmHelper.getFavorites(0).values());

        adapter = new GeneralAdapter(this, favoriteList, PlayerConstants.QUEUE_TYPE_FAVORITES, prefShowArtwork, TAG, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new PicassoScrollListener(this, TAG));

        int grid = PrefsManager.Instance().getFavouriteViewType();
        switch (grid) {
            case 0:
                recyclerView.setLayoutManager(layoutList);
                break;
            case 1:
                recyclerView.setLayoutManager(layoutGrid2);
                break;
            case 2:
                recyclerView.setLayoutManager(layoutGrid3);
                break;
            case 3:
                recyclerView.setLayoutManager(layoutGrid4);
                break;

            default:
                recyclerView.setLayoutManager(layoutList);
        }
        recyclerView.setAdapter(adapter);

        touchHelper.attachToRecyclerView(recyclerView);

        onListingChanged();
        setupMainPlayer();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
        register();
        mainUpdate();
        FirebaseManager.Instance().checkforFavsTransfers();
        alreadyResumed = true;
    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
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
                createRingtone.open(FavouritesActivity.this, selectedItem);
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
            miniPlayer.open();
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

        unregister();
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        if (favChanged) {
            save();
        }
        super.onPause();
    }

    @DebugLog
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

    private void findViewsById() {

        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        recyclerView = findViewById(R.id.itemList);
    }

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void updateGridMenu() {
        if (menuItemStorage != null) {

            if (PrefsManager.Instance().getFavouriteViewType() == 0) {
                menuItemgridone.setChecked(true);
            } else if (PrefsManager.Instance().getFavouriteViewType() == 1) {
                menuItemgridtwo.setChecked(true);
            } else if (PrefsManager.Instance().getFavouriteViewType() == 2) {
                menuItemgridthree.setChecked(true);
            } else if (PrefsManager.Instance().getFavouriteViewType() == 3) {
                menuItemgridfour.setChecked(true);
            }
        }
    }

    private void onListingChanged() {

        if (adapter.getGridtype() != PrefsManager.Instance().getFavouriteViewType()) {
            adapter.setGridtype(PrefsManager.Instance().getFavouriteViewType());
            adapter.notifyRemoveEach();
            switch (PrefsManager.Instance().getFavouriteViewType()) {
                case 0:
                    recyclerView.setLayoutManager(layoutList);
                    break;
                case 1:
                    recyclerView.setLayoutManager(layoutGrid2);
                    break;
                case 2:
                    recyclerView.setLayoutManager(layoutGrid3);
                    break;
                case 3:
                    recyclerView.setLayoutManager(layoutGrid4);
                    break;

                default:
                    recyclerView.setLayoutManager(layoutList);
            }

            adapter.notifyAddEach();
        }
    }

    private void onStorageChanged() {

        loadFavorites();
    }

    private void loadFavorites() {
        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        adapter.setShowArtwork(prefShowArtwork);

        favoriteList.clear();
        favoriteList.addAll(TrackRealmHelper.getFavorites(0).values());
        adapter.notifyDataSetChanged();
        adapter.setStorage(0);
    }

    private void mainUpdate() {
        miniPlayer.updateUI();

        alreadyResumed = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.favourite_menu, menu);
        menuItemView = menu.findItem(R.id.favourite_view);
        menuItemSearch = menu.findItem(R.id.favourite_search);
        menuItemClear = menu.findItem(R.id.favourite_clear);

        menuItemStorage = menu.findItem(R.id.storage_filter);
        menuItemStorageAll = menu.findItem(R.id.player_storage_all);
        menuItemStorageInternal = menu.findItem(R.id.player_storage_internal);
        menuItemStorageSD = menu.findItem(R.id.player_storage_sd);

        menuItemgridone = menu.findItem(R.id.grid_one);
        menuItemgridtwo = menu.findItem(R.id.grid_two);
        menuItemgridthree = menu.findItem(R.id.grid_three);
        menuItemgridfour = menu.findItem(R.id.grid_four);

        updateGridMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menuItemClear != null) {
            menuItemClear.setVisible(adapter.getItemCount() != 0);
        }
        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();
            searchView.setQueryHint("Search Song or Artist");
            searchView.setOnQueryTextListener(this);
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

            case R.id.favourite_add:
                PlayerConstants.QUEUE_TYPE = 0;

                Intent in = new Intent(this, SearchSongsActivity.class);
                in.putExtra(MyApplication.ARG_FAV, true);
                startActivity(in);
                return true;

            case R.id.favourite_search:
                return true;

            case R.id.player_storage_all:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(0);
                onStorageChanged();
                return true;

            case R.id.player_storage_internal:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(1);
                onStorageChanged();
                return true;

            case R.id.player_storage_sd:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setStorageViewType(2);
                onStorageChanged();
                return true;

            case R.id.grid_one:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setFavouriteViewType(0);
                onListingChanged();

                return true;

            case R.id.grid_two:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setFavouriteViewType(1);
                onListingChanged();

                return true;

            case R.id.grid_three:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setFavouriteViewType(2);
                onListingChanged();

                return true;

            case R.id.grid_four:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                PrefsManager.Instance().setFavouriteViewType(3);
                onListingChanged();

                return true;

            case R.id.favourite_clear:
                TrackRealmHelper.removeAllFavorites();
                //				FavoriteItem.deleteAll();
                AppController.toast(this, "Favourites cleared!");

                reload();
                return true;

            case R.id.favourite_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.favourite_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(FavouritesActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.favourite_exit:
                AppController.Instance().exit();
                return true;

            default:
                return false; //super.onOptionsItemSelected(item);
        }
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
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.context_multiselect, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        menuItemFavourite = menu.findItem(R.id.favourite);
        menuItemFavourite.setVisible(false);
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
        items.add(new MenuObject(PLAY_NEXT));
        items.add(new MenuObject(PLAY_X_TIMES));
        items.add(new MenuObject(ADD_TO_QUEUE));
        items.add(new MenuObject(ADD_TO_PLAYLIST));
        items.add(new MenuObject(GO_TO_ARTIST));
        items.add(new MenuObject(GO_TO_ALBUM));
        items.add(new MenuObject(SET_RINGTONE));
        items.add(new MenuObject(CUT));
        items.add(new MenuObject(PREVIEW_SONG));
        items.add(new MenuObject(EDIT_TAGS));
        items.add(new MenuObject(DETAILS));
        items.add(
                new MenuObject(
                        ADD_TO_LIBRARY,
                        (TrackRealmHelper.getLibraryList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_library)
                                : getString(R.string.remove_from_library)));
        if (TrackRealmHelper.getTrack(item.data).isSync()) {
            items.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
        } else {
            items.add(new MenuObject(SYNC_FAV_OR_PLAYLIST));
        }
        items.add(new MenuObject(MOVE_TO_IGNORE));
        items.add(new MenuObject(DELETE_ITEM));

        final ArrayList<MenuObject> cloudItems = new ArrayList<>();
        cloudItems.add(new MenuObject(PLAY_NEXT));
        cloudItems.add(new MenuObject(PLAY_X_TIMES));
        cloudItems.add(new MenuObject(ADD_TO_QUEUE));
        cloudItems.add(new MenuObject(ADD_TO_PLAYLIST));
        cloudItems.add(new MenuObject(DOWNLOAD));
        cloudItems.add(new MenuObject(DETAILS));
        cloudItems.add(new MenuObject(MOVE_TO_IGNORE));
        cloudItems.add(new MenuObject(DELETE_ITEM));

        final ArrayList<MenuObject> firebaseItems = new ArrayList<>();
        if (item.storage == CloudManager.FIREBASE && FirebaseManager.Instance().isOverLimit()) {
            firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
        } else {
            firebaseItems.add(new MenuObject(PLAY_NEXT));
            firebaseItems.add(new MenuObject(PLAY_X_TIMES));
            firebaseItems.add(new MenuObject(ADD_TO_QUEUE));
            firebaseItems.add(new MenuObject(ADD_TO_PLAYLIST));
            firebaseItems.add(new MenuObject(DOWNLOAD));
            firebaseItems.add(new MenuObject(DETAILS));
            firebaseItems.add(new MenuObject(MOVE_TO_IGNORE));
            firebaseItems.add(new MenuObject(DONT_SYNC_FAV_OR_PLAYLIST));
        }

        MaterialMenuAdapter materialMenuAdapter = new MaterialMenuAdapter(items, this);
        materialMenuAdapter = null;
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
            //if (PlayerConstants.QUEUE_TYPE != PlayerConstants.QUEUE_TYPE_FAVORITES) {

            AppController.Instance().play(PlayerConstants.QUEUE_TYPE_FAVORITES, position, this.adapter.getList());

      /*} else {
          PlayerConstants.QUEUE_INDEX = position;
          PlayerConstants.QUEUE_TIME = 0;

          Application.servicePlay(getActivity());
      }*/
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.adapter.isMultiSelect()) {
            FavouritesActivity.this.startSupportActionMode(this);

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
                            AppController.Instance().addToQueue(FavouritesActivity.this, selectedItem, false);
                            break;

                        case ADD_TO_PLAYLIST: //add to p
                            AppController.Instance().addToPlaylist(FavouritesActivity.this, selectedItem);
                            break;

                        case PLAY_NEXT: //play next
                            AppController.Instance().addToQueue(FavouritesActivity.this, selectedItem, true);
                            break;

                        case GO_TO_ARTIST: //goto ar
                            AppController.Instance().gotoArtist(FavouritesActivity.this, selectedItem, null);
                            break;

                        case GO_TO_ALBUM: //goto al
                            AppController.Instance().gotoAlbum(FavouritesActivity.this, selectedItem, null);
                            break;

                        case SET_RINGTONE: //createRingtone
                            boolean permission;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                permission = Settings.System.canWrite(FavouritesActivity.this);
                            } else {
                                permission =
                                        ContextCompat.checkSelfPermission(
                                                FavouritesActivity.this, Manifest.permission.WRITE_SETTINGS)
                                                == PackageManager.PERMISSION_GRANTED;
                            }
                            if (!permission) {

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    new MaterialDialog.Builder(FavouritesActivity.this)
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
                                                                    FavouritesActivity.this,
                                                                    new String[]{Manifest.permission.WRITE_SETTINGS},
                                                                    CODE_WRITE_SETTINGS_PERMISSION);
                                                        }
                                                    })
                                            .negativeText("Cancel")
                                            .show();
                                } else {
                                    SetRingtone createRingtone = new SetRingtone();
                                    createRingtone.open(FavouritesActivity.this, selectedItem);
                                }
                            } else {
                                SetRingtone createRingtone = new SetRingtone();
                                createRingtone.open(FavouritesActivity.this, selectedItem);
                            }

                            break;

                        case CUT: //cut
                            AppController.Instance().cutSong(selectedItem);
                            break;

                        case PREVIEW_SONG: //preview
                            PreviewSong previewSong = new PreviewSong();
                            previewSong.open(FavouritesActivity.this, selectedItem);
                            break;

                        case EDIT_TAGS: //edit
                            AppController.Instance()
                                    .editSong(this, "FavouritesActivity", selectedItemPosition, selectedItem);
                            break;

                        case DETAILS:
                            AppController.Instance().details(FavouritesActivity.this, selectedItem);
                            break;

                        case ADD_TO_LIBRARY:
                            toggleLibrary(selectedItemPosition, selectedItem);
                            break;


                        case PLAY_X_TIMES: //remove
                            AppController.Instance().removeAfter(FavouritesActivity.this, selectedItem);
                            break;

                        case MOVE_TO_IGNORE: //negative
                            movetoNegative(selectedItemPosition, selectedItem);
                            break;

                        case DELETE_ITEM: //remove
                            delete(selectedItemPosition, selectedItem);
                            break;

                        case DOWNLOAD:
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

                        case SYNC_FAV_OR_PLAYLIST: //sync
                            toggleSync(selectedItem, true);
                            break;

                        case DONT_SYNC_FAV_OR_PLAYLIST: //dont sync
                            toggleSync(selectedItem, false);
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(FavouritesActivity.this, selectedItem);
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

    private void movetoNegative(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(
                this,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    for (int i = 0; i < queueItems.size(); i++) {
                        QueueItem queueItem = queueItems.get(i);
                        TrackRealmHelper.movetoNegative(queueItem);
                        loadFavorites();
                    }
                    EventBus.getDefault().post(new RefreshEvent(1000));
                    emptied();
                });
    }

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(
                this,
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    TrackRealmHelper.movetoNegative(queue);
                    adapter.removeIndex(position);
                    EventBus.getDefault().post(new RefreshEvent(1000));
                });
    }

    private void toggleSync(final QueueItem queue, boolean sync) {

        if (sync && FirebaseManager.Instance().isOverLimitNow(1)) return;

        Utils.askDelete(FavouritesActivity.this, sync ? getString(R.string.sync_fav) : getString(R.string.dont_sync_fav), sync ? getString(R.string.sync_fav_confirm) : getString(R.string.dont_sync_fav_confirm), () -> {
            TrackRealmHelper.toggleSync(queue, sync);
            AppController.toast(FavouritesActivity.this, sync ? getString(R.string.song_synced) : getString(R.string.song_not_synced));
            if (sync) {
                handler.postDelayed(() -> {
                    FirebaseManager.Instance().checkforFavsTransfers();
                }, UPLOAD_SETTLE_DELAY);
            } else {
                FirebaseManager.Instance().deleteFav(queue);
            }
        });
    }

    private void delete(final int position, final QueueItem queue) {
        Utils.askDelete(FavouritesActivity.this, "Delete From Favourite", "Are you sure you want to delete this song from your Favourites ?", () -> {
            if (TrackRealmHelper.removeFavorite(queue.data)) {
                adapter.removeIndex(position);

                TrackRealmHelper.toggleSync(queue, false);
                FirebaseManager.Instance().deleteFav(queue);
                PlayerConstants.QUEUE_TYPE = 0;

                sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

                AppController.toast(FavouritesActivity.this, "Song deleted from your Favorites");
            } else {
                AppController.toast(FavouritesActivity.this, "Unable to delete from your Favorites");
            }
        });
    }

    private void deleteItems(final ArrayList<QueueItem> list) {
        Utils.askDelete(
                FavouritesActivity.this,
                "Delete From Favourite",
                String.format(
                        "Are you sure you want to delete these song%s from your Favourites ?",
                        list.size() != 1 ? "s" : ""),
                () -> {
                    ArrayList<QueueItem> del = new ArrayList<>();
                    for (QueueItem item : list) {
                        if (item == null) continue;

                        TrackRealmHelper.removeFavorite(item.data);

                        del.add(item);
                    }

                    adapter.removeAll(del);

                    emptied();
                    PlayerConstants.QUEUE_TYPE = 0;

                    sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
                    sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                    sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));

                    AppController.toast(
                            FavouritesActivity.this,
                            String.format("Song%s deleted from your Favorites", del.size() != 1 ? "s" : ""));

                    del.clear();
                });
    }

    private void addSongs() {
        PlayerConstants.QUEUE_TYPE = 0;

        Intent in = new Intent(FavouritesActivity.this, SearchSongsActivity.class);
        in.putExtra(MyApplication.ARG_FAV, true);
        startActivity(in);
    }

    private void reload() {

        loadFavorites();
        emptied();
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0) actionMode.setTitle("");
            else actionMode.setTitle(String.format("%d song%s", count, count != 1 ? "s" : ""));
        }
    }

    private void emptied() {
        if (adapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
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

        taskSaver = new FavouriteSaver(FavouritesActivity.this);
        taskSaver.execute();
    }

    private void register() {
        receiver = new FavouritesReceiver();

        IntentFilter favfilter = new IntentFilter();
        favfilter.addAction(AppController.INTENT_FAVOURITE_CHANGED);
        favfilter.addAction(AppController.INTENT_TRACK_EDITED);
        registerReceiver(receiver, favfilter);

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

    public class FavouriteSaver extends AsyncTask<Void, int[], Boolean> {
        private final Context ctx;

        public FavouriteSaver(Context context) {
            this.ctx = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //			FavoriteItem.deleteAll();
            TrackRealmHelper.removeAllFavorites();

            ArrayList<QueueItem> list = adapter.getList();

            int counter = 0;
            for (QueueItem queue : list) {
                //				FavoriteItem item = new FavoriteItem();

                //				item.insert(queue);
                if (TrackRealmHelper.addFavorite(queue.data)) {
                    counter++;
                }
            }

            //			MyApplication.favorites = Utils.loadFavs(ctx);
            favChanged = false;
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

            isSaving = false;
        }
    }

    private class FavouritesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    if (action.equals(AppController.INTENT_FAVOURITE_CHANGED)) {
                        reload();
                    } else if (action.equals(AppController.INTENT_TRACK_EDITED)) {
                        int index = intent.getIntExtra("index", -1);
                        String tag = intent.getStringExtra("tag");
                        QueueItem item = (QueueItem) intent.getSerializableExtra("item");
                        if (item != null
                                && tag != null
                                && tag.equals("FavouritesActivity")
                                && index >= 0
                                && index < adapter.getItemCount()) {
                            adapter.put(index, item);
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

    private class FavoritesItemTouchHelper extends ItemTouchHelper.Callback {
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
                favChanged = true;
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
                    case AppController.INTENT_TRACK_REPEAT:
                        mainUpdate();
                        break;
                    case AppController.INTENT_TRACK_SHUFFLE:
                        mainUpdate();
                        break;
                    case AppController.INTENT_QUEUE_STOPPED:
                        miniPlayer.layoutMiniPlayer();

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
