package com.muziko.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.PlayerConstants;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Bradley on 9/03/2017.
 */

public class SongsAdapter extends WearableRecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private ArrayList<QueueItem> data;
    private Context context;
    private ItemSelectedListener itemSelectedListener;

    public SongsAdapter(Context context, ArrayList<QueueItem> data) {
        this.context = context;
        this.data = data;
    }

    public void setListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_songs_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SongsAdapter.ViewHolder holder, final int position) {
        if (data != null && !data.isEmpty()) {
            holder.trackTitle.setText(data.get(position).getTitle());

            holder.durationtext.setText(Utils.getDuration(Long.parseLong(data.get(position).getDuration())));

            holder.imageView.setVisibility(View.INVISIBLE);
            holder.overlay.setVisibility(View.GONE);

            if (position == PlayerConstants.QUEUE_INDEX && PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.overlay.setVisibility(View.VISIBLE);
            }

            holder.bind(position, itemSelectedListener);
        }
    }

    @Override
    public int getItemCount() {
        if (data != null && !data.isEmpty()) {
            return data.size();
        }
        return 0;
    }

    public interface ItemSelectedListener {
        void onItemClicked(int position);

        void onItemLongClicked(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView trackTitle;
        private TextView durationtext;
        private View overlay;

        ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.item_image);
            trackTitle = (TextView) view.findViewById(R.id.trackTitle);
            durationtext = (TextView) view.findViewById(R.id.durationtext);
            overlay = view.findViewById(R.id.overlay);
        }

        void bind(final int position, final ItemSelectedListener listener) {

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onItemClicked(position);
                }
            });
            itemView.setOnLongClickListener((View view) -> {
                if (listener != null) {
                    listener.onItemLongClicked(position);
                }
                return true;
            });
        }
    }
}
