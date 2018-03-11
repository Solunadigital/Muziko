package com.muziko.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.interfaces.CoverArtRecyclerListener;
import com.muziko.manager.ImageManager;

import java.util.ArrayList;

/**
 * Created by dev on 14/07/2016.
 */

public class CoverArtAdapter extends RecyclerView.Adapter<CoverArtAdapter.AdapterNowPlayingHolder> {
    private final Context mContext;
    private final ArrayList<QueueItem> queueItems;
    private final CoverArtRecyclerListener listener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CoverArtAdapter(Context context, ArrayList<QueueItem> myDataset, CoverArtRecyclerListener listener) {
        mContext = context;
        queueItems = myDataset;
        this.listener = listener;
        setHasStableIds(true);
    }

    public ArrayList<QueueItem> getList() {
        return queueItems;
    }

    public void add(int position, QueueItem item) {
        queueItems.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(QueueItem item) {
        int position = queueItems.indexOf(item);
        queueItems.remove(position);
        notifyItemRemoved(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterNowPlayingHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // createActivityListener a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_coverart_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new AdapterNowPlayingHolder(mContext, view, listener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final AdapterNowPlayingHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final QueueItem queueItem = queueItems.get(position);

        if (queueItem != null) {

            ImageManager.Instance().loadImageFresco(queueItem, holder.imageCover);
            holder.titleText.setText(queueItems.get(position).title);
            holder.artistText.setText(queueItems.get(position).artist_name);
        }
    }

    @Override
    public long getItemId(int position) {
//		return items.get(position).id;
        return queueItems.get(position).hashCode();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return queueItems.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(int pos, View view, QueueItem data);
    }

    public static class AdapterNowPlayingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final SimpleDraweeView imageCover;
        private final TextView titleText;
        private final TextView artistText;
        private CoverArtRecyclerListener listener;

        public AdapterNowPlayingHolder(final Context context, final View view, final CoverArtRecyclerListener listener) {
            super(view);
            this.listener = listener;
            imageCover = view.findViewById(R.id.imageCover);
            titleText = view.findViewById(R.id.titleText);
            artistText = view.findViewById(R.id.artistText);

            if (imageCover != null) {
                imageCover.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition(), v);
            }
        }
    }

}
