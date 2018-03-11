package com.muziko.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.transition.AutoTransition;
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
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.GeneralAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.AdvancedSearchButton;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.RecyclerViewUtils;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.tasks.PlaylistDelete;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import hugo.weaving.DebugLog;

public class PlaylistActivity extends BaseActivity implements RecyclerItemListener, ActionMode.Callback, SearchView.OnQueryTextListener, View.OnClickListener, SearchView.OnCloseListener {
    private final String TAG = PlaylistActivity.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final PlaylistItemTouchHelper touchCallback = new PlaylistItemTouchHelper();
    private final ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
    public MenuItem menuItemView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private boolean listChanged = false;
    private boolean reverseSort;
    private int sortId;
    private int customsortId;
    private boolean alreadyResumed = false;
    private CoordinatorLayout coordinatorlayout;
    private RelativeLayout emptyLayout;
    private FastScrollRecyclerView recyclerView;
    private MenuItem menuItemSearch;
    private MenuItem menu_sort_title;
    private MenuItem menu_sort_duration;
    private MenuItem menu_sort_date;
    private MenuItem menu_sort_custom;
    private MenuItem menu_sort_reverse;
    private MenuItem menuItemgridone;
    private MenuItem menuItemgridtwo;
    private MenuItem menuItemgridthree;

    //	private PlaylistLoader task;
//	private boolean isBusy = false;
    private MenuItem menuItemgridfour;
    private ArrayList<QueueItem> playList;
    private ActionMode actionMode = null;
    private GeneralAdapter adapter;
    private PlaylistReceiver receiver;
    private int count = 0;
    private int batch = 500;
    private InterstitialAd mInterstitialAd;
    private AdListener mAdlistener;
    private ImageButton advancedSearch;
    private int advancedSearchInset;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        setupWindowAnimations();

        toolbar = findViewById(R.id.toolbar);
        advancedSearchInset = toolbar.getContentInsetStartWithNavigation();
        toolbar.setTitle("Playlist");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        Activity activity = this;

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        playList = new ArrayList<>();
        adapter = new GeneralAdapter(this, playList, PlayerConstants.QUEUE_TYPE_PLAYLIST, prefShowArtwork, TAG, this);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(this);
        NpaGridLayoutManager layoutGrid2 = new NpaGridLayoutManager(this, 2);
        NpaGridLayoutManager layoutGrid3 = new NpaGridLayoutManager(this, 3);
        NpaGridLayoutManager layoutGrid4 = new NpaGridLayoutManager(this, 4);

