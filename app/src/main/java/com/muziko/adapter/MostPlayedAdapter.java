package com.muziko.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.muziko.R;
import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.common.models.QueueItem;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.MostPlayedItemListener;
import com.muziko.manager.ImageManager;
import com.muziko.manager.PrefsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MostPlayedAdapter extends SelectableAdapter<MostPlayedAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
    private final int GRID1 = 0;
    private final int GRID2 = 1;
    private final int GRID3 = 2;
    private final int GRID4 = 3;
    private final Context mContext;
    private final ArrayList<TrackModel> filts;
    private final MostPlayedItemListener listener;
    private String tag;
    private String lastSectionName = "A";
    private int storage;
    private boolean showArtwork;
    private int gridtype = 0;
    private CustomFilter filter;
    private ArrayList<TrackModel> items;
    private String search = "";
    private String sortType = null;
    private int lastAnimatedPosition = -1;

    public MostPlayedAdapter(Context context, ArrayList<TrackModel> listData, boolean showArtwork, String tag, MostPlayedItemListener listener) {
        super();
        this.mContext = context;
        this.items = listData;
        this.filts = listData;
        this.showArtwork = showArtwork;
        this.tag = tag;
        this.listener = listener;
        setHasStableIds(true);
    }

    public boolean isShowArtwork() {
        return showArtwork;
    }

    public void setShowArtwork(boolean showArtwork) {
        this.showArtwork = showArtwork;
    }


    @Override
    public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case GRID1:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_mostplayed_item, parent, false);
                break;
            case GRID2:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_mostplayed_grid2, parent, false);
                break;
            case GRID3:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_mostplayed_grid3, parent, false);
                break;
            case GRID4:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_mostplayed_grid4, parent, false);
                break;

            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.adapter_mostplayed_item, parent, false);
        }

        return new AdapterQueueHolder(mContext, view, listener);
    }

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {


        final TrackModel item = this.getItem(position);
        if (item != null) {
            holder.gotoImage.setVisibility(View.GONE);
            holder.imageMenu.setVisibility(View.GONE);
            holder.imageYoutube.setVisibility(View.INVISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && holder.getItemViewType() != GRID1) {
                holder.imageThumb.setTransitionName(mContext.getString(R.string.transition_name_coverart) + position);
            }
            holder.imageThumb.setVisibility(View.VISIBLE);
            holder.textTitle.setText(item.title);
            holder.textDesc.setText(item.artist_name);

            holder.textInfo.setText("Played " + item.getPlayed() + " times");

            if (TextUtils.isEmpty(item.data)) {
                holder.gotoImage.setVisibility(View.VISIBLE);
                holder.imageMenu.setVisibility(View.GONE);
            } else {
                holder.gotoImage.setVisibility(View.GONE);
                holder.imageMenu.setVisibility(View.VISIBLE);
            }

            holder.imageYoutube.setVisibility(View.VISIBLE);

            if (gridtype == GRID1) {
                holder.textTitle.setTypeface(null, Typeface.NORMAL);
                ImageManager.Instance().loadImageListSmall(item, holder.imageThumb, tag);


            } else {

                ImageManager.Instance().loadImageList(item, holder.imageThumb, tag);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (tag.equals("com.muziko.activities.FavouritesActivity")) {
            return PrefsManager.Instance().getFavouriteViewType();
        } else {

            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position).hashCode();
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

    public TrackModel getItem(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position);
        else
            return null;
    }

    public ArrayList<TrackModel> getSelectedItems() {
        ArrayList<TrackModel> selection = new ArrayList<>();

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

    public void removeIndex(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }

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

    public ArrayList<TrackModel> getList() {
        return items;
    }

    public void add(Collection<TrackModel> list) {
        items.clear();
        items.addAll(list);

    }

    public void removeAll(ArrayList<QueueItem> del) {
        items.removeAll(del);
        notifyDataSetChanged();
    }

    public void reset() {
        items.clear();
        notifyDataSetChanged();
    }

    public void set(TrackModel item) {
        if (item != null) {
            for (int i = 0; i < getItemCount(); i++) {
                if (items.get(i) != null && items.get(i).getAcrid() != null && items.get(i).getAcrid().equals(item.getAcrid())) {
                    items.set(i, item);
                    notifyItemChanged(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void put(int index, TrackModel item) {
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

    public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final LinearLayout layoutMain;
        final LinearLayout layoutMenu;
        final View viewOverlay;
        final RoundedImageView imageThumb;
        final TextView textTitle;
        final TextView textDesc;
        final TextView textInfo;
        final ImageView imageMenu;
        final ImageView gotoImage;
        final ImageView imageYoutube;
        final MostPlayedItemListener listener;
        private final Context context;

        public AdapterQueueHolder(Context context, final View view, final MostPlayedItemListener listener) {
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

            gotoImage = view.findViewById(R.id.gotoImage);
            imageYoutube = view.findViewById(R.id.imageYoutube);
            imageMenu = view.findViewById(R.id.imageMenu);

            if (layoutMenu != null) {
                layoutMenu.setOnClickListener(this);
            }

            if (gotoImage != null) {
                gotoImage.setOnClickListener(this);
            }

            if (imageYoutube != null) {
                imageYoutube.setOnClickListener(this);
            }

            layoutMain.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                if (view == layoutMenu) {
                    listener.onMenuClicked(getAdapterPosition());
                } else if (view == gotoImage) {
                    listener.onGotoClicked(getAdapterPosition());
                } else if (view == imageYoutube) {
                    listener.onYoutubeClicked(getAdapterPosition());
                } else {
                    listener.onItemClicked(getAdapterPosition());
                }
            }
        }
    }

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            if (constraint != null && constraint.toString().length() > 0) {

                String needle = constraint.toString().toLowerCase();
                //String needles[] = constraint.toString().toLowerCase().split(" ");

                ArrayList<TrackModel> filterList = new ArrayList<>();
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
            items = (ArrayList<TrackModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
