package com.muziko.fragments.Listening;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.activities.PlayerListActivity;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.adapter.TrackAdapter;
import com.muziko.callbacks.PlayerCallback;
import com.muziko.cloud.Box.BoxApi;
import com.muziko.cloud.Drive.DriveApi;
import com.muziko.cloud.DropBox.DropBoxApi;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.DownloadFile;
import com.muziko.dialogs.PlayFrom;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.dialogs.ShareRingtone;
import com.muziko.dialogs.UploadFile;
import com.muziko.fragments.BaseFragment;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.TrackRecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.LibraryEdit;
import com.muziko.tasks.TrackDelete;

import java.util.ArrayList;

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

public class TracksFragment extends BaseFragment implements PlayerCallback, TrackRecyclerItemListener, ActionMode.Callback, MaterialMenuAdapter.Callback {
    private final String TAG = TracksFragment.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final ArrayList<QueueItem> trackList = new ArrayList<>();
    private TracksReceiver receiver;
    private boolean isFaving = false;
    private TrackAdapter adapter = null;
    private NpaLinearLayoutManager layoutList;
    private NpaGridLayoutManager layoutGrid2;
    private NpaGridLayoutManager layoutGrid3;
    private NpaGridLayoutManager layoutGrid4;
    private ActionMode actionMode = null;
    private FastScrollRecyclerView recyclerView;
    private ArrayList<QueueItem> selectedItems = new ArrayList<>();
    private QueueItem selectedItem;
    private int selectedPosition;
    private int playType;
    private String playData;
    private boolean mode;
    private int grid;
    private boolean isResumed = false;
    private int ignoreNextUpdate = 0;
    private BoxApi boxApi;
    private DropBoxApi dropBoxApi;
    private DriveApi driveApi;

    private MaterialMenuAdapter.Callback onSubMenuObjectItemSelected =
            (dialog, index, item) -> handler.postDelayed(
                    () -> {
                        switch (index) {
                            case 0: //send

                                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                    if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                                        if (networkState == NetworkInfo.State.CONNECTED) {
                                            Intent registerIntent = new Intent(getActivity(), RegisterActivity.class);
                                            startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                        } else {
                                            AppController.toast(getActivity(), getString(R.string.no_internet_for_register));
                                        }

                                    } else {
                                        AppController.Instance().sendTracks(getActivity(), selectedItems);
                                    }
                                } else {
                                    if (networkState == NetworkInfo.State.CONNECTED) {
                                        Intent registerIntent = new Intent(getActivity(), RegisterActivity.class);
                                        startActivityForResult(registerIntent, MuzikoConstants.REQUEST_REGISTER_USER_TRACKS);
                                    } else {
                                        AppController.toast(getActivity(), getString(R.string.no_internet_for_register));
                                    }
                                }

                                break;

                            case 1: //send wifi

                                AppController.Instance().sendTracksWifi(getActivity(), selectedItems);

                                break;
                        }

                        dialog.dismiss();
                    },
                    getResources().getInteger(R.integer.ripple_duration_delay));
    private boolean isAddingToLibrary;
    private boolean dragging;

    public TracksFragment() {
        // Required empty public constructor
    }

    public static TracksFragment newInstance() {
        return new TracksFragment();
    }

    @DebugLog
    private void loadWrapper() {
        if (mode) {
            loadTracksForPlayerList(getActivity());
        } else {
            loadTracks(getActivity());
        }
    }

    @DebugLog
    private void loadTracks(Context context) {

        trackList.clear();
        trackList.addAll(TrackRealmHelper.getTracks(PrefsManager.Instance().getStorageViewType()).values());
        adapter.notifyDataSetChanged();
        adapter.setStorage(PrefsManager.Instance().getStorageViewType());

        onFilterValue(PrefsManager.Instance().getTrackSort(), PrefsManager.Instance().getTrackSortReverse());
    }

    @DebugLog
    private void loadTracksForPlayerList(Context context) {

        switch (playType) {
            case PlayerConstants.QUEUE_TYPE_ALBUMS:
                trackList.clear();
                trackList.addAll(TrackRealmHelper.getTracksForAlbum(playData));
                break;
            case PlayerConstants.QUEUE_TYPE_ARTISTS:
                trackList.clear();
                trackList.addAll(TrackRealmHelper.getTracksForArtist(playData));

                break;
            case PlayerConstants.QUEUE_TYPE_GENRES:
                trackList.clear();
                trackList.addAll(TrackRealmHelper.getTracksForGenre(playData));

                break;
        }
        onFilterValue(PrefsManager.Instance().getTrackSort(), PrefsManager.Instance().getTrackSortReverse());
    }

    @DebugLog
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MuzikoConstants.REQUEST_REGISTER_USER_TRACKS:
                if (resultCode == Activity.RESULT_OK) {

                    AppController.Instance().sendTracks(getActivity(), selectedItems);
                }

                break;
            case CODE_WRITE_SETTINGS_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(getActivity())) {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(getActivity(), selectedItem);
                    } else {
                        AppController.toast(getActivity(), getString(R.string.ringtone_permissions));
                    }
                } else {

                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(getActivity(), selectedItem);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @DebugLog
    @Override
    public void onAttach(Context context) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mode = true;
            ((PlayerListActivity) getActivity()).callbackTrack = this;
        } else {
            ((MainActivity) getActivity()).callbackTrack = this;
        }
        super.onAttach(context);
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();

