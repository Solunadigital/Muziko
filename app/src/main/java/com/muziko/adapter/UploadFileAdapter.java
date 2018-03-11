package com.muziko.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.makeramen.roundedimageview.RoundedImageView;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.database.StorageFolderRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.ImageManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static com.muziko.MyApplication.IMAGE_SMALL_SIZE;

public class UploadFileAdapter extends SelectableAdapter<UploadFileAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
    private final int GRID1 = 0;
    private final int GRID2 = 1;
    private final int GRID3 = 2;
    private final int GRID4 = 3;
    private final int type;
    private final Context mContext;
    private final ArrayList<QueueItem> filts;
    private final RecyclerItemListener listener;
    private String tag;
    private String lastSectionName = "A";
    private int storage;
    private boolean showArtwork;
    private int gridtype = 0;
    private CustomFilter filter;
    private ArrayList<QueueItem> items;
    private String search = "";
    private String sortType = null;
    private int lastAnimatedPosition = -1;
    private CloudManager.StorageMode storageMode = CloudManager.StorageMode.DRIVES;
    private boolean showHidden;

    public UploadFileAdapter(Context context, ArrayList<QueueItem> listData, int type, boolean showArtwork, String tag, RecyclerItemListener listener) {
        super();
        this.mContext = context;
        this.items = listData;
        this.filts = listData;
        this.type = type;
        this.showArtwork = showArtwork;
        this.tag = tag;
        this.listener = listener;

    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public CloudManager.StorageMode getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(CloudManager.StorageMode storageMode) {
        this.storageMode = storageMode;
    }

    public boolean isShowArtwork() {
        return showArtwork;
    }

    public void setShowArtwork(boolean showArtwork) {
        this.showArtwork = showArtwork;
    }


    @Override
    public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(mContext).inflate(R.layout.empty_row, parent, false);
                break;
            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_upload_files_item, parent, false);
                break;
        }
        return new AdapterQueueHolder(mContext, view, viewType, listener);
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

        final QueueItem item = this.getItem(position);
        if (item != null) {
            if (!showHidden && StorageFolderRealmHelper.getFolder(item.storage, item.data).isHiddenFolder()) {
                return;
            }
            holder.imageThumb.setVisibility(View.VISIBLE);
            holder.textTitle.setText(item.title);

            holder.imageState.setVisibility(View.GONE);
            holder.viewOverlay.setVisibility(GONE);
            holder.imageGrabber.setVisibility(GONE);

            if (item.order == R.id.drives) {
                switch (storageMode) {
                    case DRIVES:
                        switch ((int) item.album) {
                            case CloudManager.GOOGLEDRIVE:
                                holder.layoutMenu.setVisibility(View.GONE);
                                Picasso.with(mContext)
                                        .load(R.drawable.ic_google_drive)
                                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                                        .resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
                                        .centerCrop()
                                        .into(holder.imageThumb);
                                holder.textDesc.setText(mContext.getString(R.string.google_drive));
                                break;

                            case CloudManager.DROPBOX:
                                holder.layoutMenu.setVisibility(View.GONE);
                                Picasso.with(mContext)
                                        .load(R.drawable.ic_dropbox)
                                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                                        .resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
                                        .centerCrop()
                                        .into(holder.imageThumb);
                                holder.textDesc.setText(mContext.getString(R.string.dropbox));
                                break;

                            case CloudManager.BOX:
                                holder.layoutMenu.setVisibility(View.GONE);
                                Picasso.with(mContext)
                                        .load(R.drawable.ic_box)
                                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                                        .resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
                                        .centerCrop()
                                        .into(holder.imageThumb);
                                holder.textDesc.setText(mContext.getString(R.string.box));
                                break;

                            case CloudManager.ONEDRIVE:
                                holder.layoutMenu.setVisibility(View.GONE);
                                Picasso.with(mContext)
                                        .load(R.drawable.ic_onedrive)
                                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                                        .resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
                                        .centerCrop()
                                        .into(holder.imageThumb);
                                holder.textDesc.setText(mContext.getString(R.string.onedrive));
                                break;

                            case CloudManager.AMAZON:
                                holder.layoutMenu.setVisibility(View.GONE);
                                Picasso.with(mContext)
                                        .load(R.drawable.ic_amazon)
                                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                                        .resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
                                        .centerCrop()
                                        .into(holder.imageThumb);
                                holder.textDesc.setText(mContext.getString(R.string.onedrive));
                                break;

                            case CloudManager.LOCAL:
                                holder.textTitle.setText("Local");
                                holder.layoutMenu.setVisibility(GONE);
                                Picasso.with(mContext)
                                        .load(R.drawable.drives)
                                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                                        .resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
                                        .centerCrop()
                                        .into(holder.imageThumb);
                                holder.textDesc.setText("");
                                break;
                        }
                        break;
                    default:
                        holder.layoutMenu.setVisibility(View.GONE);
                        if (item.folder) {
                            holder.textDesc.setText("");
                            holder.imageThumb.setImageResource(R.drawable.folder_icon);
                        } else {
                            holder.textDesc.setText(item.artist_name);
                            if (showArtwork) {
                                ImageManager.Instance().loadImageListSmall(item, holder.imageThumb, tag);
                            } else {
                                holder.imageThumb.setVisibility(GONE);
                            }
                        }
                        break;
                }
            } else {
                holder.layoutMenu.setVisibility(View.GONE);
                if (item.folder) {
                    if (showHidden) {
                        holder.viewOverlay.setVisibility(StorageFolderRealmHelper.getFolder(item.storage, item.data).isHiddenFolder() ? View.VISIBLE : GONE);
                    }
                    holder.excludedImage.setVisibility(StorageFolderRealmHelper.getFolder(item.storage, item.data).isExcludedFolder() ? View.VISIBLE : View.INVISIBLE);
                    holder.textDesc.setText("");
                    holder.imageThumb.setImageResource(R.drawable.folder_icon);
                } else {
                    holder.textDesc.setText(item.artist_name);
                    if (showArtwork) {
                        ImageManager.Instance().loadImageListSmall(item, holder.imageThumb, tag);
                    } else {
                        holder.imageThumb.setVisibility(GONE);
                    }
                }
            }


        }
    }

    @Override
    public int getItemViewType(int position) {
        final QueueItem item = this.getItem(position);
        if (storageMode != CloudManager.StorageMode.DRIVES && !showHidden && StorageFolderRealmHelper.getFolder(item.storage, item.data).isHiddenFolder()) {
            return 1;
        } else {
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

        Collections.sort(items, (s1, s2) -> s1.title.compareToIgnoreCase(s2.title));

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


    private void sortPlaylistLowest() {
        Collections.sort(items, (s1, s2) -> (int) (s1.order - s2.order));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortPlaylistLowest";
    }

    private void sortplaylistHighest() {
        Collections.sort(items, (s1, s2) -> (int) (s2.order - s1.order));

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "ssortPlaylistHighest";
    }

    private void sortRatingLowest() {
        Collections.sort(items, (s1, s2) -> s1.rating - s2.rating);

        if (type == PlayerConstants.QUEUE_TYPE_QUEUE) AppController.Instance().updateQueueIndex();

        notifyDataSetChanged();

        sortType = "sortRatingLowest";
    }

    private void sortRatingHighest() {
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

    public static class EmptyQueueHolder extends RecyclerView.ViewHolder {
        final View itemView;

        public EmptyQueueHolder(Context context, final View view) {
            super(view);

            itemView = view;
        }
    }


    public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
        View itemView;
        LinearLayout layoutMain;
        LinearLayout layoutMenu;
        View viewOverlay;
        RoundedImageView imageThumb;
        TextView textTitle;
        TextView textDesc;
        TextView textInfo;
        ImageView imageGrabber;
        ImageView imageMenu;
        ImageView imageState;
        ImageView excludedImage;
        RecyclerItemListener listener;
        Context context;

        public AdapterQueueHolder(Context context, final View view, int viewType, final RecyclerItemListener listener) {
            super(view);

            if (viewType == 0) {
                this.context = context;
                this.listener = listener;
                itemView = view;
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
                excludedImage = view.findViewById(R.id.excludedImage);

                if (layoutMenu != null) {
                    layoutMenu.setOnClickListener(this);
                }

                if (imageGrabber != null) {
                    imageGrabber.setOnTouchListener(this);
                }


                layoutMain.setOnClickListener(this);
                layoutMain.setOnLongClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                if (view == layoutMenu)
                    listener.onMenuClicked(getAdapterPosition());
                else
                    listener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return listener != null && listener.onItemLongClicked(getAdapterPosition());
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (listener != null) {
                    listener.onDragTouched(this);
                }
            }
            return false;
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
                    for (int has : already) {
                        if (i == has) {
                        }
                    }
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
