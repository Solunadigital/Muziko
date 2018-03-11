package com.muziko.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.api.GoogleCustomSearch.models.GoogleImageSearchResults;
import com.muziko.interfaces.BasicRecyclerItemListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Vector;

import static android.R.attr.tag;
import static com.muziko.MyApplication.IMAGE_SMALL_SIZE;

/**
 * Created by dev on 9/08/2016.
 */

public class AlbumArtInternetAdapter extends RecyclerView.Adapter<AlbumArtInternetAdapter.ViewHolder> {
	private final BasicRecyclerItemListener mListener;
	private final Context mContext;
	private final ArrayList<GoogleImageSearchResults> mqueueItemArrayList;
	private byte[] bytearray;
	private Vector converart;
	private Bitmap bm;

	// Provide a suitable constructor (depends on the kind of dataset)
	public AlbumArtInternetAdapter(Context context, ArrayList<GoogleImageSearchResults> queueItemArrayList, BasicRecyclerItemListener listener) {
		mContext = context;
		mListener = listener;
		this.mqueueItemArrayList = queueItemArrayList;
        setHasStableIds(true);
    }

	public void add(int position, GoogleImageSearchResults item) {
		mqueueItemArrayList.add(position, item);
		notifyItemInserted(position);
	}

	public void remove(GoogleImageSearchResults item) {
		int position = mqueueItemArrayList.indexOf(item);
		mqueueItemArrayList.remove(position);
		notifyItemRemoved(position);
	}

	// Create new views (invoked by the layout manager)
	@Override
	public AlbumArtInternetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
	                                                             int viewType) {
        // createActivityListener a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_coverartimage_item, parent, false);
		// set the view's size, margins, paddings and layout parameters
		return new ViewHolder(mContext, v, mListener);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		final GoogleImageSearchResults googleImageSearchResults = mqueueItemArrayList.get(position);

//			holder.textTitle.setText(queueItem);
		holder.textTitle.setVisibility(View.GONE);

		Picasso.with(mContext)
				.load(googleImageSearchResults.imageUrl)
				.tag(tag)
				.placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
				.error(R.mipmap.placeholder)
				.resize(IMAGE_SMALL_SIZE, IMAGE_SMALL_SIZE)
				.centerCrop()
				.into(holder.albumIcon);
	}

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < mqueueItemArrayList.size())
            return mqueueItemArrayList.get(position).imageUrl.hashCode();
        else
            return -1;
    }

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mqueueItemArrayList.size();
    }

    @Override
    public void onViewRecycled(final ViewHolder holder) {
        holder.cleanup();
    }

	public GoogleImageSearchResults getItem(int position) {
		if (position >= 0 && position < mqueueItemArrayList.size())
			return mqueueItemArrayList.get(position);
		else
			return null;
	}

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		// each data item is just a string in this case
		public final ImageView albumIcon;
		public final TextView textTitle;
		final BasicRecyclerItemListener listener;
		private final Context context;

		public ViewHolder(Context context, View view, final BasicRecyclerItemListener listener) {
			super(view);
			this.context = context;
			this.listener = listener;

            albumIcon = view.findViewById(R.id.albumIcon);
            textTitle = view.findViewById(R.id.textTitle);

			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (listener != null) {
				listener.onItemClicked(getAdapterPosition());
			}
		}

		public void cleanup() {
			albumIcon.setImageDrawable(null);
		}

	}

}