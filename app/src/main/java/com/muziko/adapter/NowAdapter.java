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
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.ImageManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dev on 14/07/2016.
 */

public class NowAdapter extends RecyclerView.Adapter<NowAdapter.ViewHolder> {
	private final Context mContext;
	private final ArrayList<QueueItem> mDataset;

	// Provide a suitable constructor (depends on the kind of dataset)
	public NowAdapter(Context context, ArrayList<QueueItem> myDataset) {
		mContext = context;
		mDataset = myDataset;

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
	public NowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
	                                                int viewType) {
        // createActivityListener a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_player_view, parent, false);
		// set the view's size, margins, paddings and layout parameters
		return new ViewHolder(v);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element
		final QueueItem queueItem = mDataset.get(position);

		if (queueItem != null) {


            ImageManager.Instance().loadImageAlways(queueItem, holder.insideMusicIcon);

            ImageManager.Instance().loadImageLargeAlways(queueItem, holder.mainIcon);

			holder.insideMusicName.setText(Utils.trimString(queueItem.title, 25));
			holder.insideArtistName.setText(Utils.trimString(queueItem.artist_name, 150));
		}

	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public class ViewHolder extends RecyclerView.ViewHolder {
		// each data item is just a string in this case
		public final CircleImageView mainIcon;
		public final ImageView insideMusicIcon;
		public final TextView insideMusicName;
		public final TextView insideArtistName;

		public ViewHolder(View v) {
			super(v);
            mainIcon = v.findViewById(R.id.mainIcon);
            insideMusicIcon = v.findViewById(R.id.songIconInside);
            insideMusicName = v.findViewById(R.id.songNameInside);
            insideArtistName = v.findViewById(R.id.artistNameInside);
        }
	}

}
