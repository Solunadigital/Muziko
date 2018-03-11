package com.muziko.fragments.Recent;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.RecentActivity;
import com.muziko.activities.register.RegisterActivity;
import com.muziko.adapter.GeneralAdapter;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.callbacks.ActivityCallback;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.FastScroller.OnFastScrollStateChangeListener;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.fragments.BaseFragment;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.PicassoScrollListener;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.SettingsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.LibraryEdit;
import com.muziko.tasks.TrackDelete;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import hugo.weaving.DebugLog;

import static android.R.attr.type;
import static com.muziko.MyApplication.networkState;
import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.objects.MenuObject.ADD_TO_LIBRARY;

/**
 * Created by dev on 12/07/2016.
 */
public class RecentFragment extends BaseFragment implements ActivityCallback, RecyclerItemListener, ActionMode.Callback, MaterialMenuAdapter.Callback, OnFastScrollStateChangeListener {

    private final String TAG = RecentFragment.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private FastScrollRecyclerView recyclerView;
    private ArrayList<QueueItem> itemList;
    private ActivitiesReceiver receiver;
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private ActionMode actionMode;
    private GeneralAdapter adapter;
    private Timer updateTimer = null;
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

                    case 1:     //send wifi

                        selectedItems.clear();
                        selectedItems.add(selectedItem);

                        AppController.Instance().sendTracksWifi(getActivity(), selectedItems);

