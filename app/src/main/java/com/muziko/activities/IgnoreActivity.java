package com.muziko.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.badoo.mobile.util.WeakHandler;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.NegativeFolderAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.NegativeRecyclerItemListener;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.tasks.TrackDelete;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;


public class IgnoreActivity extends BaseActivity implements NegativeRecyclerItemListener, ActionMode.Callback, SearchView.OnCloseListener, SearchView.OnQueryTextListener, View.OnClickListener {
    private final String TAG = IgnoreActivity.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private MenuItem menuItemView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private boolean alreadyResumed = false;
    private FastScrollRecyclerView recyclerView;
    private RelativeLayout emptyLayout;
    private MenuItem menuItemSearch;
    private MenuItem menuItemClear;
    private MenuItem menu_sort_reverse;
    private MenuItem menu_sort_title;
    private MenuItem menu_sort_album;
    private MenuItem menu_sort_artist;
    private MenuItem menuclear;
    private MenuItem menurestore;
    private CoordinatorLayout coordinatorlayout;
    private ArrayList<QueueItem> negativeList;
    private NegativeFolderAdapter adapter;
    private boolean reverseSort;
    private int sortId;
    private ActionMode actionMode = null;
    private InterstitialAd mInterstitialAd;
    private AdListener mAdlistener;
    private ImageButton advancedSearch;
    private int advancedSearchInset;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(true);
        setContentView(R.layout.activity_ignore);

        toolbar = findViewById(R.id.toolbar);
        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
        toolbar.setTitle("Ignore");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(this);
        NpaGridLayoutManager layoutGrid2 = new NpaGridLayoutManager(this, 2);
        NpaGridLayoutManager layoutGrid3 = new NpaGridLayoutManager(this, 3);
        NpaGridLayoutManager layoutGrid4 = new NpaGridLayoutManager(this, 4);

        emptyLayout.setVisibility(View.GONE);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        negativeList = new ArrayList<>();
        negativeList.addAll(TrackRealmHelper.getNegative(0).values());
        adapter = new NegativeFolderAdapter(this, negativeList, PlayerConstants.QUEUE_TYPE_FAVORITES, prefShowArtwork, TAG, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new PicassoScrollListener(this, TAG));
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(adapter);

        setupMainPlayer();

        EventBus.getDefault().register(this);

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
        unregister();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
        register();
        mainUpdate();

        alreadyResumed = true;
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

    private void findViewsById() {

        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        recyclerView = findViewById(R.id.itemList);

    }

    private void onStorageChanged() {

        load();
    }

    private void load() {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        adapter.setShowArtwork(prefShowArtwork);
        negativeList.clear();
        negativeList.addAll(TrackRealmHelper.getNegative(0).values());
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
        getMenuInflater().inflate(R.menu.ignore_menu, menu);
        menuItemView = menu.findItem(R.id.negative_view);
        menuItemSearch = menu.findItem(R.id.negative_search);
        menuItemClear = menu.findItem(R.id.negative_clear);
        menuclear = menu.findItem(R.id.negative_clear);
        menurestore = menu.findItem(R.id.negative_restore);

        menu_sort_title = menu.findItem(R.id.player_sort_title);
        menu_sort_album = menu.findItem(R.id.player_sort_album);
        menu_sort_artist = menu.findItem(R.id.player_sort_artist);
        menu_sort_reverse = menu.findItem(R.id.reverse);

        onFilterValue(PrefsManager.Instance().getTrashSort(), PrefsManager.Instance().getTrashSortReverse());

//		updateGridMenu();
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
            ActionBar.LayoutParams searchviewParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint("Search Song or Artist");
            searchView.setOnQueryTextListener(this);
            searchView.setOnSearchClickListener(this);
            searchView.setOnCloseListener(this);
        }

