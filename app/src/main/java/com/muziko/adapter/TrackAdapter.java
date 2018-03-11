package com.muziko.adapter;

import android.content.Context;
import android.graphics.Typeface;
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

import com.andexert.library.RippleView;
import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.eralp.circleprogressview.CircleProgressView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.SortHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.TrackRecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.ImageManager;
import com.muziko.mediaplayer.PlayerConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

import static com.muziko.MyApplication.showArtwork;

/**
 * Created by dev on 5/08/2016.
 */
public class TrackAdapter extends SelectableAdapter<TrackAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
    private final int GRID1 = 0;
    private final int GRID2 = 1;
    private final int GRID3 = 2;
    private final int GRID4 = 3;
    private final int type;
    private final Context mContext;
    private final ArrayList<QueueItem> filts;
    private final TrackRecyclerItemListener listener;
    private String mtag;
    private int storage;
    private int gridtype = 0;
    private CustomFilter filter;
    private ArrayList<QueueItem> items;
    private String search = "";
    private String sortType = null;
    private int sortid = 0;
    private int queuePlayingAt = -1;
    private boolean firebaseOverlimit;

    public TrackAdapter(Context context, ArrayList<QueueItem> listData, int type, String tag, TrackRecyclerItemListener listener) {
        super();
        this.mContext = context;
        this.items = listData;
        this.filts = listData;
        this.type = type;
        mtag = tag;
        this.listener = listener;
        firebaseOverlimit = FirebaseManager.Instance().isOverLimit();
        setHasStableIds(true);
    }

    public boolean isFirebaseOverlimit() {
        return firebaseOverlimit;
    }

    public void setFirebaseOverlimit(boolean firebaseOverlimit) {
        this.firebaseOverlimit = firebaseOverlimit;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public int getSortid() {
        return sortid;
    }

    public void setSortid(int sortid) {
        this.sortid = sortid;
    }

    public int getGridtype() {
        return gridtype;
    }

    public void setGridtype(int gridtype) {
        this.gridtype = gridtype;
    }

    public void downloadProgress(String data, int progress) {

        for (int i = 0; i < items.size(); i++) {
            QueueItem queueItem = items.get(i);
            if (queueItem.getData() != null && queueItem.getData().equals(data)) {
                notifyItemChanged(i, new Integer(progress));
            }
        }
    }

    public void updateNowPlaying() {
        if (queuePlayingAt != -1) {
            notifyItemChanged(queuePlayingAt, new Integer(-1));
        }
    }

    public void updateFirebaseOverlimit(boolean overLimit) {
        setFirebaseOverlimit(overLimit);
        notifyDataSetChanged();
    }

    @Override
    public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        setGridtype(viewType);
        switch (viewType) {
            case GRID1:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_track_item, parent, false);
                break;
            case GRID2:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_grid2, parent, false);
                break;
            case GRID3:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_grid3, parent, false);
                break;
            case GRID4:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_grid4, parent, false);
                break;

            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_list, parent, false);
        }

        return new AdapterQueueHolder(mContext, view, listener);
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

        final QueueItem item = items.get(holder.getAdapterPosition());
        if (item != null) {
            holder.imageThumb.setVisibility(View.VISIBLE);
            holder.textTitle.setText(item.title);
            holder.textDesc.setText(item.artist_name);

            holder.viewOverlay.setVisibility(isSelected(holder.getAdapterPosition()) ? View.VISIBLE : View.GONE);
            holder.layoutMenu.setVisibility(isMultiSelect() ? View.GONE : View.VISIBLE);

            if (item.storage == CloudManager.FIREBASE) {
                holder.disabledOverlay.setVisibility(isFirebaseOverlimit() ? View.VISIBLE : View.GONE);
            } else {
                holder.disabledOverlay.setVisibility(View.GONE);
            }

            switch (gridtype) {

                case GRID1:
                    holder.downloadProgress.setVisibility(View.GONE);
                    holder.textTitle.setTypeface(null, Typeface.NORMAL);
                    holder.imageGrabber.setVisibility(View.GONE);
                    if (PlayerConstants.QUEUE_SONG.data.equals(item.data)) {
                        holder.vuMeterView.setVisibility(View.VISIBLE);
                        queuePlayingAt = holder.getAdapterPosition();
                        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
                            holder.vuMeterView.resume(true);
                        } else {
                            holder.vuMeterView.stop(true);
                        }
                    } else {
                        holder.vuMeterView.setVisibility(View.GONE);
                    }
                    if (showArtwork) {
                        ImageManager.Instance().loadImageListSmall(item, holder.imageThumb, mtag);
                    } else {
                        holder.imageThumb.setVisibility(View.GONE);
                    }
                    break;
                default:
                    holder.textInfo.setText(Utils.getDuration(Utils.getInt(item.duration, 0)));
                    ImageManager.Instance().loadImageList(item, holder.imageThumb, mtag);
                    break;
            }
        }
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final QueueItem item = this.getItem(holder.getAdapterPosition());
            if (item == null) {
                return;
            }

            Object progress = payloads.get(0);

            if (gridtype == GRID1) {
                holder.downloadProgress.setVisibility(View.VISIBLE);
                holder.imageMenu.setVisibility(View.GONE);

                if (PlayerConstants.QUEUE_SONG == null) {
                    return;
                }
                if (PlayerConstants.QUEUE_SONG.data.equals(item.data)) {
                    holder.vuMeterView.setVisibility(View.VISIBLE);
                    queuePlayingAt = holder.getAdapterPosition();
                    if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING && !AppController.isBuffering) {
                        holder.vuMeterView.resume(true);
                    } else {
                        holder.vuMeterView.stop(true);
                    }
                } else {
                    holder.vuMeterView.setVisibility(View.GONE);
                }

                if ((int) progress >= 0 && (int) progress < 100) {
                    holder.downloadProgress.setProgress((int) progress);
                } else {
                    holder.downloadProgress.setVisibility(View.GONE);
                    holder.imageMenu.setVisibility(View.VISIBLE);
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {

        return gridtype;
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position).data.hashCode();
        else
            return -1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(QueueItem queueItem) {
        items.add(0, queueItem);
        notifyItemChanged(0);
    }

    public QueueItem getItem(int position) {
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
        else if (sortType.equals("sortRatingLowest"))
            sortRatingLowest();
        else if (sortType.equals("sortRatingHighest"))
            sortRatingHighest();
        else
            notifyDataSetChanged();
    }

    public void sortTrackLowest() {
        Collections.sort(items, (s1, s2) -> s1.track - s2.track);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
        notifyDataSetChanged();

        sortType = "sortTrackLowest";
    }

    public void sortTrackHighest() {
        Collections.sort(items, (s1, s2) -> s2.track - s1.track);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();
        sortType = "sortTrackHighest";
    }

    public void sortTitleLowest() {

        Collections.sort(items, (s1, s2) -> {
            String first = s1.title != null ? s1.title : "";
            String second = s2.title != null ? s2.title : "";
            return first.compareToIgnoreCase(second);
        });

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortTitleLowest";
    }

    public void sortTitleHighest() {
        Collections.sort(items, (s1, s2) -> s2.title.compareToIgnoreCase(s1.title));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortTitleHighest";
    }

    public void sortFilenameLowest() {
        Collections.sort(items, (s1, s2) -> {

            String filename1 = new File(s1.data).getName();
            String filename2 = new File(s2.data).getName();
            return filename1.compareTo(filename2);
        });
        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
        notifyDataSetChanged();

        sortType = "sortFilenameLowest";
    }

    public void sortFilenameHighest() {
        Collections.sort(items, (s1, s2) -> {

            String filename1 = new File(s1.data).getName();
            String filename2 = new File(s2.data).getName();
            return filename2.compareTo(filename1);
        });

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();
        sortType = "sortFilenameHighest";
    }

    public void sortAlbumLowest() {
        Collections.sort(items, (s1, s2) -> s1.album_name.compareTo(s2.album_name));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
        notifyDataSetChanged();

        sortType = "sortAlbumLowest";
    }

    public void sortAlbumHighest() {
        Collections.sort(items, (s1, s2) -> s2.album_name.compareTo(s1.album_name));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();
        sortType = "sortAlbumHighest";
    }

    public void sortArtistLowest() {
        Collections.sort(items, (s1, s2) -> s1.artist_name.compareTo(s2.artist_name));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
        notifyDataSetChanged();

        sortType = "sortArtistLowest";
    }

    private void sortArtistHighest() {
        Collections.sort(items, (s1, s2) -> s2.artist_name.compareTo(s1.artist_name));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();
        sortType = "sortArtistHighest";
    }

    public void sortDurationSmallest() {
        Collections.sort(items, (s1, s2) -> Integer.valueOf(s1.duration).compareTo(Integer.valueOf(s2.duration)));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortDurationSmallest";
    }

    public void sortDurationLargest() {
        Collections.sort(items, (s1, s2) -> Integer.valueOf(s2.duration).compareTo(Integer.valueOf(s1.duration)));


        notifyDataSetChanged();
        sortType = "sortDurationLargest";

    }


    public void sortYearEarliest() {
        Collections.sort(items, (s1, s2) -> s1.year - s2.year);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
        notifyDataSetChanged();

        sortType = "sortYearEarliest";
    }

    public void sortYearLatest() {
        Collections.sort(items, (s1, s2) -> s2.year - s1.year);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();
        sortType = "sortYearLatest";
    }

    public void sortDateEarliest() {
        Collections.sort(items, (s1, s2) -> s2.date.compareTo(s1.date));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();
        sortType = "sortDateEarliest";
    }

    public void sortDateLatest() {
        Collections.sort(items, (s1, s2) -> s1.date.compareTo(s2.date));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();
        notifyDataSetChanged();

        sortType = "sortDateLatest";
    }

    private void sortSongsLowest() {
        Collections.sort(items, (s1, s2) -> s1.songs - s2.songs);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortSongsLowest";
    }

    private void sortSongsHighest() {
        Collections.sort(items, (s1, s2) -> s2.songs - s1.songs);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortSongsHighest";
    }

    public void sortRatingLowest() {
        Collections.sort(items, (s1, s2) -> s1.rating - s2.rating);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortRatingLowest";
    }

    public void sortRatingHighest() {
        Collections.sort(items, (s1, s2) -> s2.rating - s1.rating);

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

    public void queueChanged() {
        if (queuePlayingAt == -1) {
            notifyDataSetChanged();
        } else {
            notifyItemChanged(queuePlayingAt);
            AsyncJob.doInBackground(() -> {
                for (int i = 0; i < items.size(); ++i) {
                    if (items.get(i).data.equals(PlayerConstants.QUEUE_SONG.data)) {
                        int finalPosition = i;
                        AsyncJob.doOnMainThread(() -> notifyItemChanged(finalPosition));
                        break;
                    }
                }
            });
        }
    }

    public int getstorage() {
        return storage;
    }

    public ArrayList<QueueItem> getList() {
        return items;
    }

    public void add(Collection<QueueItem> list) {
        items.clear();
        items.addAll(list);

        sort();
    }


    public void notifyChangeAll() {
        notifyItemRangeChanged(0, items.size());
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

    public void removeAll(ArrayList<QueueItem> del) {
        items.removeAll(del);
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

//    public void updateItems(ArrayList<QueueItem> queueItems) {
//
////        final QueueItemDiffCallback diffCallback = new QueueItemDiffCallback(this.items, queueItems);
////        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
////
////        diffResult.dispatchUpdatesTo(this);
//
//        this.items.clear();
//        this.items.addAll(queueItems);
//        notifyDataSetChanged();
//
//    }

    @NonNull
    @Override
    public String getSectionName(int position) {

        QueueItem queueItem = items.get(position);
        SortHelper sortHelper = new SortHelper();
        return sortHelper.getSectionName(sortid, queueItem);
    }

    public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final LinearLayout layoutMain;
        private final RippleView ripple;
        private final LinearLayout layoutMenu;
        private final View viewOverlay;
        private final RoundedImageView imageThumb;
        private final TextView textTitle;
        private final TextView textDesc;
        private final TextView textInfo;
        private final ImageView imageGrabber;
        private final ImageView imageMenu;
        private final CircleProgressView downloadProgress;
        private final TrackRecyclerItemListener listener;
        private final Context context;
        private final VuMeterView vuMeterView;
        private final View disabledOverlay;

        public AdapterQueueHolder(final Context context, final View view, final TrackRecyclerItemListener listener) {
            super(view);

            this.context = context;
            this.listener = listener;

            ripple = view.findViewById(R.id.ripple);
            layoutMain = view.findViewById(R.id.layoutMain);

            layoutMenu = view.findViewById(R.id.layoutMenu);
            viewOverlay = view.findViewById(R.id.viewOverlay);
            disabledOverlay = view.findViewById(R.id.disabledOverlay);
            imageThumb = view.findViewById(R.id.imageThumb);

            textTitle = view.findViewById(R.id.textTitle);
            textDesc = view.findViewById(R.id.textDesc);
            textInfo = view.findViewById(R.id.textInfo);


            imageGrabber = view.findViewById(R.id.imageGrabber);
            imageMenu = view.findViewById(R.id.imageMenu);
            vuMeterView = view.findViewById(R.id.vuMeterView);
            downloadProgress = view.findViewById(R.id.downloadProgress);

            if (layoutMenu != null) {
                layoutMenu.setOnClickListener(this);
            }


            layoutMain.setOnClickListener(this);
            layoutMain.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                if (v == layoutMenu)
                    listener.onMenuClicked(context, getAdapterPosition());
                else
                    listener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
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
                // starts with
                ArrayList<Integer> already = new ArrayList<>();
                for (int i = 0; i < filts.size(); i++) {

                    if (filts.get(i).title.length() > 0 && Utils.searchStringStartsWith(needle, filts.get(i).title)) {
                        filterList.add(filts.get(i));
                        already.add(i);
                    } else if (filts.get(i).artist_name.length() > 0 && Utils.searchStringStartsWith(needle, filts.get(i).artist_name)) {
                        filterList.add(filts.get(i));
                        already.add(i);
                    }
                }
                // contains
                for (int i = 0; i < filts.size(); i++) {
                    boolean skip = false;
                    for (int has : already) {
                        if (i == has) {
                            skip = true;
                        }
                    }
                    if (skip) continue;
                    if (filts.get(i).title.length() > 0 && Utils.searchStringContains(needle, filts.get(i).title)) {
                        filterList.add(filts.get(i));
                    } else if (filts.get(i).artist_name.length() > 0 && Utils.searchStringContains(needle, filts.get(i).artist_name)) {
                        filterList.add(filts.get(i));
                    }
                }

                result.count = filterList.size();

                result.values = filterList;

            } else {
                result.count = filts.size();

                result.values = filts;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items = (ArrayList<QueueItem>) results.values;
            notifyDataSetChanged();
        }
    }
}
