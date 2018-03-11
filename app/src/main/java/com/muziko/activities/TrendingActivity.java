package com.muziko.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
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
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.muziko.R;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.MostPlayedAdapter;
import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.events.RefreshMostPlayedEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.MostPlayed;
import com.muziko.controls.AdMobBanner;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.controls.MiniPlayer;
import com.muziko.database.ACRTrackRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.StorageUtils;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.MostPlayedItemListener;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.TrackDelete;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;

public class TrendingActivity extends BaseActivity implements SearchView.OnQueryTextListener, MaterialMenuAdapter.Callback, MostPlayedItemListener, AdMobBanner.OnAdLoadedListener {
    private final WeakHandler handler = new WeakHandler();
    private final String TAG = FavouritesActivity.class.getName();
    private final ActionMode actionMode = null;
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private MenuItem menuItemView;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Toolbar toolbar;
    private MiniPlayer miniPlayer;
    private MainReceiver mainReceiver;
    private FastScrollRecyclerView recyclerView;
    private CoordinatorLayout coordinatorlayout;
    private RelativeLayout emptyLayout;
    private RelativeLayout progressLayout;
    private MenuItem menuItemSearch;
    private MenuItem menuItemgridone;
    private MenuItem menuItemgridtwo;
    private MenuItem menuItemgridthree;
    private MenuItem menuItemgridfour;
    private ArrayList<TrackModel> trackList;
    private MostPlayedReceiver receiver;
    private MostPlayedAdapter adapter;
    private ValueEventListener mostPlayedListener;
    private DatabaseReference mostPlayedRef;
    private NpaLinearLayoutManager layoutList;
    private NpaGridLayoutManager layoutGrid2;
    private NpaGridLayoutManager layoutGrid3;
    private NpaGridLayoutManager layoutGrid4;
    private ArrayList<QueueItem> selectedItems = new ArrayList<>();
    private MaterialDialog materialDialog;
    private MaterialMenuAdapter.Callback onSubMenuObjectItemSelected = new MaterialMenuAdapter.Callback() {
        @Override
        public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

            handler.postDelayed(() -> {

                switch (index) {
                    case 0:     //send

                        selectedItems.clear();
                        selectedItems.add(selectedItem);

                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                                if (networkState == NetworkInfo.State.CONNECTED) {
                                    Intent registerIntent = new Intent(TrendingActivity.this, RegisterActivity.class);
                                    startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                } else {
                                    AppController.toast(TrendingActivity.this, getString(R.string.no_internet_for_register));
                                }

                            } else {
                                AppController.Instance().sendTracks(TrendingActivity.this, selectedItems);
                            }
                        } else {
                            if (networkState == NetworkInfo.State.CONNECTED) {
                                Intent registerIntent = new Intent(TrendingActivity.this, RegisterActivity.class);
                                startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                            } else {
                                AppController.toast(TrendingActivity.this, getString(R.string.no_internet_for_register));
                            }
                        }

                        break;

                    case 1:     //send wifi

                        selectedItems.clear();
                        selectedItems.add(selectedItem);

                        AppController.Instance().sendTracksWifi(TrendingActivity.this, selectedItems);

                        break;
                }

                dialog.dismiss();

            }, getResources().getInteger(R.integer.ripple_duration_delay));
        }
    };
    private RelativeLayout admobLayout;
    private AdMobBanner adMobControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Trending Songs");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        layoutList = new NpaLinearLayoutManager(this);
        layoutGrid2 = new NpaGridLayoutManager(this, 2);
        layoutGrid3 = new NpaGridLayoutManager(this, 3);
        layoutGrid4 = new NpaGridLayoutManager(this, 4);

        emptyLayout.setVisibility(View.GONE);
        progressLayout.setVisibility(View.GONE);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        trackList = new ArrayList<>();
        adapter = new MostPlayedAdapter(this, trackList, prefShowArtwork, TAG, this);
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

        onListingChanged();
        setupMainPlayer();

        if (AppController.Instance().shouldShowAd()) {
            adMobControl = new AdMobBanner(admobLayout, this);
        } else {
            admobLayout.setVisibility(View.GONE);
        }

        EventBus.getDefault().register(this);

        handler.postDelayed(() -> {
            getMostPlayed();
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
        register();
        mainUpdate();

    }

    @Override
    public void onDestroy() {
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (miniPlayer != null) miniPlayer.onDestroy();
        unregister();
        EventBus.getDefault().unregister(this);
        if (mostPlayedRef != null && mostPlayedListener != null) {
            mostPlayedRef.removeEventListener(mostPlayedListener);
        }
        super.onDestroy();
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case MuzikoConstants.REQUEST_REGISTER_USER_TRACKS:
                if (resultCode == Activity.RESULT_OK) {

                    AppController.Instance().sendTracks(this, selectedItems);
                }

                break;
            case CODE_WRITE_SETTINGS_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(this)) {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(this, selectedItem);
                    } else {
                        AppController.toast(this, getString(R.string.ringtone_permissions));
                    }
                } else {

                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(this, selectedItem);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
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

        unregister();
        if (miniPlayer != null) {
            miniPlayer.pause();
        }

        super.onPause();
    }

    private void getMostPlayed() {

        if (trackList.size() == 0) {
            progressLayout.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        mostPlayedRef = FirebaseManager.Instance().getMostPlayedRef();


        mostPlayedListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                AsyncJob.doInBackground(() -> {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        MostPlayed mostPlayed = postSnapshot.getValue(MostPlayed.class);
                        ACRTrackRealmHelper.insertTrack(mostPlayed);
                    }
                    AsyncJob.doOnMainThread(() -> loadMostPlayed());
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        };

        mostPlayedRef.orderByChild("played").limitToLast(100).addListenerForSingleValueEvent(mostPlayedListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshMostPlayedEvent(RefreshMostPlayedEvent event) {

        handler.postDelayed(() -> loadMostPlayed(), event.delay);
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

    private void findViewsById() {

        coordinatorlayout = findViewById(R.id.coordinatorlayout);
        emptyLayout = findViewById(R.id.emptyLayout);
        progressLayout = findViewById(R.id.progressLayout);
        recyclerView = findViewById(R.id.itemList);
        admobLayout = findViewById(R.id.admobLayout);
    }

    private void setupMainPlayer() {
        RelativeLayout contentlayout = findViewById(R.id.contentlayout);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        final LinearLayout mainPlayerLayout = findViewById(R.id.mainPlayerLayout);

        miniPlayer = new MiniPlayer(this, mainPlayerLayout, slidingUpPanelLayout, contentlayout);
    }

    private void updateGridMenu() {

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

    private void loadMostPlayed() {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        adapter.setShowArtwork(prefShowArtwork);

        trackList.clear();
        trackList.addAll(ACRTrackRealmHelper.getACRTracks());
        adapter.notifyDataSetChanged();
        adapter.setStorage(0);
        emptied();
    }

    private void mainUpdate() {
        miniPlayer.updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.mostplayed_menu, menu);
        menuItemSearch = menu.findItem(R.id.mostplayed_search);

        menuItemgridone = menu.findItem(R.id.grid_one);
        menuItemgridtwo = menu.findItem(R.id.grid_two);
        menuItemgridthree = menu.findItem(R.id.grid_three);
        menuItemgridfour = menu.findItem(R.id.grid_four);

        updateGridMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

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


            case R.id.mostplayed_search:

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

            case R.id.mostplayed_mediascan:
                AppController.Instance().scanMedia(this, coordinatorlayout);
                return true;

            case R.id.mostplayed_share:
                AppController.Instance().shareApp();
                return true;
            case R.id.sharing_wifi:
                Intent shareIntent =
                        new Intent(TrendingActivity.this, ShareWifiActivity.class);
                startActivity(shareIntent);
                return true;
            case R.id.mostplayed_exit:
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
    public void onMenuClicked(final int position) {
        QueueItem item = TrackRealmHelper.getTrack(trackList.get(position).getData());

        if (item == null) return;

        selectedItem = item;
        selectedItemPosition = position;

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(MenuObject.ADD_TO_QUEUE));
        items.add(new MenuObject(MenuObject.ADD_TO_PLAYLIST));
        items.add(new MenuObject(MenuObject.PLAY_NEXT));
        items.add(new MenuObject(MenuObject.GO_TO_ARTIST));
        items.add(new MenuObject(MenuObject.GO_TO_ALBUM));
        items.add(new MenuObject(MenuObject.SEND));
        items.add(new MenuObject(MenuObject.SET_RINGTONE));
        items.add(new MenuObject(MenuObject.CUT));
        items.add(new MenuObject(MenuObject.PREVIEW_SONG));
        items.add(new MenuObject(MenuObject.EDIT_TAGS));
        items.add(new MenuObject(MenuObject.DETAILS));
        items.add(new MenuObject(MenuObject.PLAY_X_TIMES));
        items.add(new MenuObject(MenuObject.MOVE_TO_IGNORE));
        items.add(new MenuObject(MenuObject.DELETE_ITEM));


        MaterialMenuAdapter MaterialMenuAdapter = new MaterialMenuAdapter(items, this);

        materialDialog = new MaterialDialog.Builder(this)
                .adapter(MaterialMenuAdapter, new LinearLayoutManager(this))
                .build();
        materialDialog.show();

    }

    @Override
    public void onYoutubeClicked(int position) {

        TrackModel trackModel = adapter.getItem(position);
        if (TextUtils.isEmpty(trackModel.videoId)) {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", trackModel.getTitle());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, YouTubePlayerActivity.class);
            intent.putExtra("videoId", trackList.get(position).getVideoId());
            startActivity(intent);
        }
    }

    @Override
    public void onGotoClicked(int position) {

        String query = null;
        try {
            query = URLEncoder.encode(trackList.get(position).title + " " + trackList.get(position).artist_name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        }

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        startActivity(intent);

    }

    @Override
    public void onItemClicked(int position) {

        QueueItem queueItem = TrackRealmHelper.getTrack(trackList.get(position).getData());
        if (queueItem != null) {
            AppController.Instance().playCurrentSong(queueItem);
        } else {
            AppController.toast(this, "Song doesn't exist in your library. Try listening on youtube instead");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(TrendingActivity.this, selectedItem);
                } else {
                    AppController.toast(this, "Write settings permission wasn't provided. Muziko can't set default ringtone");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void movetoNegative(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(this, "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

            for (int i = 0; i < queueItems.size(); i++) {
                QueueItem queueItem = queueItems.get(i);
                TrackRealmHelper.movetoNegative(queueItem);
                loadMostPlayed();
            }
            EventBus.getDefault().post(new RefreshEvent(1000));
        });
    }

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(this, "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

            TrackRealmHelper.movetoNegative(queue);
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

    private void deleteItems(final ArrayList<QueueItem> list) {
        Utils.askDelete(TrendingActivity.this, "Delete From Favourite", String.format("Are you sure you want to delete these song%s from your Favourites ?", list.size() != 1 ? "s" : ""), () -> {

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

            AppController.toast(TrendingActivity.this, String.format("Song%s deleted from your Favorites", del.size() != 1 ? "s" : ""));

            del.clear();
        });
    }

    private void reload() {

        loadMostPlayed();
    }

    private void emptied() {
        if (adapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);
        }

    }

    private void register() {
        receiver = new MostPlayedReceiver();

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
    public void onAdLoaded() {
        showAdMob(true);
    }

    @Override
    public void onAdClosed() {
        showAdMob(false);
    }

    private void showAdMob(boolean show) {
        if (show) {
            admobLayout.setVisibility(View.VISIBLE);
        } else {
            admobLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(() -> {
            switch (index) {
                case 0:     //add to q
                    PlayerConstants.QUEUE_TYPE = 0;
                    AppController.Instance().addToQueue(TrendingActivity.this, selectedItem, false);
                    break;

                case 1:     //add to p
                    AppController.Instance().addToPlaylist(TrendingActivity.this, selectedItem);
                    break;

                case 2:     //play next
                    AppController.Instance().addToQueue(TrendingActivity.this, selectedItem, true);
                    break;

                case 3:     //goto ar
                    AppController.Instance().gotoArtist(TrendingActivity.this, selectedItem, null);
                    break;

                case 4:     //goto al
                    AppController.Instance().gotoAlbum(TrendingActivity.this, selectedItem, null);
                    break;

                case 5: //send
                    final ArrayList<MenuObject> subMenuItems = new ArrayList<>();
                    subMenuItems.add(new MenuObject(MenuObject.SEND_CONTACTS));
                    subMenuItems.add(new MenuObject(MenuObject.SEND_WIFI));


                    MaterialMenuAdapter MaterialMenuAdapter = new MaterialMenuAdapter(subMenuItems, onSubMenuObjectItemSelected);

                    materialDialog = new MaterialDialog.Builder(this)
                            .adapter(MaterialMenuAdapter, new LinearLayoutManager(this))
                            .build();
                    materialDialog.show();

                    break;

                case 6:     //createRingtone

                    boolean permission;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permission = Settings.System.canWrite(TrendingActivity.this);
                    } else {
                        permission = ContextCompat.checkSelfPermission(TrendingActivity.this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                    }
                    if (!permission) {

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            materialDialog = new MaterialDialog.Builder(TrendingActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Permission required").content("Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.").positiveText("Ok").onPositive((mdialog, mwhich) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                                } else {
                                    ActivityCompat.requestPermissions(TrendingActivity.this, new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
                                }
                            }).negativeText("Cancel").build();
                            materialDialog.show();

                        } else {
                            SetRingtone createRingtone = new SetRingtone();
                            createRingtone.open(TrendingActivity.this, selectedItem);
                        }
                    } else {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(TrendingActivity.this, selectedItem);
                    }

                    break;

                case 7:     //cut
                    AppController.Instance().cutSong(selectedItem);
                    break;

                case 8:     //preview
                    PreviewSong previewSong = new PreviewSong();
                    previewSong.open(TrendingActivity.this, selectedItem);
                    break;

                case 89:     //edit
                    AppController.Instance().editSong(this, "FavouritesActivity", selectedItemPosition, selectedItem);
                    break;

                case 10:
                    AppController.Instance().details(TrendingActivity.this, selectedItem);
                    break;

                case 11:     //remove
                    AppController.Instance().removeAfter(TrendingActivity.this, selectedItem);
                    break;

                case 12:     //negative
                    movetoNegative(selectedItemPosition, selectedItem);
                    break;

                case 13:     //remove
                    delete(selectedItemPosition, selectedItem);
                    break;
            }

            dialog.dismiss();

        }, 600);


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
            Utils.askDelete(TrendingActivity.this, "Delete Song", "This will delete song permanently from this device, do you want to proceed ?", () -> {
                TrackDelete tr = new TrackDelete(TrendingActivity.this, PlayerConstants.QUEUE_TYPE_TRACKS, () -> adapter.removeIndex(position));

                tr.execute(queueItem);
            });
        }
    }

    private class MostPlayedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    if (action.equals(AppController.INTENT_FAVOURITE_CHANGED)) {
                        reload();
                    }
                }
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
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
                }
            }
        }
    }


}
