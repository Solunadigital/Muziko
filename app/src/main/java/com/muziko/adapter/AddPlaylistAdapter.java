package com.muziko.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.PlaylistItem;
import com.muziko.interfaces.BasicRecyclerItemListener;

import java.util.ArrayList;

/**
 * Created by dev on 27/08/2016.
 */

public class AddPlaylistAdapter extends SelectableAdapter<AddPlaylistAdapter.AdapterQueueHolder> {

	private final BasicRecyclerItemListener listener;
	private final Context mContext;
	private final ArrayList<PlaylistItem> items;

	public AddPlaylistAdapter(Context context, ArrayList<PlaylistItem> listData, BasicRecyclerItemListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.listener = listener;
        setHasStableIds(true);
    }

	@Override
	public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;
		view = LayoutInflater.from(mContext).inflate(R.layout.adapter_playlist_add, parent, false);
		return new AdapterQueueHolder(mContext, view, listener);
	}

	@Override
	public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

		final PlaylistItem item = this.getItem(position);
		if (item != null) {

			holder.playListName.setText(item.title);
			if (item.songs <= 1) {
				holder.songNumber.setText(item.songs + " " + mContext.getString(R.string.song_leading_space));
			} else {
				holder.songNumber.setText(item.songs + " " + mContext.getString(R.string.songs_leading_space));
			}

		}
	}

    @Override
    public int getItemViewType(int position) {

        return 0;
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

    private PlaylistItem getItem(int position) {
        if (position >= 0 && position < items.size())
            return items.get(position);
        else
            return null;
    }

	public void removeIndex(int position) {
		if (position >= 0 && position < items.size()) {
			items.remove(position);
			notifyItemRemoved(position);
		}

	}


	public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		final TextView playListName;
		final TextView songNumber;
		final BasicRecyclerItemListener listener;
		private final Context context;

		public AdapterQueueHolder(Context context, final View view, final BasicRecyclerItemListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            playListName = view.findViewById(R.id.playListname);
            songNumber = view.findViewById(R.id.numberText);

			view.setOnClickListener(this);

		}

		@Override
		public void onClick(View view) {
			if (listener != null) {

				listener.onItemClicked(getAdapterPosition());
			}
		}
	}
}