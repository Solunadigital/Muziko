package com.muziko.fragments.History;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.HistoryActivity;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SharedFilesAdapter;
import com.muziko.callbacks.SharingCallback;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Person;
import com.muziko.common.models.firebase.Share;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.PreviewSong;
import com.muziko.dialogs.SetRingtone;
import com.muziko.fragments.BaseFragment;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.ShareListener;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.GsonManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.service.MuzikoFirebaseService;
import com.muziko.tasks.FavoriteEdit;
import com.muziko.tasks.LibraryEdit;
import com.muziko.tasks.ShareTrackDownloader;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

import static com.muziko.manager.MuzikoConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.muziko.objects.MenuObject.ADD_TO_LIBRARY;
import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.CUT;
import static com.muziko.objects.MenuObject.DETAILS;
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

/**
 * Created by dev on 2/11/2016.
 */

public class UnknownFragment extends BaseFragment implements SharingCallback, ShareListener, MaterialMenuAdapter.Callback {
    private final String TAG = UnknownFragment.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final List<Share> shares = new ArrayList<>();
    private final List<Person> contacts = new ArrayList<>();
    private FastScrollRecyclerView mRecyclerView;
    private SharedFilesAdapter mAdapter;
    private int sortId;
    private boolean reverse;
    private boolean isFaving = false;
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private RelativeLayout emptyLayout;
    private final Runnable checkforSync = new Runnable() {
        @Override
        public void run() {
            if (MuzikoFirebaseService.sharesReady) {
                emptyLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                emptyLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            handler.postDelayed(this, 1000);
        }
    };
    private boolean isAddingToLibrary;

    public UnknownFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_activity_child, container, false);

        mAdapter = new SharedFilesAdapter(getActivity(), shares, contacts, TAG, MyApplication.SHARING_UNKNOWN, this);

        NpaLinearLayoutManager layoutList = new NpaLinearLayoutManager(getActivity());

        emptyLayout = rootView.findViewById(R.id.emptyLayout);

        mRecyclerView = rootView.findViewById(R.id.itemList);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutList);
        mRecyclerView.setAdapter(mAdapter);