//        if ( trackList.size() == 0)
        {
            isResumed = true;
            onReload();
        }
    }

    @DebugLog
    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        unregister();
        super.onDestroy();
    }

    @DebugLog
    @Override
    public void onDetach() {
        if (mode) {
            ((PlayerListActivity) getActivity()).callbackTrack = null;
        } else {
            ((MainActivity) getActivity()).callbackTrack = null;
        }
        unregister();
        super.onDetach();
    }

    @DebugLog
    @Override
    public void onListingChanged() {

        if (mode) {
            grid = PrefsManager.Instance().getPlayerListTracksViewType();
        } else {
            grid = PrefsManager.Instance().getTracksViewType();
        }
        if (adapter.getGridtype() != grid) {
            adapter.setGridtype(grid);
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
            adapter.notifyDataSetChanged();
        }
    }

    @DebugLog
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.context_default, menu);

        return true;
    }

    @DebugLog
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        if (this.mode) {
            ((PlayerListActivity) getActivity()).enableTabs(false);
        } else {
            ((MainActivity) getActivity()).enableTabs(false);
        }

        return false;
    }

    @DebugLog
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<QueueItem> list = adapter.getSelectedItems();
        if (list.size() > 0) {

            switch (item.getItemId()) {
                case R.id.play:
                    AppController.Instance().clearAddToQueue(getActivity(), list);
                    break;

                case R.id.share:
                    AppController.Instance().shareSongs(list);
                    break;

                case R.id.send:
                    selectedItems = list;

                    final ArrayList<MenuObject> subMenuItems = new ArrayList<>();
                    subMenuItems.add(new MenuObject(MenuObject.SEND_CONTACTS));
                    subMenuItems.add(new MenuObject(MenuObject.SEND_WIFI));

                    MaterialMenuAdapter MaterialMenuAdapter =
                            new MaterialMenuAdapter(subMenuItems, onSubMenuObjectItemSelected);

                    new MaterialDialog.Builder(getActivity())
                            .adapter(MaterialMenuAdapter, new LinearLayoutManager(getActivity()))
                            .show();

                    break;

                case R.id.add_to_queue:
                    AppController.Instance().addToQueue(getActivity(), list, false);
                    break;

                case R.id.play_next:
                    AppController.Instance().addToQueue(getActivity(), list, true);
                    break;

                case R.id.add_to_playlist:
                    AppController.Instance().addToPlaylist(getActivity(), list, false);
                    break;

                case R.id.delete:
                    deleteItems(list);
                    break;

                case R.id.multi_tag_edit:
                    AppController.Instance().multiTagEdit(getActivity(), list);
                    break;

                case R.id.trash:
                    movetoNegative(list);

                    break;

                case R.id.favourite:
                    favorite(list);
                    break;

                default:
                    return false;
            }
        }

        mode.finish();
        actionMode = null;
        return true;
    }

    @DebugLog
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (this.mode) {
            ((PlayerListActivity) getActivity()).enableTabs(true);
        } else {
            ((MainActivity) getActivity()).enableTabs(true);
        }
        handler.post(() -> {
            if (!recyclerView.isComputingLayout()) {
                ((SelectableAdapter) recyclerView.getAdapter()).setMultiSelect(false);
                actionMode = null;
            } else {
                onDestroyActionMode(this.actionMode);
            }
        });
    }

    @DebugLog
    @Override
    public void onMenuClicked(Context context, final int position) {
        final QueueItem item = adapter.getItem(position);
        if (item == null) return;

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
        if (mode) {
            switch (playType) {
                case PlayerConstants.QUEUE_TYPE_ALBUMS:
                    items.add(new MenuObject(GO_TO_ARTIST));
                    break;
                case PlayerConstants.QUEUE_TYPE_ARTISTS:
                    items.add(new MenuObject(GO_TO_ALBUM));
                    break;
                case PlayerConstants.QUEUE_TYPE_GENRES:
                    items.add(new MenuObject(GO_TO_ALBUM));
                    items.add(new MenuObject(GO_TO_ARTIST));
                    break;
            }
        } else {
            items.add(new MenuObject(GO_TO_ALBUM));
            items.add(new MenuObject(GO_TO_ARTIST));
        }
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

        new MaterialDialog.Builder(getActivity())
                .adapter(materialMenuAdapter, new LinearLayoutManager(getActivity()))
                .show();
    }

    @DebugLog
    @Override
    public void onItemClicked(int position) {

        QueueItem queueItem = adapter.getItem(position);

        if (queueItem.storage == CloudManager.FIREBASE && FirebaseManager.Instance().isOverLimitNow(0))
            return;

        if (adapter.isMultiSelect()) {
            toggleSelection(position);
        } else {
            handler.postDelayed(
                    () -> {
                        AsyncJob.doInBackground(
                                () -> {
                                    AppController.Instance()
                                            .play(PlayerConstants.QUEUE_TYPE_TRACKS, position, adapter.getList());
                                },
                                ThreadManager.Instance().getMuzikoBackgroundThreadPool());
                    },
                    getResources().getInteger(R.integer.ripple_duration_delay));
        }
    }

    @DebugLog
    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.adapter.isMultiSelect()) {
            if (mode) {
                ((PlayerListActivity) getActivity()).startSupportActionMode(this);
            } else {
                ((MainActivity) getActivity()).startSupportActionMode(this);
            }

            this.adapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    @DebugLog
    @Override
    public void onFilterValue(int value, boolean reverse) {
        PlayerConstants.QUEUE_TYPE = 0;
        adapter.setSortid(value);
        switch (value) {
            case R.id.player_sort_tracks:
                if (!reverse) {
                    adapter.sortTrackLowest();
                } else {
                    adapter.sortTrackHighest();
                }
                break;
            case R.id.player_sort_tracktitle:
                if (!reverse) {
                    adapter.sortTitleLowest();
                } else {
                    adapter.sortTitleHighest();
                }
                break;
            case R.id.player_sort_filename:
                if (!reverse) {
                    adapter.sortFilenameLowest();
                } else {
                    adapter.sortFilenameHighest();
                }
                break;
            case R.id.player_sort_album:
                if (!reverse) {
                    adapter.sortAlbumLowest();
                } else {
                    adapter.sortAlbumHighest();
                }
                break;
            case R.id.player_sort_artist:
                if (!reverse) {
                    adapter.sortArtistLowest();
                } else {
                    adapter.sortAlbumHighest();
                }
                break;
            case R.id.player_sort_trackduration:
                if (!reverse) {
                    adapter.sortDurationSmallest();
                } else {
                    adapter.sortDurationLargest();
                }
                break;
            case R.id.player_sort_year:
                if (!reverse) {
                    adapter.sortYearEarliest();
                } else {
                    adapter.sortYearLatest();
                }
                break;
            case R.id.player_sort_trackdate:
                if (!reverse) {
                    adapter.sortDateEarliest();
                } else {
                    adapter.sortDateLatest();
                }
                break;

            case R.id.player_sort_rating:
                if (!reverse) {
                    adapter.sortRatingLowest();
                } else {
                    adapter.sortRatingHighest();
                }
                break;
        }
    }

    @DebugLog
    @Override
    public void onSearchQuery(String chars) {
        adapter.search(chars);
    }

    @DebugLog
    @Override
    public void onReload() {

        loadWrapper();
    }

    @Override
    public void onQueueChanged() {
        handler.postDelayed(
                () -> {
                    adapter.queueChanged();
                },
                500);

    }

    @DebugLog
    @Override
    public void onStorageChanged() {

        loadWrapper();
    }

    @DebugLog
    @Override
    public void onLayoutChanged(float bottomMargin) {

        if (recyclerView != null) {
            Resources resources = getActivity().getResources();
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
            recyclerView.requestLayout();
        }
    }

    @DebugLog
    @Override
    public void onDownloadProgress(String url, int progress) {
        if (adapter != null) {
            adapter.downloadProgress(url, progress);
        }
    }

    @Override
    public void onTrackAdded(String data) {
        QueueItem queueItem = TrackRealmHelper.getTrack(data);
        if (queueItem != null) {
            adapter.addItem(queueItem);
        }
    }

    @Override
    public void onNowPlaying() {
        if (adapter != null) {
            adapter.updateNowPlaying();
        }
    }

    @Override
    public void onFirebaseLimitChanged() {
        if (adapter != null) {
            if (adapter.isFirebaseOverlimit() != FirebaseManager.Instance().isOverLimit()) {
                adapter.updateFirebaseOverlimit(FirebaseManager.Instance().isOverLimit());
            }
        }
    }

    @DebugLog
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(getActivity(), selectedItem);
                } else {
                    AppController.toast(
                            getActivity(),
                            "Write settings permission wasn't provided. Muziko can't set default ringtone");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @DebugLog
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_tracks, container, false);

        isFaving = false;

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mode = true;
            playType = bundle.getInt(MyApplication.ARG_TYPE, 0);
            playData = bundle.getString(MyApplication.ARG_DATA);
        }

        adapter =
                new TrackAdapter(getActivity(), trackList, PlayerConstants.QUEUE_TYPE_TRACKS, TAG, this);
        adapter.setSortid(PrefsManager.Instance().getTrackSort());
        adapter.setStorage(PrefsManager.Instance().getStorageViewType());

        layoutList = new NpaLinearLayoutManager(getActivity());
        layoutGrid2 = new NpaGridLayoutManager(getActivity(), 2);
        layoutGrid3 = new NpaGridLayoutManager(getActivity(), 3);
        layoutGrid4 = new NpaGridLayoutManager(getActivity(), 4);

        recyclerView = rootView.findViewById(R.id.itemList);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(false);
        if (mode) {
            grid = PrefsManager.Instance().getPlayerListTracksViewType();
        } else {
            grid = PrefsManager.Instance().getTracksViewType();
        }
        adapter.setGridtype(grid);
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
        register();
        return rootView;
    }

    @DebugLog
    private void favorite(final ArrayList<QueueItem> queueItems) {

        for (int i = 0; i < queueItems.size(); i++) {
            QueueItem queueItem = queueItems.get(i);
            TrackRealmHelper.addFavorite(queueItem.data);
        }

        AppController.toast(getActivity(), "Songs added to Favorites");
        getActivity().sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
        getActivity().sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
        getActivity().sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
    }

    @DebugLog
    private void favorite(final int pos, final QueueItem queue) {
        if (isFaving) return;
        isFaving = true;

        FavoriteEdit fe =
                new FavoriteEdit(
                        getActivity(),
                        PlayerConstants.QUEUE_TYPE_TRACKS,
                        s -> {
                            isFaving = false;
                            adapter.notifyItemChanged(pos);
                        });
        fe.execute(queue);
    }


    @DebugLog
    private void toggleLibrary(final int pos, final QueueItem queue) {
        if (queue.isLibrary()) {
            LibraryEdit libraryEdit = new LibraryEdit(getActivity(), true, new LibraryEdit.LibraryEditListener() {
                @Override
                public void onLibraryEdited(boolean s) {
                    adapter.notifyDataSetChanged();
                }
            });
            libraryEdit.execute(queue);
        } else {
            if (!FirebaseManager.Instance().isOverLimitNow(1)) {
                LibraryEdit libraryEdit = new LibraryEdit(getActivity(), new LibraryEdit.LibraryEditListener() {
                    @Override
                    public void onLibraryEdited(boolean s) {
                        adapter.notifyItemChanged(pos);
                    }
                });
                libraryEdit.execute(queue);
            }
        }
    }

    private void toggleSync(final QueueItem queue, boolean sync) {

        Utils.askDelete(getActivity(), sync ? getString(R.string.sync_fav) : getString(R.string.dont_sync_fav), sync ? getString(R.string.sync_fav_confirm) : getString(R.string.dont_sync_fav_confirm), () -> {
            TrackRealmHelper.toggleSync(queue, sync);
            AppController.toast(getActivity(), sync ? getString(R.string.song_synced) : getString(R.string.song_not_synced));
            if (sync) {
                handler.postDelayed(() -> {
                    FirebaseManager.Instance().checkforFavsTransfers();
                }, UPLOAD_SETTLE_DELAY);
            } else {
                FirebaseManager.Instance().deleteFav(queue);
            }
        });
    }


    @DebugLog
    private void movetoNegative(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(getActivity(), "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {
            for (int i = 0; i < queueItems.size(); i++) {
                QueueItem queueItem = queueItems.get(i);
                TrackRealmHelper.movetoNegative(queueItem);
            }
            onReload();
        });
    }

    @DebugLog
    private void movetoIgnore(final int position, final QueueItem queue) {
        Utils.askDelete(
                getActivity(),
                "Move to Ignore Folder",
                "You can later restore it from ignore folder, are you sure you want to ignore it?",
                () -> {
                    TrackRealmHelper.movetoNegative(queue);
                    adapter.removeIndex(position);
                });
    }

    @DebugLog
    private void delete(final int position, final QueueItem queue) {
        DeleteTrackRunnable deleteTrackRunnable = new DeleteTrackRunnable(position, queue);
        if (queue.storage == 2) {
            checkStoragePermissions(
                    "Do you want to grant SD card permissions so tracks can be deleted?",
                    deleteTrackRunnable);
        } else {
            handler.post(deleteTrackRunnable);
        }
    }

    @DebugLog
    private void deleteItems(final ArrayList<QueueItem> queueItems) {

        DeleteTracksRunnable deleteTracksRunnable = new DeleteTracksRunnable(queueItems);
        checkStoragePermissions(
                "Do you want to grant SD card permissions so tracks can be deleted?", deleteTracksRunnable);
    }

    @DebugLog
    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0) actionMode.setTitle("");
            else actionMode.setTitle(String.format("%d song%s", count, count != 1 ? "s" : ""));
        }
    }

    @DebugLog
    private void register() {
        receiver = new TracksReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_TRACK_DELETED);
        filter.addAction(AppController.INTENT_TRACK_CHANGED);
        filter.addAction(AppController.INTENT_TRACK_EDITED);
        getActivity().registerReceiver(receiver, filter);
    }

    @DebugLog
    private void unregister() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @DebugLog
    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(
                () -> {
                    switch (item.id) {
                        case ADD_TO_QUEUE: //add to q
                            PlayerConstants.QUEUE_TYPE = 0;
                            AppController.Instance().addToQueue(getActivity(), selectedItem, false);
                            break;

                        case ADD_TO_PLAYLIST: //add to p
                            AppController.Instance().addToPlaylist(getActivity(), selectedItem);
                            break;

                        case FAV: //add to f`
                            favorite(selectedPosition, selectedItem);
                            break;

                        case PLAY_NEXT: //play next
                            AppController.Instance().addToQueue(getActivity(), selectedItem, true);
                            break;

                        case SET_START_TIME: //play from
                            PlayFrom playFrom = new PlayFrom();
                            playFrom.open(getActivity(), selectedItem);
                            break;

                        case GO_TO_ARTIST: //goto ar
                            AppController.Instance().gotoArtist(getActivity(), selectedItem, null);
                            break;

                        case GO_TO_ALBUM: //goto al
                            AppController.Instance().gotoAlbum(getActivity(), selectedItem, null);
                            break;

                        case SEND:
                            final ArrayList<MenuObject> subMenuItems = new ArrayList<>();
                            subMenuItems.add(new MenuObject(MenuObject.SEND_CONTACTS));
                            subMenuItems.add(new MenuObject(MenuObject.SEND_WIFI));

                            MaterialMenuAdapter MaterialMenuAdapter =
                                    new MaterialMenuAdapter(subMenuItems, onSubMenuObjectItemSelected);

                            new MaterialDialog.Builder(getActivity())
                                    .adapter(MaterialMenuAdapter, new LinearLayoutManager(getActivity()))
                                    .show();

                            break;

                        case SET_RINGTONE: //createRingtone
                            boolean permission;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                permission = Settings.System.canWrite(getActivity());
                            } else {
                                permission =
                                        ContextCompat.checkSelfPermission(
                                                getActivity(), Manifest.permission.WRITE_SETTINGS)
                                                == PackageManager.PERMISSION_GRANTED;
                            }
                            if (!permission) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    new MaterialDialog.Builder(getActivity())
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
                                                                    Uri.parse("package:" + getActivity().getPackageName()));
                                                            startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                                                        } else {
                                                            ActivityCompat.requestPermissions(
                                                                    getActivity(),
                                                                    new String[]{Manifest.permission.WRITE_SETTINGS},
                                                                    CODE_WRITE_SETTINGS_PERMISSION);
                                                        }
                                                    })
                                            .negativeText("Cancel")
                                            .show();

                                } else {
                                    SetRingtone createRingtone = new SetRingtone();
                                    createRingtone.open(getActivity(), selectedItem);
                                }
                            } else {
                                SetRingtone createRingtone = new SetRingtone();
                                createRingtone.open(getActivity(), selectedItem);
                            }
                            break;

                        case SEND_AUDIO_CLIP: //share ringtone
                            ShareRingtone shareRingtone = new ShareRingtone();
                            shareRingtone.open(getActivity(), selectedItem);
                            break;

                        case CUT: //cut
                            AppController.Instance().cutSong(selectedItem);
                            break;

                        case PREVIEW_SONG: //preview
                            PreviewSong previewSong = new PreviewSong();
                            previewSong.open(getActivity(), selectedItem);
                            break;

                        case EDIT_TAGS: //edit
                            AppController.Instance().editSong(getActivity(), TAG, selectedPosition, selectedItem);
                            break;

                        case DETAILS: //details
                            AppController.Instance().details(getActivity(), selectedItem);
                            break;

                        case SHARE_ITEM: //share
                            AppController.Instance().shareSong(getActivity(), selectedItem);
                            break;

                        case PLAY_X_TIMES: //remove
                            AppController.Instance().removeAfter(getActivity(), selectedItem);
                            break;

                        case MOVE_TO_IGNORE: //negative
                            movetoIgnore(selectedPosition, selectedItem);
                            break;

                        case DELETE_ITEM: //remove
                            delete(selectedPosition, selectedItem);

                            break;

                        case DOWNLOAD:
                            if (networkState != NetworkInfo.State.CONNECTED) {
                                AppController.toast(getActivity(), getString(R.string.no_network_connection));
                                return;
                            }
                            if (FileHelper.fileExists(selectedItem)) {
                                AppController.toast(getActivity(), getString(R.string.file_exists));
                                return;
                            }

                            DownloadFile downloadFile = new DownloadFile();
                            downloadFile.init(getActivity(), selectedItem);

                            break;

                        case UPLOAD:
                            if (networkState != NetworkInfo.State.CONNECTED) {
                                AppController.toast(getActivity(), getString(R.string.no_network_connection));
                                return;
                            }
                            UploadFile uploadFile = new UploadFile();
                            uploadFile.load(getActivity(), selectedItem);
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
    private class DeleteTrackRunnable implements Runnable {

        private int position;
        private QueueItem queueItem;

        public DeleteTrackRunnable(int position, QueueItem queueItem) {
            this.position = position;
            this.queueItem = queueItem;
        }

        @Override
        public void run() {
            ignoreNextUpdate = 2;
            Utils.askDelete(getActivity(), "Delete Song", "This will delete song permanently from this device, do you want to proceed ?", () -> {
                TrackDelete tr = new TrackDelete(getActivity(), PlayerConstants.QUEUE_TYPE_TRACKS, () -> {
                    adapter.removeIndex(position);
                });
                tr.execute(queueItem);
            });
        }
    }

    @DebugLog
    private class DeleteTracksRunnable implements Runnable {

        private ArrayList<QueueItem> queueItems;

        public DeleteTracksRunnable(ArrayList<QueueItem> queueItems) {
            this.queueItems = queueItems;
        }

        @Override
        public void run() {
            ignoreNextUpdate = 1 + queueItems.size();
            Utils.askDelete(
                    getActivity(),
                    "Delete Songs",
                    String.format(
                            "This will delete song%s permanently from this device, do you want to proceed ?",
                            queueItems.size() != 1 ? "s" : ""),
                    () -> {
                        TrackDelete tr = new TrackDelete(getActivity(), PlayerConstants.QUEUE_TYPE_TRACKS, () -> {
                            adapter.removeAll(queueItems);
                        });
                        tr.execute(queueItems);
                    });
        }
    }

    @DebugLog
    private class TracksReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    switch (action) {
                        case AppController.INTENT_TRACK_DELETED:
                            // track delete handled in line
                            //                            loadWrapper();
                            break;
                        case AppController.INTENT_TRACK_CHANGED:
                            if (ignoreNextUpdate > 0) {
                                ignoreNextUpdate--;
                            }
                            loadWrapper();
                            break;
                        case AppController.INTENT_TRACK_EDITED:
                            int index = intent.getIntExtra("index", -1);
                            String tag = intent.getStringExtra("tag");
                            QueueItem item = (QueueItem) intent.getSerializableExtra("item");
                            if (item != null
                                    && tag != null
                                    && tag.equals(TAG)
                                    && index >= 0
                                    && index < adapter.getItemCount()) {
                                adapter.put(index, item);
                            } else {
                                loadWrapper();
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
