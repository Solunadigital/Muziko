package com.muziko.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.models.PlaylistItem;
import com.muziko.interfaces.RecyclerItemListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dev on 22/08/2016.
 */
public class PlaylistMergeAdapter extends SelectableAdapter<PlaylistMergeAdapter.AdapterQueueHolder> {

	private final Context mContext;
	private final ArrayList<PlaylistItem> items;
	private final RecyclerItemListener listener;

	public PlaylistMergeAdapter(Context context, ArrayList<PlaylistItem> listData, RecyclerItemListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.listener = listener;
	}


	@Override
	public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;

		view = LayoutInflater.from(mContext).inflate(R.layout.adapter_playlist_merge, parent, false);

		return new AdapterQueueHolder(mContext, view, listener);
	}

	@Override
	public void onBindViewHolder(final AdapterQueueHolder holder, int position) {
//        runEnterAnimation(holder.itemView, position);

		final PlaylistItem item = this.getItem(position);
		if (item != null) {

			holder.playListName.setText(item.title);
			if (item.songs <= 1) {
				holder.songNumber.setText(item.songs + mContext.getString(R.string.song_leading_space));
			} else {
				holder.songNumber.setText(item.songs + mContext.getString(R.string.songs_leading_space));
			}

			holder.imageGrabber.setVisibility(View.VISIBLE);
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

	private PlaylistItem getItem(int position) {
		if (position >= 0 && position < items.size())
			return items.get(position);
		else
			return null;
	}

	public boolean moveTo(int from, int to) {
		boolean ret = false;
		try {
			if (!items.isEmpty()) {
				Collections.swap(items, from, to);
				notifyItemMoved(from, to);


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

	public ArrayList<PlaylistItem> getSelectedItems() {
		ArrayList<PlaylistItem> selection = new ArrayList<>();

		List<Integer> indexes = getSelectedIndexes();
		for (Integer intr : indexes) {
			selection.add(getItem(intr));
		}
		indexes.clear();

		return selection;
	}

	public void removeIndex(int position) {
		if (position >= 0 && position < items.size()) {
			items.remove(position);
			notifyItemRemoved(position);
		}

	}


	public ArrayList<PlaylistItem> getList() {
		return items;
	}

	public void removeAll(ArrayList<PlaylistItem> del) {
		items.removeAll(del);
		notifyDataSetChanged();
	}

	public void reset() {
		items.clear();
		notifyDataSetChanged();
	}

	public void set(PlaylistItem item) {
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

	public void put(int index, PlaylistItem item) {
		items.set(index, item);
		notifyItemChanged(index);
	}

	public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

		final TextView playListName;
		final TextView songNumber;
		final ImageView imageGrabber;
		final RecyclerItemListener listener;
		private final Context context;

		public AdapterQueueHolder(Context context, final View view, final RecyclerItemListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            playListName = view.findViewById(R.id.playListname);
            songNumber = view.findViewById(R.id.numberText);
            imageGrabber = view.findViewById(R.id.imageGrabber);


			if (imageGrabber != null) {
				imageGrabber.setOnTouchListener(this);
			}


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
}
