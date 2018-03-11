package com.muziko.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.NowPlayingRecyclerItemListener;
import com.muziko.manager.ImageManager;
import com.muziko.mediaplayer.PlayerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 14/07/2016.
 */

public class NowPlayingAdapter extends RecyclerView.Adapter<NowPlayingAdapter.AdapterNowPlayingHolder> {
	private final Context mContext;
	private final ArrayList<QueueItem> mDataset;
	private final NowPlayingRecyclerItemListener listener;
	private boolean showSeekLayout = false;

	// Provide a suitable constructor (depends on the kind of dataset)
	public NowPlayingAdapter(Context context, ArrayList<QueueItem> myDataset, NowPlayingRecyclerItemListener listener) {
		mContext = context;
		mDataset = myDataset;
		this.listener = listener;

		Resources res = mContext.getResources();
		Bitmap src = BitmapFactory.decodeResource(res, R.mipmap.placeholder);
		RoundedBitmapDrawable placeholder = RoundedBitmapDrawableFactory.create(mContext.getResources(), src);
		placeholder.setCircular(true);
	}

	public void add(int position, QueueItem item) {
		mDataset.add(position, item);
		notifyItemInserted(position);
	}

	public void remove(QueueItem item) {
		int position = mDataset.indexOf(item);
		mDataset.remove(position);
		notifyItemRemoved(position);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public AdapterNowPlayingHolder onCreateViewHolder(ViewGroup parent,
	                                                  int viewType) {
        // createActivityListener a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.now_playing_item, parent, false);
		// set the view's size, margins, paddings and layout parameters
		return new AdapterNowPlayingHolder(mContext, view, listener);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final AdapterNowPlayingHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		final QueueItem queueItem = mDataset.get(position);

		if (queueItem != null) {
            ImageManager.Instance().loadImageLargeAlways(queueItem, holder.imageCover);

			holder.playerSeekPositionText.setText(Utils.getDuration(PlayerConstants.QUEUE_TIME));
			holder.playerSeekDurationText.setText(Utils.getDuration(Integer.parseInt(PlayerConstants.QUEUE_SONG.duration)));

			if (showSeekLayout) {
				holder.playerSeekLayout.setVisibility(View.VISIBLE);
			} else {
				holder.playerSeekLayout.setVisibility(View.GONE);
			}

		}

    }

    @Override
    public void onBindViewHolder(final AdapterNowPlayingHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final QueueItem queueItem = mDataset.get(position);
            holder.playerSeekPositionText.setText(Utils.getDuration(PlayerConstants.QUEUE_TIME));
            holder.playerSeekDurationText.setText(Utils.getDuration(Integer.parseInt(PlayerConstants.QUEUE_SONG.duration)));

            if (showSeekLayout) {
                holder.playerSeekLayout.setVisibility(View.VISIBLE);
            } else {
                holder.playerSeekLayout.setVisibility(View.GONE);
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	public void showSeekLayout(boolean show) {
		showSeekLayout = show;
		notifyItemChanged(PlayerConstants.QUEUE_INDEX, new Integer(0));


	}

	public void updateProgress() {
		notifyItemChanged(PlayerConstants.QUEUE_INDEX, new Integer(0));


	}

	public static class AdapterNowPlayingHolder extends RecyclerView.ViewHolder {

		public final ImageView imageCover;
		public final RelativeLayout playerSeekLayout;
		public final TextView playerSeekPositionText;
		public final TextView playerSeekDurationText;
		final NowPlayingRecyclerItemListener listener;
		private final Context context;

		public AdapterNowPlayingHolder(final Context context, final View view, final NowPlayingRecyclerItemListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            imageCover = view.findViewById(R.id.imageCover);
            playerSeekLayout = view.findViewById(R.id.playerSeekLayout);
            playerSeekPositionText = view.findViewById(R.id.playerSeekPositionText);
            playerSeekDurationText = view.findViewById(R.id.playerSeekDurationText);

			view.setOnClickListener(view1 -> {
				if (listener != null) {
					listener.onNowPlayingItemClicked();
				}
			});

		}
	}

}