//        handler.postDelayed(checkforSync, 1000);

        return rootView;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {


        ((HistoryActivity) getActivity()).callbackUnknown = this;


        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((HistoryActivity) getActivity()).callbackUnknown = null;

    }

    @Override
    public void onListingChanged() {

    }

    @Override
    public void onItemClicked(int position) {
        Share share = mAdapter.getItem(position);
        if (share.getLocalfile() == null) {

            if (FirebaseManager.firebaseShareDownloaderTasks.containsKey(share.getShareUrl())) {

                new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Downlod in progress").content("Are you want to cancel the download?").positiveText("Cancel").onPositive((dialog, which) -> {

                    ShareTrackDownloader shareTrackDownloader = FirebaseManager.firebaseShareDownloaderTasks.get(share.getShareUrl());

                    shareTrackDownloader.cancelDownload();
                    shareTrackDownloader = null;
                    FirebaseManager.firebaseShareDownloaderTasks.remove(share.getShareUrl());
                    mAdapter.notifyItemChanged(position);

                }).negativeText("Keep going").show();

            } else {
                new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Download track").content("Are you want to download this track?").positiveText("OK").onPositive((dialog, which) -> {

                    String json = GsonManager.Instance().getGson().toJson(share);
                    Intent intent = new Intent(getActivity(), MuzikoFirebaseService.class);
                    intent.setAction(AppController.ACTION_DOWNLOAD);
                    intent.putExtra(AppController.ARG_ITEM, json);
                    getActivity().startService(intent);

                }).negativeText("Cancel").show();
            }
        } else {
            final QueueItem queueItem = TrackRealmHelper.getTrackByShare(share);
            if (queueItem == null) {
                AppController.toast(getActivity(), getString(R.string.track_not_found));
                return;
            }
            AppController.Instance().playCurrentSong(queueItem);
        }
    }

    public void onMenuClicked(Context context, int position) {
        final Share share = mAdapter.getItem(position);

        final QueueItem item = TrackRealmHelper.getTrackByShare(share);

        if (item == null) {
            AppController.toast(getActivity(), getString(R.string.track_not_found));
            return;
        }

        selectedItem = item;
        selectedItemPosition = position;

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(ADD_TO_QUEUE));
        items.add(new MenuObject(ADD_TO_PLAYLIST));
        items.add(new MenuObject(FAV, (TrackRealmHelper.getFavoritesList().indexOf(item.data) == -1) ? getString(R.string.add_to_favs) : getString(R.string.remove_from_favs)));
        items.add(new MenuObject(PLAY_NEXT));
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
        items.add(new MenuObject(SHARE_ITEM));
        items.add(new MenuObject(PLAY_X_TIMES));
        items.add(new MenuObject(MOVE_TO_IGNORE));

        MaterialMenuAdapter MaterialMenuAdapter = new MaterialMenuAdapter(items, this);

        new MaterialDialog.Builder(getActivity())
                .adapter(MaterialMenuAdapter, new LinearLayoutManager(getActivity()))
                .show();
    }

    @Override
    public void onBlockClicked(int position) {

    }

    @Override
    public boolean onItemLongClicked(int position) {

        return false;
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        handler.postDelayed(() -> {

            switch (item.id) {
                case ADD_TO_QUEUE:     //add to q
                    PlayerConstants.QUEUE_TYPE = 0;
                    AppController.Instance().addToQueue(getActivity(), selectedItem, false);
                    break;

                case ADD_TO_PLAYLIST:     //add to p
                    AppController.Instance().addToPlaylist(getActivity(), selectedItem);
                    break;

                case FAV:     //add to f`
                    favorite(selectedItemPosition, selectedItem);
                    break;

                case PLAY_NEXT:     //play next
                    AppController.Instance().addToQueue(getActivity(), selectedItem, true);
                    break;

                case GO_TO_ARTIST:     //goto ar
                    AppController.Instance().gotoArtist(getActivity(), selectedItem, null);
                    break;

                case GO_TO_ALBUM:     //goto al
                    AppController.Instance().gotoAlbum(getActivity(), selectedItem, null);
                    break;


                case SET_RINGTONE:     //createRingtone

                    boolean permission;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permission = Settings.System.canWrite(getActivity());
                    } else {
                        permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
                    }
                    if (!permission) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Permission required").content("Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.").positiveText("Ok").onPositive((mdialog, mwhich) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                    startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                                } else {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
                                }
                            }).negativeText("Cancel").show();
                        } else {
                            SetRingtone createRingtone = new SetRingtone();
                            createRingtone.open(getActivity(), selectedItem);
                        }
                    } else {
                        SetRingtone createRingtone = new SetRingtone();
                        createRingtone.open(getActivity(), selectedItem);
                    }
                    break;

                case CUT:     //cut
                    AppController.Instance().cutSong(selectedItem);
                    break;

                case PREVIEW_SONG:     //preview
                    PreviewSong previewSong = new PreviewSong();
                    previewSong.open(getActivity(), selectedItem);
                    break;

                case EDIT_TAGS:     //edit
                    AppController.Instance().editSong(getActivity(), TAG, selectedItemPosition, selectedItem);
                    break;

                case DETAILS:     //details
                    AppController.Instance().details(getActivity(), selectedItem);
                    break;

                case SHARE_ITEM:     //share
                    AppController.Instance().shareSong(getActivity(), selectedItem);
                    break;

                case PLAY_X_TIMES:     //remove
                    AppController.Instance().removeAfter(getActivity(), selectedItem);
                    break;

                case MOVE_TO_IGNORE:     //negative
                    movetoNegative(selectedItemPosition, selectedItem);
                    break;

                case ADD_TO_LIBRARY:
                    toggleLibrary(selectedItemPosition, selectedItem);
                    break;
            }

            dialog.dismiss();

        }, 600);
    }

    @DebugLog
    private void toggleLibrary(final int pos, final QueueItem queue) {
        if (queue.isLibrary()) {
            LibraryEdit libraryEdit = new LibraryEdit(getActivity(), true, s -> mAdapter.notifyItemChanged(pos));
            libraryEdit.execute(queue);
        } else {
            if (!FirebaseManager.Instance().isOverLimitNow(1)) {
                LibraryEdit libraryEdit = new LibraryEdit(getActivity(), s -> mAdapter.notifyItemChanged(pos));
                libraryEdit.execute(queue);
            }
        }
    }

    private void favorite(final int pos, final QueueItem queue) {
        if (isFaving) return;
        isFaving = true;

        FavoriteEdit fe = new FavoriteEdit(getActivity(), PlayerConstants.QUEUE_TYPE_TRACKS, s -> {
            isFaving = false;

            mAdapter.notifyItemChanged(pos);
        });
        fe.execute(queue);
    }

    private void movetoNegative(final int position, final QueueItem queue) {
        Utils.askDelete(getActivity(), "Move to Ignore Folder", "You can later restore it from ignore folder, are you sure you want to ignore it?", () -> {

            TrackRealmHelper.movetoNegative(queue);
            mAdapter.removeIndex(position);
        });
    }

    @Override
    public void onFilterValue(int value, boolean reverse) {

        switch (value) {

            case R.id.sharing_sort_sent:
                if (!reverse) {
                    mAdapter.sortSentLowest();
                } else {
                    mAdapter.sortSentHighest();
                }
                break;

            case R.id.sharing_sort_title:
                if (!reverse) {
                    mAdapter.sortTitleLowest();
                } else {
                    mAdapter.sortTitleHighest();
                }
                break;
            case R.id.sharing_sort_online:
                if (!reverse) {
                    mAdapter.sortSenOnlineLowest();
                } else {
                    mAdapter.sortSentOnlineHighest();
                }
                break;
            case R.id.sharing_sort_downloaded:
                if (!reverse) {
                    mAdapter.sortDownloadedLowest();
                } else {
                    mAdapter.sortDownloadedHighest();
                }
                break;
        }
    }

    @Override
    public void onSearchQuery(String chars) {
        mAdapter.search(chars);
    }

    @Override
    public void onReload() {

        reload();
    }

    @Override
    public void onStorageChanged() {

    }

    @Override
    public void onLayoutChanged(Float bottomMargin) {
        Resources resources = getActivity().getResources();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
        params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
        mRecyclerView.requestLayout();
    }

    @Override
    public void onDownloadProgress(String url, int progress) {

    }

    private void reload() {

        contacts.clear();
        contacts.addAll(MyApplication.fullUerList.values());

        shares.clear();
        for (Share share : MyApplication.shareList) {

            if (!share.getSenderId().equals(FirebaseManager.Instance().getCurrentUserId()) && !share.isFriend()) {
                shares.add(share);
            }
        }

        mAdapter.notifyDataSetChanged();
        onFilterValue(sortId, reverse);
    }
}
