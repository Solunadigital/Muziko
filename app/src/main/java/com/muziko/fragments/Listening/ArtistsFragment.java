package com.muziko.fragments.Listening;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.badoo.mobile.util.WeakHandler;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.activities.PlayerListActivity;
import com.muziko.adapter.ArtistAdapter;
import com.muziko.adapter.MaterialMenuAdapter;
import com.muziko.adapter.SelectableAdapter;
import com.muziko.callbacks.PlayerCallback;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.LayoutManagers.NpaGridLayoutManager;
import com.muziko.controls.LayoutManagers.NpaLinearLayoutManager;
import com.muziko.database.TrackRealmHelper;
import com.muziko.fragments.BaseFragment;
import com.muziko.helpers.SortHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.objects.MenuObject;
import com.muziko.tasks.TrackDelete;

import java.util.ArrayList;

import static com.muziko.objects.MenuObject.ADD_TO_PLAYLIST;
import static com.muziko.objects.MenuObject.ADD_TO_QUEUE;
import static com.muziko.objects.MenuObject.DELETE_ITEM;
import static com.muziko.objects.MenuObject.MOVE_TO_IGNORE;
import static com.muziko.objects.MenuObject.PLAY_NEXT;
import static com.muziko.objects.MenuObject.SHARE_ITEM;


public class ArtistsFragment extends BaseFragment implements PlayerCallback, RecyclerItemListener, ActionMode.Callback, MaterialMenuAdapter.Callback {
    private final String TAG = ArtistsFragment.class.getName();
    private final WeakHandler handler = new WeakHandler();
    private final ArrayList<QueueItem> artistList = new ArrayList<>();
	private ArtistsReceiver receiver;
	private ArtistAdapter adapter = null;
	private NpaLinearLayoutManager layoutList;
	private NpaGridLayoutManager layoutGrid2;
	private NpaGridLayoutManager layoutGrid3;
	private NpaGridLayoutManager layoutGrid4;
    private FastScrollRecyclerView recyclerView;
    private QueueItem selectedItem;
	private int selectedItemPosition;
	private ActionMode actionMode = null;
	private boolean isResumed;

