package com.muziko.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.makeramen.roundedimageview.RoundedImageView;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.SearchRecyclerListener;
import com.muziko.manager.AppController;
import com.muziko.manager.ImageManager;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by dev on 8/09/2016.
 */
public class PlayerListAdapter extends SelectableAdapter<RecyclerView.ViewHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
	private final int GRID1 = 0;
	private final int GRID2 = 1;
	private final int GRID3 = 2;
	private final int GRID4 = 3;
	private final int type;
	private final Context mContext;
	private final ArrayList<QueueItem> filts;
	private final ArrayList<QueueItem> sort = new ArrayList<>();
	private final ArrayList<QueueItem> nosort = new ArrayList<>();
	private final SearchRecyclerListener listener;
	private String lastSectionName = "A";
	private int storage;
	private String tag;
	private boolean showArtwork;
	private int gridtype = 0;
	private int filterCount = 0;
	private CustomFilter filter;
	private ArrayList<QueueItem> items;
	private String search = "";
	private String sortType = null;
	private int albumsFirstPosition = 0;
	private int tracksFirstPosition = 0;
	private int albumCount = 0;

	public PlayerListAdapter(Context context, ArrayList<QueueItem> listData, int type, boolean showArtwork, String tag, SearchRecyclerListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.filts = listData;
		this.type = type;
		this.showArtwork = showArtwork;
		this.tag = tag;
		this.listener = listener;
	}

	public int getAlbumCount() {
		return albumCount;
	}

	public void setAlbumCount(int albumCount) {
		this.albumCount = albumCount;
	}

	public int getAlbumsFirstPosition() {
		return albumsFirstPosition;
	}

	public void setAlbumsFirstPosition(int albumsFirstPosition) {
		this.albumsFirstPosition = albumsFirstPosition;
	}

	public int getTracksFirstPosition() {
		return tracksFirstPosition;
	}

	public void setTracksFirstPosition(int tracksFirstPosition) {
		this.tracksFirstPosition = tracksFirstPosition;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getFilterCount() {
		return filterCount;
	}

	private void setFilterCount(int filterCount) {
		this.filterCount = filterCount;
	}

	public boolean isShowArtwork() {
		return showArtwork;
	}

	public void setShowArtwork(boolean showArtwork) {
		this.showArtwork = showArtwork;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;
		ItemViewHolder itemViewHolder;
		HeaderViewHolder headerViewHolder;
		if (viewType == 1) {
			view = LayoutInflater.from(mContext).inflate(R.layout.section_header, parent, false);
			headerViewHolder = new HeaderViewHolder(mContext, view);
			return headerViewHolder;
		} else if (viewType == 2) {
			view = LayoutInflater.from(mContext).inflate(R.layout.adapter_grid3, parent, false);
			itemViewHolder = new ItemViewHolder(mContext, view, listener);
			return itemViewHolder;
		} else {
			view = LayoutInflater.from(mContext).inflate(R.layout.adapter_list, parent, false);
			itemViewHolder = new ItemViewHolder(mContext, view, listener);
			return itemViewHolder;
		}
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
		ItemViewHolder holder;
		HeaderViewHolder headerViewHolder;
		final QueueItem item = this.getItem(position);


		if (item != null) {
			switch (item.type) {
				case MyApplication.TRACKHEADER:
					headerViewHolder = (HeaderViewHolder) viewHolder;
					headerViewHolder.sectiontitle.setText(MyApplication.TRACKS);
					break;
				case MyApplication.ALBUMHEADER:
					headerViewHolder = (HeaderViewHolder) viewHolder;
					headerViewHolder.sectiontitle.setText(MyApplication.ALBUMS);
					break;
				default:
					holder = (ItemViewHolder) viewHolder;
					holder.queueItem = item;

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && holder.getItemViewType() != GRID1) {
						holder.imageThumb.setTransitionName(mContext.getString(R.string.transition_name_coverart) + position);
					}
					holder.imageThumb.setVisibility(View.VISIBLE);
					holder.textTitle.setText(item.title);
					if (type == PlayerConstants.QUEUE_TYPE_ARTISTS || type == PlayerConstants.QUEUE_TYPE_ALBUMS || type == PlayerConstants.QUEUE_TYPE_GENRES || type == PlayerConstants.QUEUE_TYPE_PLAYLIST) {
						holder.textDesc.setText(String.format("%d song%s", item.songs, item.songs != 1 ? "s" : ""));

					} else if (type == PlayerConstants.QUEUE_TYPE_ACTIVITY) {
						holder.textDesc.setText(item.songs == 1 ? "Once" : String.format("%d time%s", item.songs, item.songs != 1 ? "s" : ""));
					} else if (type == PlayerConstants.QUEUE_TYPE_FOLDERS) {
						if (item.folder) {
							if (item.title.equals(".") || item.title.equals(".."))
								holder.textDesc.setText("");
							else
								holder.textDesc.setText("");
//						holder.textDesc.setText(String.format("%d song%s", item.songs, item.songs != 1 ? "s" : ""));
						} else {
							holder.textDesc.setText(item.artist_name);
						}
					} else {
						holder.textDesc.setText(item.artist_name);
					}

					holder.viewOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);

					if (type == PlayerConstants.QUEUE_TYPE_ALBUMS || type == PlayerConstants.QUEUE_TYPE_ARTISTS || type == PlayerConstants.QUEUE_TYPE_GENRES)
						holder.layoutMenu.setVisibility(View.VISIBLE);
					else if (type == PlayerConstants.QUEUE_TYPE_FOLDERS)
						holder.layoutMenu.setVisibility((isMultiSelect() || item.folder) ? View.GONE : View.VISIBLE);
					else
						holder.layoutMenu.setVisibility(isMultiSelect() ? View.GONE : View.VISIBLE);

					if (item.type.equals(MyApplication.TRACKS)) {
						holder.textTitle.setTypeface(null, Typeface.NORMAL);

						if (type == PlayerConstants.QUEUE_TYPE_QUEUE || type == PlayerConstants.QUEUE_TYPE_FAVORITES || type == PlayerConstants.QUEUE_TYPE_PLAYLIST_SONGS || type == PlayerConstants.QUEUE_TYPE_PLAYLIST)
							holder.imageGrabber.setVisibility(isMultiSelect() ? View.GONE : View.VISIBLE);
						else
							holder.imageGrabber.setVisibility(View.GONE);

						holder.imageState.setVisibility((type == PlayerConstants.QUEUE_TYPE_QUEUE ? View.VISIBLE : View.INVISIBLE));

						boolean show = false;
						if (type == PlayerConstants.QUEUE_TYPE_QUEUE) {
							if (item.hash.equals(PlayerConstants.QUEUE_SONG.hash)) {
								holder.textTitle.setTypeface(null, Typeface.BOLD);
								if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
									show = true;
								}
							}
						}

						holder.imageState.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

						if (type == PlayerConstants.QUEUE_TYPE_PLAYLIST) {
							String playlistdesc = String.format("%d song%s", item.songs, item.songs != 1 ? "s" : "") + "/" + Utils.getDuration(Utils.getInt(item.duration, 0));
							holder.textDesc.setText(playlistdesc);
						}
					} else {
						if (type == PlayerConstants.QUEUE_TYPE_ARTISTS || type == PlayerConstants.QUEUE_TYPE_ALBUMS || type == PlayerConstants.QUEUE_TYPE_GENRES || type == PlayerConstants.QUEUE_TYPE_FOLDERS)
							holder.textInfo.setText("");
						else
							holder.textInfo.setText(Utils.getDuration(Utils.getInt(item.duration, 0)));
					}

					if (type == PlayerConstants.QUEUE_TYPE_FOLDERS && item.folder) {


						holder.imageThumb.setImageResource(R.drawable.folder_drawable);   //.setImageDrawable(mContext.getResources().getDrawable(R.drawable.folder_drawable));
					} else {
						if (item.type.equals(MyApplication.TRACKS)) {
							holder.imageThumb.setVisibility(View.GONE);
						} else {
//							MyApplication.loadImage/SmallFresco(item, holder.imageThumb);
                            ImageManager.Instance().loadImageList(item, holder.imageThumb, tag);
                        }
					}
			}
		}
	}

    @Override
    public int getItemViewType(int position) {

        switch (items.get(position).type) {
            case MyApplication.TRACKHEADER:
                return 1;
            case MyApplication.ALBUMHEADER:
                return 1;
            case MyApplication.ALBUMS:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position).id;
        else
            return -1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

	public int getGridtype() {
		return gridtype;
	}

	public void setGridtype(int gridtype) {
		this.gridtype = gridtype;
	}

	public void notifyRemoveEach() {
		for (int i = 0; i < items.size(); i++) {
			notifyItemRemoved(i);
		}
	}

	public void notifyAddEach() {
		for (int i = 0; i < items.size(); i++) {
			notifyItemInserted(i);
		}
	}

	private QueueItem getItem(int position) {
		if (position >= 0 && position < items.size())
			return items.get(position);
		else
			return null;
	}

	public ArrayList<QueueItem> getSelectedItems() {
		ArrayList<QueueItem> selection = new ArrayList<>();

		List<Integer> indexes = getSelectedIndexes();
		for (Integer intr : indexes) {
			selection.add(getItem(intr));
		}
		indexes.clear();

		return selection;
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new CustomFilter();
		}
		return filter;
	}

	public boolean moveTo(int from, int to) {
		boolean ret = false;
		try {
			if (!items.isEmpty()) {
				Collections.swap(items, from, to);
				notifyItemMoved(from, to);


				QueueItem toqueueItem = getItem(to);
				toqueueItem.order = to;
				items.set(to, toqueueItem);

				QueueItem fromqueueItem = getItem(from);
				fromqueueItem.order = from;
				items.set(from, fromqueueItem);
				/*
				QueueItem item = items.get(from);
                items.remove(item);
                items.add(to, item);
                */

				ret = true;
			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}

		return ret;
	}

	public void removeIndex(int position) {
		if (position >= 0 && position < items.size()) {
			items.remove(position);
			notifyItemRemoved(position);
		}

	}

	private void sort() {
		if (sortType == null)
			notifyDataSetChanged();

		else if (sortType.equals("sortTrackLowest"))
			sortTrackLowest();
		else if (sortType.equals("sortTrackHighest"))
			sortTrackHighest();

		else if (sortType.equals("sortTitleLowest"))
			sortTitleLowest();
		else if (sortType.equals("sortTitleHighest"))
			sortTitleHighest();

		else if (sortType.equals("sortFilenameLowest"))
			sortFilenameLowest();
		else if (sortType.equals("sortFilenameHighest"))
			sortFilenameHighest();

		else if (sortType.equals("sortAlbumLowest"))
			sortAlbumLowest();
		else if (sortType.equals("sortAlbumHighest"))
			sortAlbumHighest();

		else if (sortType.equals("sortAlbumLowest"))
			sortAlbumLowest();
		else if (sortType.equals("sortAlbumHighest"))
			sortAlbumHighest();

		else if (sortType.equals("sortArtistLowest"))
			sortArtistLowest();
		else if (sortType.equals("sortArtistHighest"))
			sortArtistHighest();

		else if (sortType.equals("sortDurationSmallest"))
			sortDurationSmallest();
		else if (sortType.equals("sortDurationLargest"))
			sortDurationLargest();

		else if (sortType.equals("sortYearEarliest"))
			sortYearEarliest();
		else if (sortType.equals("sortYearLatest"))
			sortYearLatest();

		else if (sortType.equals("sortDateEarliest"))
			sortDateEarliest();
		else if (sortType.equals("sortDateLatest"))
			sortDateLatest();

		else if (sortType.equals("sortSongsLowest"))
			sortSongsLowest();
		else if (sortType.equals("sortSongsHighest"))
			sortSongsHighest();

		else if (sortType.equals("sortPlaylistLowest"))
			sortPlaylistLowest();
		else if (sortType.equals("sortPlaylistHighest"))
			sortplaylistHighest();
		else if (sortType.equals("sortRatingLowest"))
			sortRatingLowest();
		else if (sortType.equals("sortRatingHighest"))
			sortRatingHighest();

		else
			notifyDataSetChanged();
	}

	private void getandremoveTracks() {
		sort.clear();
		nosort.clear();

		for (int i = tracksFirstPosition + 1; i < items.size(); i++) {
			sort.add(items.get(i));
		}


		for (int i = 0; i <= tracksFirstPosition; i++) {
			nosort.add(items.get(i));
		}
	}

	public void sortTrackLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.track - s2.track);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
		notifyDataSetChanged();

		sortType = "sortTrackLowest";
	}

	public void sortTrackHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.track - s1.track);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();
		sortType = "sortTrackHighest";
	}

	public void sortTitleLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.title.compareToIgnoreCase(s2.title));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortTitleLowest";
	}

	public void sortTitleHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.title.compareToIgnoreCase(s1.title));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortTitleHighest";
	}

	public void sortFilenameLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> {

			String filename1 = new File(s1.data).getName();
			String filename2 = new File(s2.data).getName();
			return filename1.compareTo(filename2);
		});

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
		notifyDataSetChanged();

		sortType = "sortFilenameLowest";
	}

	public void sortFilenameHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> {

			String filename1 = new File(s1.data).getName();
			String filename2 = new File(s2.data).getName();
			return filename2.compareTo(filename1);
		});

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();
		sortType = "sortFilenameHighest";
	}

	public void sortAlbumLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.album_name.compareTo(s2.album_name));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
		notifyDataSetChanged();

		sortType = "sortAlbumLowest";
	}

	public void sortAlbumHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.album_name.compareTo(s1.album_name));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);


		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();
		sortType = "sortAlbumHighest";
	}

	public void sortArtistLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.artist_name.compareTo(s2.artist_name));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);


		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
		notifyDataSetChanged();

		sortType = "sortArtistLowest";
	}

	private void sortArtistHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.artist_name.compareTo(s1.artist_name));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();
		sortType = "sortArtistHighest";
	}

	public void sortDurationSmallest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> Integer.valueOf(s1.duration).compareTo(Integer.valueOf(s2.duration)));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortDurationSmallest";
	}

	public void sortDurationLargest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> Integer.valueOf(s2.duration).compareTo(Integer.valueOf(s1.duration)));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		notifyDataSetChanged();
		sortType = "sortDurationLargest";

	}


	public void sortYearEarliest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.year - s2.year);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
		notifyDataSetChanged();

		sortType = "sortYearEarliest";
	}

	public void sortYearLatest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.year - s1.year);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();
		sortType = "sortYearLatest";
	}

	public void sortDateEarliest() {

		getandremoveTracks();


		Collections.sort(sort, (s1, s2) -> s2.date.compareTo(s1.date));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();
		sortType = "sortDateEarliest";
	}

	public void sortDateLatest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.date.compareTo(s2.date));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
		notifyDataSetChanged();

		sortType = "sortDateLatest";
	}

	private void sortSongsLowest() {
		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.songs - s2.songs);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortSongsLowest";
	}

	private void sortSongsHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.songs - s1.songs);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortSongsHighest";
	}


	private void sortPlaylistLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> (int) (s1.order - s2.order));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);


		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortPlaylistLowest";
	}

	private void sortplaylistHighest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> (int) (s2.order - s1.order));

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortPlaylistHighest";
	}

	public void sortRatingLowest() {

		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s1.rating - s2.rating);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortRatingLowest";
	}

	public void sortRatingHighest() {
		getandremoveTracks();

		Collections.sort(sort, (s1, s2) -> s2.rating - s1.rating);

		items.clear();
		items.addAll(nosort);
		items.addAll(sort);

		if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

		notifyDataSetChanged();

		sortType = "sortRatingHighest";
	}

	public void update() {
		if (search.length() > 0)
			getFilter().filter(search);
		else
			notifyDataSetChanged();
	}

	public void search(String chars) {
		search = chars;
		getFilter().filter(chars);
	}

	public void setStorage(int storage) {
		this.storage = storage;
//        notifyDataSetChanged();
	}

	public int getstorage() {
		return storage;
	}

	public ArrayList<QueueItem> getList() {
		return items;
	}


	public ArrayList<QueueItem> getTrackList() {
		ArrayList<QueueItem> tracks = new ArrayList<>();

		for (int i = tracksFirstPosition + 1; i < items.size(); i++) {
			tracks.add(items.get(i));
		}

		return tracks;
	}

	public void add(Collection<QueueItem> list) {
		items.clear();
		items.addAll(list);

		sort();
	}

	public void removeAll(ArrayList<QueueItem> del) {
		items.removeAll(del);
		notifyDataSetChanged();
	}

	public void reset() {
		items.clear();
		notifyDataSetChanged();
	}

	public void set(QueueItem item) {
		if (item != null) {
			for (int i = 0; i < getItemCount(); i++) {
				if (items.get(i) != null && items.get(i).data != null && items.get(i).data.equals(item.data)) {
					items.set(i, item);
					notifyItemChanged(i);
				}
			}
		}
		notifyDataSetChanged();
	}

	public void put(int index, QueueItem item) {
		items.set(index, item);
		notifyItemChanged(index);
	}

	@NonNull
	@Override
	public String getSectionName(int position) {

		String s = items.get(position).title;
		if (s == null) {
			return lastSectionName;
		}
		if (s.length() == 0) {
			return lastSectionName;
		}
		boolean hasNonAlpha = s.matches("^.*[^a-zA-Z0-9 ].*$");

		if (hasNonAlpha) {
			return lastSectionName;
		} else {
			lastSectionName = s.substring(0, 1).toUpperCase();
			return s.substring(0, 1).toUpperCase();
		}
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		final LinearLayout layoutMain;
		final LinearLayout layoutMenu;
		final View viewOverlay;
		final RoundedImageView imageThumb;
		final TextView textTitle;
		final TextView textDesc;
		final TextView textInfo;
		final ImageView imageGrabber;
		final ImageView imageMenu;
		final ImageView imageState;
		final TextView sectiontitle;
		final SearchRecyclerListener listener;
		private final Context context;
		QueueItem queueItem;

		public ItemViewHolder(Context context, final View view, final SearchRecyclerListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            layoutMain = view.findViewById(R.id.layoutMain);
            layoutMenu = view.findViewById(R.id.layoutMenu);
            viewOverlay = view.findViewById(R.id.viewOverlay);

            imageThumb = view.findViewById(R.id.imageThumb);

            textTitle = view.findViewById(R.id.textTitle);
            textDesc = view.findViewById(R.id.textDesc);
            textInfo = view.findViewById(R.id.textInfo);


            imageGrabber = view.findViewById(R.id.imageGrabber);
            imageState = view.findViewById(R.id.imageState);
            imageMenu = view.findViewById(R.id.imageMenu);

            sectiontitle = view.findViewById(R.id.sectiontitle);

			if (layoutMenu != null) {
				layoutMenu.setOnClickListener(this);
			}

			layoutMain.setOnClickListener(this);
			layoutMain.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (listener != null) {
				if (view == layoutMenu)
					listener.onItemMenuClicked(queueItem, getAdapterPosition());
				else
					listener.onItemClicked(queueItem, getAdapterPosition());
			}
		}

		@Override
		public boolean onLongClick(View view) {
			return listener != null && listener.onItemLongClicked(getAdapterPosition());
		}
	}

	private class CustomFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults result = new FilterResults();
			if (constraint != null && constraint.toString().length() > 0) {

				String needle = constraint.toString().toLowerCase();
				//String needles[] = constraint.toString().toLowerCase().split(" ");

				ArrayList<QueueItem> filterList = new ArrayList<>();
				ArrayList<QueueItem> tracklist = new ArrayList<>();
				ArrayList<QueueItem> artistlist = new ArrayList<>();
				ArrayList<QueueItem> albumlist = new ArrayList<>();
				String title;
				String artist;
				String album;
				int trackCount = 0;
				int artistCount = 0;
				int albumCount = 0;
				int next = 0;

				ArrayList<Integer> trackAlready = new ArrayList<>();
				ArrayList<Integer> artistAlready = new ArrayList<>();
				ArrayList<Integer> albumAlready = new ArrayList<>();

				for (QueueItem queueItem : filts) {
					if (queueItem.type.equals(MyApplication.TRACKS)) {
						tracklist.add(queueItem);
					}

					if (queueItem.type.equals(MyApplication.ARTISTS)) {
						artistlist.add(queueItem);
					}

					if (queueItem.type.equals(MyApplication.ALBUMS)) {
						albumlist.add(queueItem);
					}

				}


				for (int i = 0; i < tracklist.size(); i++) {

					if (tracklist.get(i).title.length() > 0 && Utils.searchStringStartsWith(needle, tracklist.get(i).title)) {
						filterList.add(tracklist.get(i));
						trackCount++;
						trackAlready.add(i);
					} else if (tracklist.get(i).artist_name.length() > 0 && Utils.searchStringStartsWith(needle, tracklist.get(i).artist_name)) {
						filterList.add(tracklist.get(i));
						trackCount++;
						trackAlready.add(i);
					} else if (tracklist.get(i).album_name.length() > 0 && Utils.searchStringStartsWith(needle, tracklist.get(i).album_name)) {
						filterList.add(tracklist.get(i));
						trackCount++;
						trackAlready.add(i);
					}
				}

				for (int i = 0; i < tracklist.size(); i++) {

					for (int has : trackAlready) {
						if (i == has) {
                        }
                    }

					title = tracklist.get(i).title;
					artist = tracklist.get(i).artist_name;
					album = tracklist.get(i).album_name;

					if (title.toLowerCase().contains(needle)) {
						filterList.add(tracklist.get(i));
						trackCount++;
					} else if (artist.toLowerCase().contains(needle)) {
						filterList.add(tracklist.get(i));
						trackCount++;
					} else if (album.toLowerCase().contains(needle)) {
						filterList.add(tracklist.get(i));
						trackCount++;
					}
				}

				if (trackCount > 0) {
					QueueItem trackheader = new QueueItem();
					trackheader.type = MyApplication.TRACKHEADER;
					filterList.add(0, trackheader);
				}

				next = filterList.size();

				for (int i = 0; i < artistlist.size(); i++) {

					if (artistlist.get(i).title.length() > 0 && Utils.searchStringStartsWith(needle, artistlist.get(i).title)) {
						filterList.add(artistlist.get(i));
						artistCount++;
						artistAlready.add(i);
					} else if (artistlist.get(i).artist_name.length() > 0 && Utils.searchStringStartsWith(needle, artistlist.get(i).artist_name)) {
						filterList.add(artistlist.get(i));
						artistCount++;
						artistAlready.add(i);
					} else if (artistlist.get(i).album_name.length() > 0 && Utils.searchStringStartsWith(needle, artistlist.get(i).album_name)) {
						filterList.add(artistlist.get(i));
						artistCount++;
						artistAlready.add(i);
					}
				}

				for (int i = 0; i < artistlist.size(); i++) {

					for (int has : artistAlready) {
						if (i == has) {
                        }
                    }

					title = artistlist.get(i).title;
					artist = artistlist.get(i).artist_name;
					album = artistlist.get(i).album_name;

					if (title.toLowerCase().contains(needle)) {
						filterList.add(artistlist.get(i));
						artistCount++;
					} else if (artist.toLowerCase().contains(needle)) {
						filterList.add(artistlist.get(i));
						artistCount++;
					} else if (album.toLowerCase().contains(needle)) {
						filterList.add(artistlist.get(i));
						artistCount++;
					}
				}

				if (artistCount > 0) {
					QueueItem artistheader = new QueueItem();
					artistheader.type = MyApplication.ARTISTHEADER;
					filterList.add(next, artistheader);
				}

				next = filterList.size();

				for (int i = 0; i < albumlist.size(); i++) {

					if (albumlist.get(i).title.length() > 0 && Utils.searchStringStartsWith(needle, albumlist.get(i).title)) {
						filterList.add(albumlist.get(i));
						albumCount++;
						albumAlready.add(i);
					} else if (albumlist.get(i).artist_name.length() > 0 && Utils.searchStringStartsWith(needle, albumlist.get(i).artist_name)) {
						filterList.add(albumlist.get(i));
						albumCount++;
						albumAlready.add(i);
					} else if (albumlist.get(i).album_name.length() > 0 && Utils.searchStringStartsWith(needle, albumlist.get(i).album_name)) {
						filterList.add(albumlist.get(i));
						albumCount++;
						albumAlready.add(i);
					}
				}

				for (int i = 0; i < albumlist.size(); i++) {

					for (int has : albumAlready) {
						if (i == has) {
                        }
                    }

					title = albumlist.get(i).title;
					artist = albumlist.get(i).artist_name;
					album = albumlist.get(i).album_name;

					if (title.toLowerCase().contains(needle)) {
						filterList.add(albumlist.get(i));
						albumCount++;
					} else if (artist.toLowerCase().contains(needle)) {
						filterList.add(albumlist.get(i));
						albumCount++;
					} else if (album.toLowerCase().contains(needle)) {
						filterList.add(albumlist.get(i));
						albumCount++;
					}
				}

				if (albumCount > 0) {
					QueueItem albumheader = new QueueItem();
					albumheader.type = MyApplication.ALBUMHEADER;
					filterList.add(next, albumheader);
				}

				result.count = filterList.size();
				result.values = filterList;
				setFilterCount(filterList.size());

			} else {
				result.count = filts.size();
				result.values = filts;
				setFilterCount(filts.size());
			}

			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			items = (ArrayList<QueueItem>) results.values;
			notifyDataSetChanged();
			listener.onLoaded();
		}
	}

	public class HeaderViewHolder extends RecyclerView.ViewHolder {
		final TextView sectiontitle;
		private final Context context;

		public HeaderViewHolder(Context context, final View view) {
			super(view);

			this.context = context;

            sectiontitle = view.findViewById(R.id.sectiontitle);

		}
	}

}