                        break;
                }

                dialog.dismiss();

            }, getResources().getInteger(R.integer.ripple_duration_delay));
        }
    };
    private boolean isAddingToLibrary;

    public RecentFragment() {
        // Required empty public constructor
    }

    private void loadRecentlyPlayed() {

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();
        adapter.setShowArtwork(prefShowArtwork);

        itemList.clear();
        itemList.addAll(TrackRealmHelper.getRecentlyPlayed(MyApplication.MAX_ACTIVITY_ITEMS).values());
        adapter.notifyDataSetChanged();
        adapter.setStorage(0);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NewApi")
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

    @Override
    public void onAttach(Context context) {


        ((RecentActivity) getActivity()).callbackPlayed = this;


        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    @Override
    public void onDestroy() {
        unregister();
        if (updateTimer != null) updateTimer.cancel();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((RecentActivity) getActivity()).callbackPlayed = null;


        unregister();
    }

    @Override
    public void onListingChanged() {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.context_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        ((RecentActivity) getActivity()).enableTabs(false);
        MenuItem menuResetPlayCount = menu.findItem(R.id.reset_most_play_count);
        menuResetPlayCount.setVisible(false);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<QueueItem> list = adapter.getSelectedItems();
        if (list.size() > 0) {
            // Handle presses on the action bar items
            switch (item.getItemId()) {

                case R.id.play:
                    AppController.Instance().clearAddToQueue(getActivity(), list);
                    break;

                case R.id.reset_recent_play_count:
                    resetItems(list);
                    break;

                case R.id.share:
                    AppController.Instance().shareSongs(list);
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

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        ((RecentActivity) getActivity()).enableTabs(true);
        handler.post(() -> {
            if (!recyclerView.isComputingLayout()) {
                ((SelectableAdapter) recyclerView.getAdapter()).setMultiSelect(false);
                actionMode = null;
            } else {
                onDestroyActionMode(this.actionMode);
            }
        });
    }

    private void favorite(final ArrayList<QueueItem> queueItems) {

        for (int i = 0; i < queueItems.size(); i++) {
            QueueItem queueItem = queueItems.get(i);
            TrackRealmHelper.addFavorite(queueItem.data);
        }

        AppController.toast(getActivity(), "Songs added to Favorites");
        getActivity().sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
        if (type != PlayerConstants.QUEUE_TYPE_TRACKS){
            getActivity().sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
            getActivity().sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
    }}

    private void movetoNegative(final ArrayList<QueueItem> queueItems) {
        Utils.askDelete(getActivity(), "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

            for (int i = 0; i < queueItems.size(); i++) {
                QueueItem queueItem = queueItems.get(i);
                TrackRealmHelper.movetoNegative(queueItem);
            }
            reload();
        });
    }

    @Override
    public void onDragTouched(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    public void onMenuClicked(final int position) {
        final QueueItem item = adapter.getItem(position);
        if (item == null) return;

        selectedItem = item;
        selectedItemPosition = position;
        selectedItems.clear();
        selectedItems.add(item);

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(MenuObject.PLAY_NEXT));
        items.add(new MenuObject(MenuObject.PLAY_X_TIMES));
        items.add(new MenuObject(MenuObject.ADD_TO_QUEUE));
        items.add(new MenuObject(MenuObject.ADD_TO_PLAYLIST));
        items.add(new MenuObject(MenuObject.FAV, (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1) ? getString(R.string.add_to_favs) : getString(R.string.remove_from_favs)));
        items.add(new MenuObject(MenuObject.RESET_RECENT_PLAYED));
        items.add(new MenuObject(MenuObject.GO_TO_ALBUM));
        items.add(new MenuObject(MenuObject.GO_TO_ARTIST));
        items.add(new MenuObject(MenuObject.SET_RINGTONE));
        items.add(new MenuObject(MenuObject.PREVIEW_SONG));
        items.add(new MenuObject(MenuObject.MOVE_TO_IGNORE));
        items.add(new MenuObject(MenuObject.SEND));
        items.add(new MenuObject(MenuObject.SHARE_ITEM));
        items.add(new MenuObject(MenuObject.CUT));
        items.add(new MenuObject(MenuObject.EDIT_TAGS));
        items.add(new MenuObject(MenuObject.DETAILS));
        items.add(
                new MenuObject(
                        ADD_TO_LIBRARY,
                        (TrackRealmHelper.getLibraryList().indexOf(item.data) == -1)
                                ? getString(R.string.add_to_library)
                                : getString(R.string.remove_from_library)));
        items.add(new MenuObject(MenuObject.DELETE_ITEM));

        MaterialMenuAdapter MaterialMenuAdapter = new MaterialMenuAdapter(items, this);

        materialDialog = new MaterialDialog.Builder(getActivity())
                .adapter(MaterialMenuAdapter, new LinearLayoutManager(getActivity()))
                .build();
        materialDialog.show();


    }

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
                                            .play(PlayerConstants.QUEUE_TYPE_RECENT, position, adapter.getList());
                                },
                                ThreadManager.Instance().getMuzikoBackgroundThreadPool());
                    },
                    getResources().getInteger(R.integer.ripple_duration_delay));

        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!this.adapter.isMultiSelect()) {
            ((AppCompatActivity) getActivity()).startSupportActionMode(this);

            this.adapter.setMultiSelect(true);
            toggleSelection(position);
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SetRingtone createRingtone = new SetRingtone();
                    createRingtone.open(getActivity(), selectedItem);
                } else {
                    AppController.toast(getActivity(), "Write settings permission wasn't provided. Muziko can't set default ringtone");
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_activity_child, container, false);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        itemList = new ArrayList<>();
        adapter = new GeneralAdapter(getActivity(), itemList, PlayerConstants.QUEUE_TYPE_TRACKS, prefShowArtwork, TAG, this);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(getActivity());


        recyclerView = rootView.findViewById(R.id.itemList);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setStateChangeListener(this);
        recyclerView.addOnScrollListener(new PicassoScrollListener(getActivity(), TAG));
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(adapter);

        register();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        unregister();

        super.onDestroyView();
    }

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(getActivity(), "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

            TrackRealmHelper.movetoNegative(queue);
            adapter.removeIndex(position);
        });
    }

    private void delete(final int position, final QueueItem queue) {
        DeleteTrackRunnable deleteTrackRunnable = new DeleteTrackRunnable(position, queue);
//        deleteTrackRunnable.run();
        checkStoragePermissions("Do you want to grant SD card permissions so tracks can be deleted?", deleteTrackRunnable);
    }

    @Override
    public void onFilterValue(int value) {
        PlayerConstants.QUEUE_TYPE = 0;

        switch (value) {
            case R.id.activity_sort_date_lastest:
                adapter.sortDateLatest();
                break;
            case R.id.activity_sort_date_earliest:
                adapter.sortDateEarliest();
                break;

            case R.id.activity_sort_duration_largest:
                adapter.sortDurationLargest();
                break;
            case R.id.activity_sort_duration_smallest:
                adapter.sortDurationSmallest();
                break;

            case R.id.activity_sort_atoz:
                adapter.sortTitleLowest();
                break;
            case R.id.activity_sort_ztoa:
                adapter.sortTitleHighest();
                break;

        }
    }

    @Override
    public void onSearchQuery(String chars) {
        adapter.search(chars);
    }

    @Override
    public void onReload(Context context) {

        reload();
    }

    @Override
    public void onStorageChanged() {

    }

    @Override
    public void onLayoutChanged(Float bottomMargin) {
        Resources resources = getActivity().getResources();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
        params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
        recyclerView.requestLayout();
    }

    @DebugLog
    private void toggleLibrary(final int pos, final QueueItem queue) {
        if (queue.isLibrary()) {
            LibraryEdit libraryEdit = new LibraryEdit(getActivity(), true, s -> adapter.notifyItemChanged(pos));
            libraryEdit.execute(queue);
        } else {
            if (!FirebaseManager.Instance().isOverLimitNow(1)) {
                LibraryEdit libraryEdit = new LibraryEdit(getActivity(), s -> adapter.notifyItemChanged(pos));
                libraryEdit.execute(queue);
            }
        }
    }

    private void queueUpdate(final Context context) {

        if (updateTimer != null) {
            try {
                updateTimer.cancel();
                updateTimer.purge();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }

            updateTimer = null;
        }

        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                handler.post(() -> reload());
            }
        }, 100);

    }

    private void resetItem(QueueItem item, int position) {

        if (TrackRealmHelper.resetRecentlyPlayedCount(item)) {
            adapter.removeIndex(position);
        } else {

            AppController.toast(getActivity(), "Unable to reset track play count!");
        }

    }

    private void resetItems(ArrayList<QueueItem> items) {

        for (QueueItem queueItem : items) {
            TrackRealmHelper.resetRecentlyPlayedCount(queueItem);
        }
        reload();
    }

    private void favorite(final int position, QueueItem queue) {
        FavoriteEdit fe = new FavoriteEdit(getActivity(), PlayerConstants.QUEUE_TYPE_FAVORITES, s -> adapter.notifyItemChanged(position));
        fe.execute(queue);
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(() -> {
            switch (item.id) {

                case MenuObject.RESET_RECENT_PLAYED:     //add to q
                    resetItem(selectedItem, selectedItemPosition);
                    break;

                case MenuObject.ADD_TO_QUEUE:     //add to q
                    PlayerConstants.QUEUE_TYPE = 0;
                    AppController.Instance().addToQueue(getActivity(), selectedItem, false);
                    break;

                case MenuObject.ADD_TO_PLAYLIST:     //add to p
                    AppController.Instance().addToPlaylist(getActivity(), selectedItem);
                    break;

                case MenuObject.FAV:     //add to f`
                    favorite(selectedItemPosition, selectedItem);
                    break;

                case MenuObject.PLAY_NEXT:     //play next
                    AppController.Instance().addToQueue(getActivity(), selectedItem, true);
                    break;

                case MenuObject.GO_TO_ARTIST:     //goto ar
                    AppController.Instance().gotoArtist(getActivity(), selectedItem, null);
                    break;

                case MenuObject.GO_TO_ALBUM:     //goto al
                    AppController.Instance().gotoAlbum(getActivity(), selectedItem, null);
                    break;

                case MenuObject.SEND:

                    final ArrayList<MenuObject> subMenuItems = new ArrayList<>();
                    subMenuItems.add(new MenuObject(MenuObject.SEND_CONTACTS));
                    subMenuItems.add(new MenuObject(MenuObject.SEND_WIFI));


                    MaterialMenuAdapter MaterialMenuAdapter = new MaterialMenuAdapter(subMenuItems, onSubMenuObjectItemSelected);

                    materialDialog = new MaterialDialog.Builder(getActivity())
                            .adapter(MaterialMenuAdapter, new LinearLayoutManager(getActivity()))
                            .build();
                    materialDialog.show();
                    break;

                case MenuObject.SET_RINGTONE:     //createRingtone

                    boolean permission;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permission = Settings.System.canWrite(getActivity());
                    } else {
                        permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                    }
                    if (!permission) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            materialDialog = new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Permission required").content("Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.").positiveText("Ok").onPositive((mdialog, mwhich) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                    startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                                } else {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
                                }
                            }).negativeText("Cancel").build();
                            materialDialog.show();

                        } else {
                            SetRingtone createRingtone = new SetRingtone();
                            createRingtone.open(getActivity(), selectedItem);
                        }
                    } else {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(getActivity(), selectedItem);
                    }
                    break;

                case MenuObject.CUT:     //cut
                    AppController.Instance().cutSong(selectedItem);
                    break;

                case MenuObject.PREVIEW_SONG:     //preview
                    PreviewSong previewSong = new PreviewSong();
                    previewSong.open(getActivity(), selectedItem);
                    break;

                case MenuObject.EDIT_TAGS:     //edit
                    AppController.Instance().editSong(getActivity(), TAG, selectedItemPosition, selectedItem);
                    break;

                case MenuObject.DETAILS:     //details
                    AppController.Instance().details(getActivity(), selectedItem);
                    break;

                case ADD_TO_LIBRARY:
                    toggleLibrary(selectedItemPosition, selectedItem);
                    break;

                case MenuObject.SHARE_ITEM:     //share
                    AppController.Instance().shareSong(getActivity(), selectedItem);
                    break;

                case MenuObject.MOVE_TO_IGNORE:     //share
                    movetoNegative(selectedItemPosition, selectedItem);
                    break;

                case MenuObject.DELETE_ITEM:     //delete
                    delete(selectedItemPosition, selectedItem);
                    break;

                case MenuObject.PLAY_X_TIMES:     //remove
                    AppController.Instance().removeAfter(getActivity(), selectedItem);
                    break;
            }

            dialog.dismiss();

        }, 600);
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

    private void register() {
        receiver = new ActivitiesReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_RECENT_CHANGED);
        filter.addAction(AppController.INTENT_TRACK_EDITED);

        getActivity().registerReceiver(receiver, filter);
    }

    private void unregister() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void reload() {
        loadRecentlyPlayed();
    }

    @Override
    public void onFastScrollStart() {
        ((RecentActivity) getActivity()).fastScrolling(true);
    }

    @Override
    public void onFastScrollStop() {

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
            Utils.askDelete(getActivity(), "Delete Song", "This will delete song permanently from this device, do you want to proceed ?", () -> {
                TrackDelete tr = new TrackDelete(getActivity(), PlayerConstants.QUEUE_TYPE_TRACKS, () -> adapter.removeIndex(position));
                tr.execute(queueItem);
            });
        }
    }

    private class ActivitiesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    switch (action) {
                        case AppController.INTENT_RECENT_CHANGED:
                            queueUpdate(context);
                            break;
                        case AppController.INTENT_MOST_CHANGED:
                            queueUpdate(context);
                            break;
                        case AppController.INTENT_TRACK_CHANGED:
                            reload();
                            break;
                        case AppController.INTENT_TRACK_DELETED:
                            reload();
                            break;
                        case AppController.INTENT_TRACK_EDITED:
                            int index = intent.getIntExtra("index", -1);
                            String tag = intent.getStringExtra("tag");
                            QueueItem item = (QueueItem) intent.getSerializableExtra("item");
                            if (item != null &&
                                    tag != null && tag.equals(TAG) &&
                                    index >= 0 && index < adapter.getItemCount()) {
                                adapter.put(index, item);
                            } else {
                                reload();
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