    public ArtistsFragment() {
        // Required empty public constructor
    }

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_player_artists, container, false);

		adapter = new ArtistAdapter(getActivity(), artistList, TAG, this);
        adapter.setSortid(PrefsManager.Instance().getGenreSort());

		layoutList = new NpaLinearLayoutManager(getActivity());
		layoutGrid2 = new NpaGridLayoutManager(getActivity(), 2);
		layoutGrid3 = new NpaGridLayoutManager(getActivity(), 3);
		layoutGrid4 = new NpaGridLayoutManager(getActivity(), 4);

        recyclerView = rootView.findViewById(R.id.itemList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
//		recyclerView.addOnScrollListener(new PicassoScrollListener(getActivity(), TAG));
        recyclerView.setHasFixedSize(true);
        int grid = PrefsManager.Instance().getArtistsViewType();
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

	@Override
	public void onAttach(Context context) {
		((MainActivity) getActivity()).callbackArtist = this;
		super.onAttach(context);
	}

    @Override
    public void onResume() {
        super.onResume();
		if (!isResumed || artistList.size() == 0) {
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

        ((MainActivity) getActivity()).callbackArtist = null;
        unregister();
        super.onDetach();
    }

    @Override
    public void onListingChanged() {

        if (adapter.getGridtype() != PrefsManager.Instance().getArtistsViewType()) {
            adapter.setGridtype(PrefsManager.Instance().getArtistsViewType());
            switch (PrefsManager.Instance().getArtistsViewType()) {
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
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		getActivity().getMenuInflater().inflate(R.menu.context_groups, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		actionMode = mode;
		((MainActivity) getActivity()).enableTabs(false);

		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		ArrayList<QueueItem> list = adapter.getSelectedItems();

		ArrayList<QueueItem> queueItems = new ArrayList<>();
		for (QueueItem queueItem : list) {
			queueItems.addAll(TrackRealmHelper.getTracksForArtist(queueItem.title));
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
        ((MainActivity) getActivity()).enableTabs(true);
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
			else actionMode.setTitle(String.format("%d artist%s", count, count != 1 ? "s" : ""));
		}
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

		final ArrayList<MenuObject> items = new ArrayList<>();
		items.add(new MenuObject(PLAY_NEXT));
		items.add(new MenuObject(ADD_TO_QUEUE));
		items.add(new MenuObject(ADD_TO_PLAYLIST));
		items.add(new MenuObject(MOVE_TO_IGNORE));
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

            QueueItem artist = adapter.getItem(position);
            if (artist == null) return;

            Intent playerlistIntent = new Intent(getActivity(), PlayerListActivity.class);
            playerlistIntent.putExtra(MyApplication.ARG_ID, artist.id);
            playerlistIntent.putExtra(MyApplication.ARG_ART, artist.album);
            playerlistIntent.putExtra(MyApplication.ARG_NAME, artist.title);
            playerlistIntent.putExtra(MyApplication.ARG_DATA, artist.title);
            playerlistIntent.putExtra(MyApplication.ARG_TYPE, PlayerConstants.QUEUE_TYPE_ARTISTS);
            playerlistIntent.putExtra(MyApplication.ARG_DURATION, artist.duration);
            playerlistIntent.putExtra(MyApplication.ARG_SONGS, artist.songs);
            handler.postDelayed(() -> ActivityCompat.startActivity(getActivity(), playerlistIntent, null), getResources().getInteger(R.integer.ripple_duration_delay));
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

    @Override
    public void onMenuObjectItemSelected(MaterialDialog dialog, int index, MenuObject item) {

		handler.postDelayed(() -> {

			switch (item.id) {
				case ADD_TO_QUEUE:     //add to q
					AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ARTISTS, PlayerConstants.QUEUE_ACTION_QUEUE);
					break;

				case ADD_TO_PLAYLIST:     //add to p
					AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ARTISTS, PlayerConstants.QUEUE_ACTION_SAVE);
					break;

				case PLAY_NEXT:     //play next
					AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ARTISTS, PlayerConstants.QUEUE_ACTION_NEXT);
					break;

				case SHARE_ITEM:     //share
					AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ARTISTS, PlayerConstants.QUEUE_ACTION_SHARE);
					break;

				case MOVE_TO_IGNORE:     //Negative
					movetoNegative(selectedItemPosition, selectedItem);
					break;

				case DELETE_ITEM:     //remove
					AppController.Instance().actionItem(getActivity(), selectedItemPosition, selectedItem, PlayerConstants.QUEUE_TYPE_ARTISTS, PlayerConstants.QUEUE_ACTION_DELETE);
					break;
			}
			dialog.dismiss();

		}, getResources().getInteger(R.integer.ripple_duration_delay));
	}

	private void movetoNegative(final int position, final QueueItem queueItem) {
		Utils.askDelete(getActivity(), getString(R.string.move_to_ignore), getString(R.string.you_can_restore_ignore_later), () -> {

			ArrayList<QueueItem> queueItems;
			queueItems = TrackRealmHelper.getTracksForArtist(queueItem.title);
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
        adapter.getFilter().filter(chars);
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

        ArrayList<QueueItem> artists = new ArrayList<>();
        artists.addAll(TrackRealmHelper.getArtists().values());
        SortHelper sortHelper = new SortHelper();
        artists = sortHelper.sort(PrefsManager.Instance().getArtistSort(), PrefsManager.Instance().getArtistSortReverse(), artists);
        return artists;
    }

	private void reload() {

		artistList.clear();
		adapter.notifyDataSetChanged();
		artistList.addAll(TrackRealmHelper.getArtists().values());
		adapter.notifyDataSetChanged();

        onFilterValue(PrefsManager.Instance().getArtistSort(), PrefsManager.Instance().getArtistSortReverse());
    }

	private void register() {
		receiver = new ArtistsReceiver();

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

	private class ArtistsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent != null) {
					String action = intent.getAction();

					if (action.equals(AppController.INTENT_TRACK_DELETED)) {
						reload();
					} else if (action.equals(AppController.INTENT_TRACK_EDITED)) {
						reload();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