        emptyLayout.setVisibility(View.GONE);
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
//        FirebaseManager.Instance().checkforPlaylistTransfers();
    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
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
    protected void onPause() {

        super.onPause();
        if (miniPlayer != null) {
            miniPlayer.pause();
        }
        if (listChanged) {

            ArrayList<QueueItem> queueItemList;
            queueItemList = adapter.getList();
            for (int i = 0; i < queueItemList.size(); i++) {
                QueueItem queueItem = queueItemList.get(i);
                PlaylistItem item = PlaylistItem.copyQueue(queueItem);
                PlaylistRealmHelper.updateOrder(item, i);
            }
        }

        unregister();
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

    @DebugLog
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFirebaseRefreshEvent(FirebaseRefreshEvent event) {

        reload();
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

    private void mainUpdate() {
        miniPlayer.updateUI();

        alreadyResumed = true;
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= 21) {
            AutoTransition enterauto = new AutoTransition();
            enterauto.setDuration(1000);
            getWindow().setEnterTransition(enterauto);
            getWindow().setReenterTransition(enterauto);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_playlist, menu);

        menuItemSearch = menu.findItem(R.id.playlist_search);
        MenuItem menuItemAdd = menu.findItem(R.id.playlist_add);

        MenuItem menuItemStorage = menu.findItem(R.id.storage_filter);
        MenuItem menuItemStorageAll = menu.findItem(R.id.player_storage_all);
        MenuItem menuItemStorageInternal = menu.findItem(R.id.player_storage_internal);
        MenuItem menuItemStorageSD = menu.findItem(R.id.player_storage_sd);

        menu_sort_title = menu.findItem(R.id.player_sort_title);
        menu_sort_duration = menu.findItem(R.id.player_sort_duration);
        menu_sort_date = menu.findItem(R.id.player_sort_date);
        MenuItem menu_sort_songs = menu.findItem(R.id.player_sort_songs);
        menu_sort_custom = menu.findItem(R.id.player_sort_custom);
        menu_sort_reverse = menu.findItem(R.id.reverse);

        customsortId = R.id.player_sort_custom;

        onFilterValue(PrefsManager.Instance().getPlaylistSort(), PrefsManager.Instance().getPlaylistSortReverse());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menuItemSearch != null) {
            SearchView searchView = (SearchView) menuItemSearch.getActionView();
            AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
            Resources resources = getResources();
            advancedSearch = advancedSearchButton.addButton(this, resources, searchView);
            advancedSearch.setOnClickListener(this);
            ActionBar.LayoutParams searchviewParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setQueryHint("Search Playlist");
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

            case R.id.playlist_search:
                return true;

            case R.id.player_sort_title:
                sortId = item.getItemId();
                PrefsManager.Instance().setPlaylistSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortTitleLowest();
                } else {
                    adapter.sortTitleHighest();
                }
                return true;
            case R.id.player_sort_duration:
                sortId = item.getItemId();
                PrefsManager.Instance().setPlaylistSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortDurationSmallest();
                } else {
                    adapter.sortDurationLargest();
                }
                return true;

