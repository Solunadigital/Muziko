package com.muziko.fragments.Listening;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.AlbumArtFolderActivity;
import com.muziko.activities.AlbumArtID3Activity;
import com.muziko.activities.AlbumArtInternetActivity;
import com.muziko.activities.MainActivity;
import com.muziko.activities.PlayerListActivity;
import com.muziko.adapter.AlbumAdapter;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.callbacks.PlayerCallback;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.fragments.BaseFragment;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.helpers.SortHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.ImageManager;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.TrackDelete;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_ALBUM_FOLDER;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_GALLERY;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_ID3_TAGS;
import static com.muziko.manager.MuzikoConstants.ARTWORK_PICK_FROM_INTERNET;
import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.DELETE_ITEM;
import static com.muziko.objects.MenuObject.MANAGE_ARTWORK;
import static com.muziko.objects.MenuObject.MOVE_TO_IGNORE;
import static com.muziko.objects.MenuObject.PLAY_NEXT;
import static com.muziko.objects.MenuObject.SHARE_ITEM;


public class AlbumsFragment extends BaseFragment implements PlayerCallback, RecyclerItemListener, ActionMode.Callback, MaterialMenuAdapter.Callback {
    private final String TAG = AlbumsFragment.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final int FILE_SELECT_CODE = 390;
    private final ArrayList<QueueItem> albumList = new ArrayList<>();
    private QueueItem selectedItem;
    private int selectedItemPosition;
    private AlbumsReceiver receiver;
    private AlbumAdapter adapter = null;
    private NpaLinearLayoutManager layoutList;
    private NpaGridLayoutManager layoutGrid2;
    private NpaGridLayoutManager layoutGrid3;
    private NpaGridLayoutManager layoutGrid4;

    private FastScrollRecyclerView recyclerView;
    private ActionMode actionMode = null;
    private int playType;
    private String playName;
    private boolean mode;
    private int grid;
    private QueueItem queueItem;
    private int position;
    private Target picassoTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
            byte[] byteArray = stream.toByteArray();
            ArtworkHelper artworkHelper = new ArtworkHelper();
            artworkHelper.setArt(getActivity(), queueItem, byteArray);
            adapter.notifyItemChanged(position);
            if (mode) {
                ((PlayerListActivity) getActivity()).updateFragments();
            } else {
                ((MainActivity) getActivity()).updateFragments();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // loading of the bitmap failed
            // TODO do some action/warning/error message
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
    private boolean isResumed;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Override
    public void onDragTouched(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    public void onMenuClicked(final int position) {
        final QueueItem item = adapter.getItem(position);
        if (item == null) return;
//		final QueueItem queueitem = TrackRealmHelper.getTrackByAlbum(item.album);

        selectedItem = item;
        selectedItemPosition = position;

        final ArrayList<MenuObject> items = new ArrayList<>();
        items.add(new MenuObject(PLAY_NEXT));
        items.add(new MenuObject(ADD_TO_QUEUE));
        items.add(new MenuObject(ADD_TO_PLAYLIST));
        items.add(new MenuObject(MOVE_TO_IGNORE));
        items.add(new MenuObject(MANAGE_ARTWORK));
        items.add(new MenuObject(SHARE_ITEM));
        items.add(new MenuObject(DELETE_ITEM));

        MaterialMenuAdapter MaterialMenuAdapter = new MaterialMenuAdapter(items, this);

        new MaterialDialog.Builder(getActivity())
                .adapter(MaterialMenuAdapter, new LinearLayoutManager(getActivity()))
                .show();

    }

    @Override
    public void onItemClicked(int position) {
        if (adapter.isMultiSelect()) {
            toggleSelection(position);
        } else {
            QueueItem album = adapter.getItem(position);
            if (album == null) return;

            Intent playerlistIntent = new Intent(getActivity(), PlayerListActivity.class);
            playerlistIntent.putExtra(MyApplication.ARG_ID, album.id);
            playerlistIntent.putExtra(MyApplication.ARG_ART, album.album);
            playerlistIntent.putExtra(MyApplication.ARG_NAME, album.title);
            playerlistIntent.putExtra(MyApplication.ARG_TYPE, PlayerConstants.QUEUE_TYPE_ALBUMS);
            playerlistIntent.putExtra(MyApplication.ARG_DATA, album.title);
            playerlistIntent.putExtra(MyApplication.ARG_DURATION, album.duration);
            playerlistIntent.putExtra(MyApplication.ARG_SONGS, album.songs);
            handler.postDelayed(() -> ActivityCompat.startActivity(getActivity(), playerlistIntent, null), getResources().getInteger(R.integer.ripple_duration_delay));

        }

    }

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

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.context_groups, menu);
        return true;
    }

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

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        ArrayList<QueueItem> list = adapter.getSelectedItems();