        if (negativeList.size() == 0) {
            menuclear.setVisible(false);
            menurestore.setVisible(false);
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

            case R.id.ignore_add:
                PlayerConstants.QUEUE_TYPE = 0;

                Intent in = new Intent(this, SearchSongsActivity.class);
                in.putExtra(MyApplication.ARG_IGNORE, true);
                startActivity(in);
                return true;

            case R.id.player_sort_title:
                sortId = item.getItemId();
                PrefsManager.Instance().setTrashSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortTitleLowest();
                } else {
                    adapter.sortTitleHighest();
                }
                return true;
            case R.id.player_sort_album:
                sortId = item.getItemId();
                PrefsManager.Instance().setTrashSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortAlbumLowest();
                } else {
                    adapter.sortAlbumHighest();
                }
                return true;

            case R.id.player_sort_artist:
                sortId = item.getItemId();
                PrefsManager.Instance().setTrashSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortArtistLowest();
                } else {
                    adapter.sortArtistHighest();
                }
                return true;


            case R.id.reverse:
                if (item.isChecked()) {
                    item.setChecked(false);
                    reverseSort = false;
                    PrefsManager.Instance().setTrashSortReverse(false);
                } else {
                    item.setChecked(true);
                    reverseSort = true;
                    PrefsManager.Instance().setTrashSortReverse(true);
                }

                switch (sortId) {

                    case R.id.player_sort_title:
                        if (!reverseSort) {
                            adapter.sortTitleLowest();
                        } else {
                            adapter.sortTitleHighest();
                        }
                        break;

                    case R.id.player_sort_album:
                        if (!reverseSort) {
                            adapter.sortAlbumLowest();
                        } else {
                            adapter.sortAlbumHighest();
                        }
                        break;

                    case R.id.player_sort_artist:
                        if (!reverseSort) {
                            adapter.sortArtistLowest();
                        } else {
                            adapter.sortArtistHighest();
                        }
                        break;
                }

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

            case R.id.negative_restore:
                for (QueueItem queueItem : adapter.getList()) {
                    TrackRealmHelper.moveoutofNegative(queueItem);
                }
                reload();
                EventBus.getDefault().post(new RefreshEvent(1000));
                return true;

            case R.id.negative_clear:
                deleteItems(adapter.getList());
                EventBus.getDefault().post(new RefreshEvent(1000));
                return true;

            case R.id.negative_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.negative_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(IgnoreActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.negative_exit:
                AppController.Instance().exit();
                return true;

            default:
                return false;   //super.onOptionsItemSelected(item);
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

    private void onFilterValue(int value, boolean reverse) {

        if (menu_sort_reverse == null) {
            return;
        }

        if (reverse) {
            menu_sort_reverse.setChecked(true);
        }

        switch (value) {

            case R.id.player_sort_title:
                if (!reverse) {
                    adapter.sortTitleLowest();
                } else {
                    adapter.sortTitleHighest();
                }
                menu_sort_title.setChecked(true);
                break;
            case R.id.player_sort_album:
                if (!reverse) {
                    adapter.sortAlbumLowest();
                } else {
                    adapter.sortAlbumHighest();
                }
                menu_sort_album.setChecked(true);
                break;
            case R.id.player_sort_artist:
                if (!reverse) {
                    adapter.sortArtistLowest();
                } else {
                    adapter.sortArtistHighest();
                }
                menu_sort_artist.setChecked(true);
                break;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.context_negative_folder, menu);
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

                case R.id.restore:
                    Utils.askDelete(this, "Restore", "Are you sure you want to restore selected tracks?", () -> {
                        for (QueueItem queueItem : list) {
                            TrackRealmHelper.moveoutofNegative(queueItem);
                        }
                        reload();
                    });

                    break;

                case R.id.delete:
                    Utils.askDelete(this, "Restore", "Are you sure you want to delete selected tracks?", () -> {
                        deleteItems(list);
                    });

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
    public void onMenuClicked(final int position) {
        final QueueItem item = adapter.getItem(position);
        if (item == null) return;

        final CharSequence[] items = {"Restore", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0:     //restore
                    movetoNegative(position, item);
                    break;

                case 1:     //delete
                    delete(position, item);
                    break;
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onItemClicked(int position) {
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
            IgnoreActivity.this.startSupportActionMode(this);

            this.adapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(this, "Restore", "Are you sure you want to restore this track?", () -> {

            TrackRealmHelper.moveoutofNegative(queue);
            adapter.removeIndex(position);
            EventBus.getDefault().post(new RefreshEvent(1000));
        });
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

    private void reload() {

        load();
        emptied();
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0)
                actionMode.setTitle("");
            else
                actionMode.setTitle(String.format("%d song%s", count, count != 1 ? "s" : ""));
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

    private void register() {

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

        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, filter);
    }

    private void unregister() {

        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
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
            Utils.askDelete(IgnoreActivity.this, "Delete Song", "This will delete song permanently from this device, do you want to proceed ?", () -> {
                TrackDelete tr = new TrackDelete(IgnoreActivity.this, PlayerConstants.QUEUE_TYPE_TRACKS, () -> adapter.removeIndex(position));

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
            Utils.askDelete(IgnoreActivity.this, "Delete Songs", String.format("Are you sure you want to delete these song%s ?", queueItems.size() != 1 ? "s" : ""), () -> {

                ArrayList<QueueItem> del = new ArrayList<>();
                for (QueueItem item : queueItems) {
                    if (item == null) continue;

                    TrackRealmHelper.deleteTrack(item.data);

                    del.add(item);
                }

                adapter.removeAll(del);
                emptied();
                AppController.toast(IgnoreActivity.this, String.format("Song%s deleted", del.size() != 1 ? "s" : ""));

                del.clear();
            });
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
                }
            }
        }
    }
}