            case R.id.player_sort_date:
                sortId = item.getItemId();
                PrefsManager.Instance().setPlaylistSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortDateEarliest();
                } else {
                    adapter.sortDateLatest();
                }
                return true;

            case R.id.player_sort_songs:
                sortId = item.getItemId();
                PrefsManager.Instance().setPlaylistSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortSongsLowest();
                } else {
                    adapter.sortSongsHighest();
                }
                return true;

            case R.id.player_sort_custom:
                sortId = item.getItemId();
                PrefsManager.Instance().setPlaylistSort(item.getItemId());
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                if (!reverseSort) {
                    adapter.sortPlaylistLowest();
                } else {
                    adapter.sortplaylistHighest();
                }
                return true;

            case R.id.reverse:
                if (item.isChecked()) {
                    item.setChecked(false);
                    reverseSort = false;
                    PrefsManager.Instance().setPlaylistSortReverse(false);
                } else {
                    item.setChecked(true);
                    reverseSort = true;
                    PrefsManager.Instance().setPlaylistSortReverse(true);
                }

                switch (sortId) {

                    case R.id.player_sort_title:
                        if (!reverseSort) {
                            adapter.sortTitleLowest();
                        } else {
                            adapter.sortTitleHighest();
                        }
                        break;

                    case R.id.player_sort_duration:
                        if (!reverseSort) {
                            adapter.sortDurationSmallest();
                        } else {
                            adapter.sortDurationLargest();
                        }
                        break;

                    case R.id.player_sort_date:
                        if (!reverseSort) {
                            adapter.sortDateEarliest();
                        } else {
                            adapter.sortDateLatest();
                        }
                        break;
                    case R.id.player_sort_songs:
                        if (!reverseSort) {
                            adapter.sortSongsLowest();
                        } else {
                            adapter.sortSongsHighest();
                        }
                        break;
                    case R.id.player_sort_custom:
                        if (!reverseSort) {
                            adapter.sortPlaylistLowest();
                        } else {
                            adapter.sortplaylistHighest();
                        }
                        break;
                }

                return true;

            case R.id.player_storage_all:
                PrefsManager.Instance().setStorageViewType(0);
                return true;

            case R.id.player_storage_internal:
                PrefsManager.Instance().setStorageViewType(1);
                return true;

            case R.id.player_storage_sd:
                PrefsManager.Instance().setStorageViewType(2);
                return true;

            case R.id.playlist_add:
                create();
                return true;

            case R.id.playlist_clearall:
                deletePlaylists(playList);
                return true;

            case R.id.playlist_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.playlist_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(PlaylistActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.playlist_exit:
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
            case R.id.player_sort_duration:
                if (!reverse) {
                    adapter.sortDurationSmallest();
                } else {
                    adapter.sortDurationLargest();
                }
                menu_sort_duration.setChecked(true);
                break;
            case R.id.player_sort_date:
                if (!reverse) {
                    adapter.sortDateEarliest();
                } else {
                    adapter.sortDateLatest();
                }
                menu_sort_date.setChecked(true);
                break;
            case R.id.player_sort_songs:
                if (!reverse) {
                    adapter.sortSongsLowest();
                } else {
                    adapter.sortSongsHighest();
                }
                menu_sort_date.setChecked(true);
                break;
            case R.id.player_sort_custom:
                if (!reverse) {
                    adapter.sortPlaylistLowest();
                } else {
                    adapter.sortplaylistHighest();
                }
                menu_sort_custom.setChecked(true);
                break;
        }
        //}
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.context_playlist_multiselect, menu);
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


                case R.id.add_to_queue:
                    ArrayList<QueueItem> tracks = new ArrayList<>();
                    for (QueueItem queueItem : list) {
                        tracks.addAll(PlaylistSongRealmHelper.loadAllByPlaylist(0, queueItem.id));
                    }

                    AppController.Instance().addToQueue(this, tracks, false);
                    break;

                case R.id.delete:
                    deletePlaylists(list);
                    break;

                case R.id.merge:
                    AppController.Instance().mergePlayList(this, list);
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
//        final CharSequence[] syncedItems = {"Edit", "Delete", "Share", "Clone", "Don't Sync"};
//        final CharSequence[] notsyncedItems = {"Edit", "Delete", "Share", "Clone", "Sync"};
        final CharSequence[] syncedItems = {"Edit", "Delete", "Share", "Clone"};
        final CharSequence[] notsyncedItems = {"Edit", "Delete", "Share", "Clone"};

        PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(item.getId());
        if (playlistItem != null && playlistItem.isSync()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(syncedItems, (dialog, which) -> {
                switch (which) {
                    case 0:     //edit
                        edit(position, item);
                        break;

                    case 1:     //remove
                        delete(position, item, true);
                        break;

                    case 2:     //share
                        share(item);
                        break;

                    case 3:     //remove
                        clonePlaylist(item);
                        break;

                    case 4:     //dont sync
                        toggleSync(item, false);
                        break;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(notsyncedItems, (dialog, which) -> {
                switch (which) {
                    case 0:     //edit
                        edit(position, item);
                        break;

                    case 1:     //remove
                        delete(position, item, false);
                        break;

                    case 2:     //share
                        share(item);
                        break;

                    case 3:     //remove
                        clonePlaylist(item);
                        break;

                    case 4:     //sync
                        toggleSync(item, true);
                        break;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }


    }

    @Override
    public void onItemClicked(int position) {


        if (this.adapter.isMultiSelect()) {
            toggleSelection(position);
        } else {
            handler.postDelayed(
                    () -> open(PlaylistItem.copyQueue(adapter.getItem(position))),
                    getResources().getInteger(R.integer.ripple_duration_delay));

        }

    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.adapter.isMultiSelect()) {
            PlaylistActivity.this.startSupportActionMode(this);

            this.adapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    private void toggleSync(final QueueItem playlistItem, boolean sync) {
        PlaylistItem item = PlaylistRealmHelper.getPlaylist(playlistItem.id);

        if (sync && FirebaseManager.Instance().isOverLimitNow(item.songs)) return;


        Utils.askDelete(PlaylistActivity.this, sync ? getString(R.string.sync_playlist) : getString(R.string.dont_sync_playlist), sync ? getString(R.string.sync_playlist_confirm) : getString(R.string.dont_sync_playlist_confirm), () -> {

            PlaylistRealmHelper.toggleSync(playlistItem.id, sync);
            AppController.toast(PlaylistActivity.this, sync ? getString(R.string.playlist_synced) : getString(R.string.playlist_not_synced));

//            if (sync) {
//                FirebaseManager.Instance().uploadPlaylist(playlistItem.hash);
//            } else {
//                FirebaseManager.Instance().deletePlaylist(item);
//            }
        });
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0)
                actionMode.setTitle("");
            else
                actionMode.setTitle(String.format("%d playlist%s", count, count != 1 ? "s" : ""));
        }
    }

    private void deletePlaylists(final ArrayList<QueueItem> queueItemList) {


        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Delete Playlists").content("Do you really want to delete these playlists?").positiveText("YES").onPositive((dialog, which) -> {

            for (QueueItem queueItem : queueItemList) {
                PlaylistItem item = PlaylistItem.copyQueue(queueItem);
                PlaylistDelete taskAdd = new PlaylistDelete(PlaylistActivity.this, item.id);
                taskAdd.execute();
                playList.remove(queueItem);
            }
            adapter.notifyDataSetChanged();
        }).negativeText("NO").show();
    }

    private void delete(final int position, QueueItem queue, boolean isSync) {

        PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(queue.id);

        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Delete Playlist").content("Do you really want to delete this playlist").positiveText("YES").onPositive((dialog, which) -> {

            PlaylistDelete taskAdd = new PlaylistDelete(PlaylistActivity.this, queue.id);
            taskAdd.execute();
            adapter.removeIndex(position);

            if (isSync) {
                FirebaseManager.Instance().deletePlaylist(playlistItem);
            }
        }).negativeText("NO").show();
    }

    private void share(QueueItem queue) {

        ArrayList<QueueItem> queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, queue.id);

        StringBuilder builder = new StringBuilder();
        builder.append("Playlist name: ").append(queue.title).append("\n");
        builder.append("\n");
        for (int i = 0; i < queueItems.size(); i++) {

            QueueItem queueItem = queueItems.get(i);
            builder.append(i).append(" - ").append(queueItem.title).append("\n");
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    private void clonePlaylist(QueueItem queue) {

        final ArrayList<QueueItem> queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, queue.id);

        MaterialDialog createDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).title("Clone Playlist").positiveText("CLONE").negativeText("CANCEL")

                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", "", (dialog, input) -> {
                    // Do something
                    if (input.toString().length() > 0) {

                        PlaylistItem item = new PlaylistItem();
                        item.id = Utils.randLong();
                        item.title = input.toString();
                        item.hash = Utils.generateSha1Hash(item.title + System.currentTimeMillis());
                        long playlistid = PlaylistRealmHelper.insert(item);
                        if (playlistid > 0) {

                            if (queueItems.size() > 0) {
                                ArrayList<PlaylistQueueItem> playlistQueueItems = new ArrayList<>();
                                for (QueueItem queueItem : queueItems) {
                                    PlaylistQueueItem playlistQueueItem = new PlaylistQueueItem();
                                    playlistQueueItem.copyQueue(queueItem);
                                    playlistQueueItem.playlist = playlistid;
                                    playlistQueueItems.add(playlistQueueItem);
                                }

                                PlaylistSongRealmHelper.insertList(playlistQueueItems, false);
                            }
                            reload();
                        } else {
                            AppController.toast(PlaylistActivity.this, "Unable to create playlist!");
                        }
                    } else {
                        AppController.toast(PlaylistActivity.this, "Enter name of playlist!");

                    }
                }).build();

        createDialog.show();
    }

    private void edit(final int position, final QueueItem queue) {
        final PlaylistItem item = PlaylistItem.copyQueue(queue);

        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).title("Update Playlist")
                .positiveText("UPDATE")
                .negativeText("CANCEL")
                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", item.title, (dialog, input) -> {
            if (input.toString().length() > 0) {
                item.title = input.toString();

                if (PlaylistRealmHelper.update(item)) {
                    reload();
                } else {
                    AppController.toast(PlaylistActivity.this, "Unable to rename playlist!");
                }
            } else {
                AppController.toast(PlaylistActivity.this, "Enter name of playlist!");
            }
        }).show();
    }

    private void open(PlaylistItem item) {

        Intent playlistsongIntent = new Intent(PlaylistActivity.this, PlaylistSongsActivity.class);
        playlistsongIntent.putExtra(MyApplication.ARG_ID, item.id);
        playlistsongIntent.putExtra(MyApplication.ARG_TITLE, item.title);
        startActivity(playlistsongIntent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    private void create() {
        MaterialDialog createDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).title("Create Playlist").positiveText("CREATE").negativeText("CANCEL")

                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", "", (dialog, input) -> {
                    // Do something
                    if (input.toString().length() > 0) {

                        PlaylistItem item = new PlaylistItem();
                        item.id = Utils.randLong();
                        item.title = input.toString();
                        item.hash = Utils.generateSha1Hash(item.title + System.currentTimeMillis());
                        if (PlaylistRealmHelper.insert(item) > 0) {
                            reload();
                        } else {
                            AppController.toast(PlaylistActivity.this, "Unable to create playlist!");
                        }
                    } else {
                        AppController.toast(PlaylistActivity.this, "Enter name of playlist!");

                    }
                }).build();

        createDialog.show();
    }


    private void reload() {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        adapter.setShowArtwork(prefShowArtwork);

        ArrayList<PlaylistItem> items = PlaylistRealmHelper.loadAll();

        if (items.size() > 0) {

            playList.clear();
            RecyclerViewUtils.postAndNotifyAdapter(new Handler(Looper.getMainLooper()), recyclerView, adapter);

            for (PlaylistItem item : items) {
                QueueItem queue = new QueueItem();
                queue.copyPlaylist(item);

                playList.add(queue);
                RecyclerViewUtils.postAndNotifyAdapter(new Handler(Looper.getMainLooper()), recyclerView, adapter);
            }
        }
        onFilterValue(PrefsManager.Instance().getPlaylistSort(), PrefsManager.Instance().getPlaylistSortReverse());
        emptied();
    }

    private void emptied() {
        if (adapter.getItemCount() > 0) {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void register() {
        receiver = new PlaylistReceiver();

        IntentFilter playlistfilter = new IntentFilter();
        playlistfilter.addAction(AppController.INTENT_PLAYLIST_CHANGED);
        registerReceiver(receiver, playlistfilter);

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
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
            mainReceiver = null;
        }
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

    private class PlaylistReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    if (action.equals(AppController.INTENT_PLAYLIST_CHANGED)) {
                        reload();
                    }
                }
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
        }
    }


    private class PlaylistItemTouchHelper extends ItemTouchHelper.Callback {
        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

        //and in your imlpementaion of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            if (adapter.moveTo(from, to)) {
                listChanged = true;

                PrefsManager.Instance().setPlaylistSort(customsortId);
                menu_sort_custom.setChecked(true);
            }

            handler.postDelayed(() -> {

                ArrayList<QueueItem> queueItemList;
                queueItemList = adapter.getList();
                for (int i = 0; i < queueItemList.size(); i++) {
                    QueueItem queueItem = queueItemList.get(i);
                    PlaylistItem item = PlaylistItem.copyQueue(queueItem);
                    PlaylistRealmHelper.updateOrder(item, i);
                }

            }, 300);
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
                }
            }
        }
    }
}