        ArrayList<QueueItem> queueItems = new ArrayList<>();
        for (QueueItem queueItem : list) {
            queueItems.addAll(TrackRealmHelper.getTracksForAlbum(queueItem.title));
        }
        if (queueItems.size() > 0) {

            switch (item.getItemId()) {

                case R.id.share:
                    AppController.Instance().shareSongs(queueItems);
                    break;

                case R.id.add_to_queue:
                    AppController.Instance().addToQueue(getActivity(), queueItems, false);
                    break;

                case R.id.play_next:
                    AppController.Instance().addToQueue(getActivity(), queueItems, true);
                    break;

                case R.id.add_to_playlist:
                    AppController.Instance().addToPlaylist(getActivity(), queueItems, false);
                    break;

                case R.id.delete:
                    deleteItems(list, queueItems);
                    break;

                case R.id.multi_tag_edit:
                    AppController.Instance().multiTagEdit(getActivity(), queueItems);
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

    private void deleteItems(final ArrayList<QueueItem> list, final ArrayList<QueueItem> queueItems) {
        DeleteTracksRunnable deleteTracksRunnable = new DeleteTracksRunnable(queueItems);
        checkStoragePermissions(getString(R.string.grant_sd_perm), deleteTracksRunnable);
    }

    private void toggleSelection(int position) {
        ((SelectableAdapter) recyclerView.getAdapter()).toggleSelection(position);

        int count = ((SelectableAdapter) recyclerView.getAdapter()).getSelectedItemCount();

        if (actionMode != null) {
            if (count == 0) actionMode.setTitle("");
            else actionMode.setTitle(String.format("%d album%s", count, count != 1 ? "s" : ""));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARTWORK_PICK_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK && selectedItem != null) {

                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = Utils.getPath(getActivity(), uri);
                    if (path != null) {

//						ImageCompressionAsyncTask imageCompression = new ImageCompressionAsyncTask() {
//							@Override
//							protected void onPostExecute(byte[] imageBytes) {
//								// image here is compressed & ready to be sent to the server
//							}
//						};
//						imageCompression.execute(path);// imagePath as a string

                        Bitmap bMap = BitmapFactory.decodeFile(path);
                        Bitmap out = Utils.resize(bMap, MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        out.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
                        bMap.recycle();
                        out.recycle();
                        byte[] bitmapData = stream.toByteArray();

                        if (bitmapData != null) {
                            ArtworkHelper artworkHelper = new ArtworkHelper();
                            artworkHelper.setArt(getActivity(), selectedItem, bitmapData);
                            adapter.notifyItemChanged(selectedItemPosition);
                            if (mode) {
                                ((PlayerListActivity) getActivity()).updateFragments();
                            } else {
                                ((MainActivity) getActivity()).updateFragments();
                            }
                        } else {
                            AppController.toast(getActivity(), "Unable to read file!");
                        }
                    } else {
                        AppController.toast(getActivity(), "File not available on device!");
                    }
                }

                break;

            case ARTWORK_PICK_FROM_INTERNET:
                if (resultCode == Activity.RESULT_OK) {


                    final QueueItem queueItem = (QueueItem) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    byte[] byteArray = data.getByteArrayExtra("image");

                    if (byteArray != null) {
                        ArtworkHelper artworkHelper = new ArtworkHelper();
                        artworkHelper.setArt(getActivity(), queueItem, byteArray);
                        adapter.notifyItemChanged(position);
                        if (mode) {
                            ((PlayerListActivity) getActivity()).updateFragments();
                        } else {
                            ((MainActivity) getActivity()).updateFragments();
                        }
                    } else {
                        AppController.toast(getActivity(), "Album Art not found");
                    }

                }
                break;

            case ARTWORK_PICK_FROM_ALBUM_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    queueItem = (QueueItem) data.getSerializableExtra("item");
                    position = data.getIntExtra("index", 0);
                    String filePath = data.getStringExtra("filepath");

                    if (filePath != null) {

                        Picasso.with(getActivity())
                                .load(filePath)
                                .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                                .into(picassoTarget);

                    } else {
                        AppController.toast(getActivity(), "Album Art not found");
                    }

                }

                break;

            case ARTWORK_PICK_FROM_ID3_TAGS:
                if (resultCode == Activity.RESULT_OK) {
                    ArtworkHelper artworkHelper = new ArtworkHelper();
                    QueueItem queueItem = (QueueItem) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    byte[] bitmapData = artworkHelper.pickAlbumArt(getActivity(), queueItem);

                    if (queueItem != null && bitmapData != null) {
                        artworkHelper.setArt(getActivity(), queueItem, bitmapData);
                        adapter.notifyItemChanged(position);
                        if (mode) {
                            ((PlayerListActivity) getActivity()).updateFragments();
                        } else {
                            ((MainActivity) getActivity()).updateFragments();
                        }
                    } else {
                        AppController.toast(getActivity(), "Album Art not found");
                    }

                }

                break;


            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == Activity.RESULT_OK && selectedItem != null) {

                    Uri uri = result.getUri();
                    String path = Utils.getPath(getActivity(), uri);
                    if (path != null) {

                        Bitmap bMap = BitmapFactory.decodeFile(path);
                        Bitmap out = Utils.resize(bMap, MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        out.compress(Bitmap.CompressFormat.JPEG, MyApplication.JPEG_COMPRESS, stream);
                        bMap.recycle();
                        out.recycle();
                        byte[] bitmapData = stream.toByteArray();
                        ArtworkHelper artworkHelper = new ArtworkHelper();
                        if (bitmapData != null) {
                            artworkHelper.setArt(getActivity(), selectedItem, bitmapData);
                            adapter.notifyItemChanged(selectedItemPosition);
                            if (mode) {
                                ((PlayerListActivity) getActivity()).updateFragments();
                            } else {
                                ((MainActivity) getActivity()).updateFragments();
                            }
                        } else {
                            AppController.toast(getActivity(), "Unable to read file!");
                        }
                    } else {
                        AppController.toast(getActivity(), "File not available on device!");
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Crashlytics.logException(error);
                    AppController.toast(getActivity(), "Album art was not changed");
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mode = true;
            ((PlayerListActivity) getActivity()).callbackAlbum = this;
        } else {
            ((MainActivity) getActivity()).callbackAlbum = this;
        }
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isResumed || albumList.size() == 0) {
            isResumed = true;
            onReload();
        }
    }

    @Override
    public void onDestroy() {
        unregister();
        handler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mode) {
            ((PlayerListActivity) getActivity()).callbackAlbum = null;
        } else {
            ((MainActivity) getActivity()).callbackAlbum = null;
        }
        unregister();
        super.onDetach();

    }

    @Override
    public void onListingChanged() {

        if (mode) {
            grid = PrefsManager.Instance().getPlayerListAlbumsViewType();
        } else {
            grid = PrefsManager.Instance().getAlbumsViewType();
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

            adapter.notifyChangeAll();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player_albums, container, false);

        boolean prefShowArtwork = SettingsManager.Instance().getPrefShowArtWork();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mode = true;
            playType = bundle.getInt(MyApplication.ARG_TYPE, 0);
            playName = bundle.getString(MyApplication.ARG_NAME);
        }

        adapter = new AlbumAdapter(getActivity(), albumList, TAG, this);
        adapter.setSortid(PrefsManager.Instance().getGenreSort());

        layoutList = new NpaLinearLayoutManager(getActivity());
        layoutGrid2 = new NpaGridLayoutManager(getActivity(), 2);
        layoutGrid3 = new NpaGridLayoutManager(getActivity(), 3);
        layoutGrid4 = new NpaGridLayoutManager(getActivity(), 4);

        recyclerView = rootView.findViewById(R.id.itemList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
//		recyclerView.addOnScrollListener(new PicassoScrollListener(getActivity(), TAG));
        recyclerView.setHasFixedSize(true);
        if (mode) {
            grid = PrefsManager.Instance().getPlayerListAlbumsViewType();
        } else {
            grid = PrefsManager.Instance().getAlbumsViewType();
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

    private void movetoNegative(final int position, final QueueItem queueItem) {
        Utils.askDelete(getActivity(), getString(R.string.move_to_ignore), getString(R.string.you_can_restore_ignore_later), () -> {

            ArrayList<QueueItem> queueItems;
            queueItems = TrackRealmHelper.getTracksForAlbum(queueItem.album_name);
            for (QueueItem queueItem1 : queueItems) {
                TrackRealmHelper.movetoNegative(queueItem1);
            }
            adapter.removeIndex(position);
        });
    }

    @Override
    public void onFilterValue(int value, boolean reverse) {
        adapter.setSortid(value);
        switch (value) {
            case R.id.player_sort_tracks:
                if (!reverse) {
                    adapter.sortTrackLowest();
                } else {
                    adapter.sortTrackHighest();
                }
                break;
            case R.id.player_sort_title:
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
            case R.id.player_sort_duration:
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
            case R.id.player_sort_date:
                if (!reverse) {
                    adapter.sortDateEarliest();
                } else {
                    adapter.sortDateLatest();
                }
                break;
            case R.id.player_sort_songs:
                if (!reverse) {
                    adapter.sortSongsLowest();
                } else {
                    adapter.sortSongsHighest();
                }
                break;
        }
        //}
    }

    @Override
    public void onSearchQuery(String chars) {
        adapter.search(chars);
    }

    @Override
    public void onReload() {

        reload();
    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onStorageChanged() {

    }

    @Override
    public void onLayoutChanged(float bottomMargin) {

        if (recyclerView != null) {
            Resources resources = getActivity().getResources();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            params.setMargins(0, 0, 0, Utils.toPixels(resources, bottomMargin));
            recyclerView.requestLayout();
        }
    }

    @Override
    public void onDownloadProgress(String url, int progress) {

    }

    @Override
    public void onTrackAdded(String data) {

    }

    @Override
    public void onNowPlaying() {

    }

    @Override
    public void onFirebaseLimitChanged() {

    }


    private ArrayList<QueueItem> firstLoad(final Context context) {

        ArrayList<QueueItem> albums = new ArrayList<>();
        albums.addAll(TrackRealmHelper.getAlbums().values());
        SortHelper sortHelper = new SortHelper();
        albums = sortHelper.sort(PrefsManager.Instance().getAlbumSort(), PrefsManager.Instance().getAlbumSortReverse(), albums);
        return albums;
    }

    private void reload() {
        if (mode) {
            albumList.clear();
            adapter.notifyDataSetChanged();
            ArrayList<QueueItem> albums = new ArrayList<>();
            albums.addAll(TrackRealmHelper.getAlbums().values());
            for (QueueItem queueItem : albums) {
                if (playType == PlayerConstants.QUEUE_TYPE_ARTISTS) {
                    if (queueItem.artist_name.equals(playName)) {
                        queueItem.type = MyApplication.ALBUMS;
                        albumList.add(queueItem);
                    }
                }

                if (playType == PlayerConstants.QUEUE_TYPE_GENRES) {
                    if (queueItem.genre_name.equals(playName)) {
                        queueItem.type = MyApplication.ALBUMS;
                        albumList.add(queueItem);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            albumList.clear();
            adapter.notifyDataSetChanged();
            albumList.addAll(TrackRealmHelper.getAlbums().values());
            adapter.notifyDataSetChanged();
        }

        onFilterValue(PrefsManager.Instance().getAlbumSort(), PrefsManager.Instance().getAlbumSortReverse());
    }

    private void register() {
        receiver = new AlbumsReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppController.INTENT_TRACK_DELETED);
        filter.addAction(AppController.INTENT_TRACK_EDITED);
        getActivity().registerReceiver(receiver, filter);
    }

    private void unregister() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

        final ArtworkHelper artworkHelper = new ArtworkHelper();

        handler.postDelayed(() -> {


            switch (item.id) {
                case ADD_TO_QUEUE:     //add to q
                    AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_QUEUE);
                    break;

                case ADD_TO_PLAYLIST:     //add to p
                    AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_SAVE);
                    break;

                case PLAY_NEXT:     //play next
                    AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_NEXT);
                    break;

                case MANAGE_ARTWORK:     //manage artwork

                    selectedItem = TrackRealmHelper.getTrackByAlbum(selectedItem.album);

                    new MaterialDialog.Builder(getActivity())
                            .theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).title(R.string.manage_artwork)
                            .items(R.array.manage_artwork)
                            .itemsCallback((dialog1, view1, which1, text) -> {

                                switch (which1) {

                                    case 0:

                                        artworkHelper.autoPickAlbumArt(getActivity(), selectedItem, false);
                                        adapter.notifyItemChanged(selectedItemPosition);
                                        if (mode) {
                                            ((PlayerListActivity) getActivity()).updateFragments();
                                        } else {
                                            ((MainActivity) getActivity()).updateFragments();
                                        }
                                        break;

                                    case 1:

                                        Intent internetintent = new Intent(getActivity(), AlbumArtInternetActivity.class);
                                        internetintent.putExtra("tag", TAG);
                                        internetintent.putExtra("item", selectedItem);
                                        internetintent.putExtra("index", selectedItemPosition);
                                        getActivity().startActivityForResult(internetintent, ARTWORK_PICK_FROM_INTERNET);

                                        break;

                                    case 2:

                                        Intent intent = new Intent(Intent.ACTION_PICK);
                                        intent.setType("image/*");

                                        try {
                                            getActivity(). startActivityForResult(Intent.createChooser(intent, getString(R.string.select_album_art)), ARTWORK_PICK_FROM_GALLERY);
                                        } catch (ActivityNotFoundException ex) {
                                            // Potentially direct the user to the Market with a Dialog
                                            AppController.toast(getActivity(), getString(R.string.install_file_manager));
                                        }
                                        break;

                                    case 3:
                                        Intent folderintent = new Intent(getActivity(), AlbumArtFolderActivity.class);
                                        folderintent.putExtra("tag", TAG);
                                        folderintent.putExtra("item", selectedItem);
                                        folderintent.putExtra("index", selectedItemPosition);
                                        getActivity().startActivityForResult(folderintent, ARTWORK_PICK_FROM_ALBUM_FOLDER);

                                        break;

                                    case 4:

                                        Intent albumartIntent = new Intent(getActivity(), AlbumArtID3Activity.class);
                                        albumartIntent.putExtra("tag", TAG);
                                        albumartIntent.putExtra("item", selectedItem);
                                        albumartIntent.putExtra("index", selectedItemPosition);
                                        getActivity().startActivityForResult(albumartIntent, ARTWORK_PICK_FROM_ID3_TAGS);
                                        break;

                                    case 5:

                                        Uri myUri = Uri.parse("content://media/external/audio/albumart/" + selectedItem.album);
                                        CropImage.activity(myUri)
                                                .setGuidelines(CropImageView.Guidelines.ON)
                                                .setActivityTitle(selectedItem.album_name)
                                                .start(getActivity());

                                        break;

                                    case 6:
                                        artworkHelper.removeArt(getActivity(), selectedItem);
                                        adapter.notifyItemChanged(selectedItemPosition);
                                        if (mode) {
                                            ((PlayerListActivity) getActivity()).updateFragments();
                                        } else {
                                            ((MainActivity) getActivity()).updateFragments();
                                        }
                                        break;

                                }
                            })
                            .show();

                    break;

                case SHARE_ITEM:     //share
                    AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_SHARE);
                    break;

                case MOVE_TO_IGNORE:     //negative
                    movetoNegative(selectedItemPosition, selectedItem);
                    break;

                case DELETE_ITEM:     //remove
                    AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ALBUMS, PlayerConstants.QUEUE_ACTION_DELETE);
                    break;

            }
            dialog.dismiss();

        }, getResources().getInteger(R.integer.ripple_duration_delay));
    }

    private class DeleteTracksRunnable implements Runnable {

        private ArrayList<QueueItem> queueItems;

        public DeleteTracksRunnable(ArrayList<QueueItem> queueItems) {
            this.queueItems = queueItems;
        }

        @Override
        public void run() {
            Utils.askDelete(getActivity(), getString(R.string.delete_songs), String.format("This will delete song%s permanently from this device, do you want to proceed ?", queueItems.size() != 1 ? "s" : ""), () -> {
                TrackDelete tr = new TrackDelete(getActivity(), PlayerConstants.QUEUE_TYPE_TRACKS, () -> {
                    adapter.removeAll(queueItems);
                    AppController.toast(getActivity(), String.format("Song%s deleted from device", queueItems.size() != 1 ? "s" : ""));
                });
                tr.execute(queueItems);

            });
        }
    }

    private class AlbumsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();

                    if (action.equals(AppController.INTENT_TRACK_DELETED)) {
                        reload();
                    }
                    if (action.equals(AppController.INTENT_TRACK_EDITED)) {
                        reload();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract class ImageCompressionAsyncTask extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... strings) {
            if (strings.length == 0 || strings[0] == null)
                return null;
            return ImageManager.Instance().compressImage(strings[0]);
        }

        protected abstract void onPostExecute(byte[] imageBytes);
    }

}
