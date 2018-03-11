package com.muziko.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eralp.circleprogressview.CircleProgressView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.RecyclerItemListener;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.ImageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static com.muziko.MyApplication.showArtwork;

public class LibraryAdapter extends SelectableAdapter<LibraryAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
    private final int GRID1 = 0;
    private final int GRID2 = 1;
    private final int GRID3 = 2;
    private final int GRID4 = 3;
    private final Context mContext;
    private final List<QueueItem> filts;
    private final RecyclerItemListener listener;
    private String tag;
    private String lastSectionName = "A";
    private CustomFilter filter;
    private ArrayList<QueueItem> items;
    private String search = "";
    private String sortType = null;
    private boolean firebaseOverlimit;

    public LibraryAdapter(Context context, ArrayList<QueueItem> listData, String tag, RecyclerItemListener listener) {
        super();
        this.mContext = context;
        this.items = listData;
        this.filts = listData;
        this.tag = tag;
        this.listener = listener;
        firebaseOverlimit = FirebaseManager.Instance().isOverLimit();
    }

    public boolean isFirebaseOverlimit() {
        return firebaseOverlimit;
    }

    public void setFirebaseOverlimit(boolean firebaseOverlimit) {
        this.firebaseOverlimit = firebaseOverlimit;
    }

    public void updateFirebaseOverlimit(boolean overLimit) {
        setFirebaseOverlimit(overLimit);
        notifyDataSetChanged();
    }

    @Override
    public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_library_item, parent, false);


        return new AdapterQueueHolder(mContext, view, listener);
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

        final QueueItem queueItem = this.getItem(position);
        if (queueItem != null) {

            holder.viewOverlay.setVisibility(isSelected(position) ? View.VISIBLE : GONE);
            holder.imageRemote.setVisibility(GONE);
            holder.downloadProgress.setVisibility(GONE);
            holder.imageMenu.setVisibility(View.VISIBLE);
            holder.imageDownload.setVisibility(GONE);

            holder.disabledOverlay.setVisibility(isFirebaseOverlimit() ? View.VISIBLE : View.GONE);

            holder.textTitle.setText(queueItem.getTitle());
            holder.textArtist.setText(queueItem.getArtist_name());
            if (showArtwork) {
                ImageManager.Instance().loadImageListSmall(queueItem, holder.imageThumb, tag);
            } else {
                holder.imageThumb.setVisibility(GONE);
            }

//            if (!FileHelper.fileExists(queueItem)) {
//                holder.imageRemote.setVisibility(View.VISIBLE);
//            }
        }
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final QueueItem queueItem = this.getItem(position);
            Object progress = payloads.get(0);

            holder.imageRemote.setVisibility(GONE);
            holder.downloadProgress.setVisibility(View.VISIBLE);
            holder.imageMenu.setVisibility(GONE);
            holder.imageDownload.setVisibility(GONE);

            if ((int) progress >= 0) {
                holder.downloadProgress.setProgress((int) progress);
            } else {
                holder.downloadProgress.setVisibility(GONE);
                holder.imageDownload.setVisibility(View.VISIBLE);
            }

        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateProgress(String url, int progress) {

        for (int i = 0; i < items.size(); i++) {
            QueueItem queueItem = items.get(i);
            if (queueItem.getData() != null && queueItem.getData().equals(url)) {
                notifyItemChanged(i, new Integer(progress));
            }
        }

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

    public void search(String chars) {
        search = chars;
        getFilter().filter(chars);
    }


    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter();
        }
        return filter;
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

        else if (sortType.equals("sortTitleLowest"))
            sortTitleLowest();
        else if (sortType.equals("sortTitleHighest"))
            sortTitleHighest();
        else if (sortType.equals("sortSentLowest"))
            sortSentLowest();
        else if (sortType.equals("sortSentHighest"))
            sortSentHighest();
        else
            notifyDataSetChanged();
    }

    public void sortTitleLowest() {

        Collections.sort(items, (s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));

        notifyDataSetChanged();

        sortType = "sortTitleLowest";
    }

    public void sortTitleHighest() {
        Collections.sort(items, (s1, s2) -> s2.getTitle().compareToIgnoreCase(s1.getTitle()));

        notifyDataSetChanged();

        sortType = "sortTitleHighest";
    }

    public void sortSentLowest() {

        Collections.sort(items, (s1, s2) -> {

            Long startTimeStamp = s1.getDate();
            Long endTimeStamp = s2.getDate();

            return startTimeStamp.compareTo(endTimeStamp);
        });

        notifyDataSetChanged();

        sortType = "sortSentLowest";
    }

    public void sortSentHighest() {

        Collections.sort(items, (s1, s2) -> {

            Long startTimeStamp = s1.getDate();
            Long endTimeStamp = s2.getDate();

            return endTimeStamp.compareTo(startTimeStamp);
        });

        notifyDataSetChanged();

        sortType = "sortSentHighest";
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
                if (items.get(i) != null && items.get(i).getTitle() != null && items.get(i).getTitle().equals(item.getTitle())) {
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

        String s = items.get(position).getTitle();
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

    public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final RelativeLayout layoutMain;
        final LinearLayout layoutMenu;
        final TextView textTitle;
        final TextView textArtist;
        final RecyclerItemListener listener;
        final ImageView imageMenu;
        final ImageView imageDownload;
        final ImageView imageRemote;
        final RoundedImageView imageThumb;
        final CircleProgressView downloadProgress;
        private final Context context;
        private final View disabledOverlay;
        private View viewOverlay;

        public AdapterQueueHolder(Context context, final View view, final RecyclerItemListener listener) {
            super(view);

            this.context = context;
            this.listener = listener;

            layoutMain = view.findViewById(R.id.layoutMain);
            layoutMenu = view.findViewById(R.id.layoutMenu);
            viewOverlay = view.findViewById(R.id.viewOverlay);
            disabledOverlay = view.findViewById(R.id.disabledOverlay);

            textTitle = view.findViewById(R.id.textTitle);
            textArtist = view.findViewById(R.id.textArtist);

            imageRemote = view.findViewById(R.id.imageRemote);
            imageDownload = view.findViewById(R.id.imageDownload);
            imageMenu = view.findViewById(R.id.imageMenu);
            imageThumb = view.findViewById(R.id.imageThumb);
            downloadProgress = view.findViewById(R.id.downloadProgress);

            if (imageMenu != null) {
                imageMenu.setOnClickListener(this);
            }

            if (layoutMenu != null) {
                layoutMenu.setOnClickListener(this);
            }

            layoutMain.setOnClickListener(this);
            layoutMain.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                if (view == imageMenu) {
                    listener.onMenuClicked(getAdapterPosition());
                } else {
                    listener.onItemClicked(getAdapterPosition());
                }
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
                // starts with
                ArrayList<Integer> already = new ArrayList<>();
                for (int i = 0; i < filts.size(); i++) {

                    if (filts.get(i).getTitle().length() > 0 && Utils.searchStringStartsWith(needle, filts.get(i).getTitle())) {
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
                    if (filts.get(i).getTitle().length() > 0 && Utils.searchStringContains(needle, filts.get(i).getTitle())) {
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
