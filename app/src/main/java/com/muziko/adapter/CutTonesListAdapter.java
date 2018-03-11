package com.muziko.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.ImageManager;
import com.muziko.mediaplayer.PlayerConstants;

import java.util.ArrayList;

public class CutTonesListAdapter extends BaseAdapter implements Filterable {
	private final Context mContext;
	private final ArrayList<QueueItem> filterData;
	private ArrayList<QueueItem> mListData;
	private CustomFilter filter;

	private OnPlaySongRequestedListner onPlaySongRequestedListner;

	public CutTonesListAdapter(Context context, ArrayList<QueueItem> listData) {
		super();
		this.mContext = context;
		this.mListData = listData;
		this.filterData = listData;
		getFilter();
	}

	@Override
	public int getCount() {
		return mListData.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mListData.size())
			return mListData.get(position);
		else
			return null;
	}

	@Override
    public long getItemId(int position) {
        //Unimplemented, because we aren't using Sqlite.
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		holder = new ViewHolder();
		convertView = LayoutInflater.from(mContext).inflate(R.layout.element_cut_tone_list, parent, false);

        holder.songIcon = convertView.findViewById(R.id.songIcon);
        holder.songName = convertView.findViewById(R.id.songName);
        holder.artistName = convertView.findViewById(R.id.artistName);
        holder.dropDownButton = convertView.findViewById(R.id.menuDropLayout);
        holder.blueMenu = convertView.findViewById(R.id.bottomLayout);
        holder.upLayout = convertView.findViewById(R.id.upLayout);
        holder.mainLayout = convertView.findViewById(R.id.mainLayout);
        holder.blueTriangle = convertView.findViewById(R.id.blueIcon);
        holder.dropDownImage = convertView.findViewById(R.id.menuDrop);
        holder.setAsRingtone = convertView.findViewById(R.id.setAsRingtone);

        holder.editSongDetailButton = convertView.findViewById(R.id.editSongDetailButton);
        holder.deleteSongButton = convertView.findViewById(R.id.deleteSongButton);

		convertView.setTag(holder);

		holder.dropDownImage.setImageResource(R.drawable.menu_down);
		holder.blueTriangle.setVisibility(View.GONE);
		holder.blueMenu.setVisibility(View.GONE);

		final QueueItem mDataList = (QueueItem) this.getItem(position);
		if (mDataList == null) {
			holder.songName.setText("");
			holder.artistName.setText("");
		} else {
			String titleText = Utils.trimString(mDataList.title, 20);
			String artistText = Utils.trimString(mDataList.artist_name, 20);
			artistText = Utils.getLongDuration(Utils.getInt(mDataList.duration, 0));

			holder.songName.setText(titleText);
			holder.artistName.setText(artistText);

            ImageManager.Instance().loadImage(mDataList, holder.songIcon);

			holder.dropDownButton.setOnClickListener(v -> {

				if (holder.blueMenu.getVisibility() == View.VISIBLE) {

					holder.dropDownImage.setImageResource(R.drawable.menu_down);
					holder.blueTriangle.setVisibility(View.GONE);
					holder.blueMenu.setVisibility(View.GONE);

				} else {

					holder.dropDownImage.setImageResource(R.mipmap.drop_down_circle_blue);
					holder.blueTriangle.setVisibility(View.VISIBLE);
					holder.blueMenu.setVisibility(View.VISIBLE);
					onPlaySongRequestedListner.onScrollDown(position);

				}

			});

			holder.upLayout.setOnClickListener(v -> {
				if (onPlaySongRequestedListner != null) {
					PlayerConstants.QUEUE_TIME = 0;
					onPlaySongRequestedListner.onPlaySong(mDataList);
				}
			});


			holder.setAsRingtone.setOnClickListener(v -> {
				if (onPlaySongRequestedListner != null) {
					onPlaySongRequestedListner.onSetRingtone(mDataList);
				}
			});

			holder.deleteSongButton.setOnClickListener(v -> {
				if (onPlaySongRequestedListner != null) {

					onPlaySongRequestedListner.onDelete(mDataList);
				}
			});

			holder.editSongDetailButton.setOnClickListener(v -> {
				if (onPlaySongRequestedListner != null) {

					onPlaySongRequestedListner.onEditSong(mDataList);
				}
			});
		}

		return convertView;
	}

	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new CustomFilter();
		}
		return filter;
	}

	public void addPlayListner(OnPlaySongRequestedListner onPlaySongRequestedListner) {
		this.onPlaySongRequestedListner = onPlaySongRequestedListner;
	}

	public interface OnPlaySongRequestedListner {

		void onPlaySong(QueueItem dataBean);

		void onDelete(QueueItem dataBean);

		void onEditSong(QueueItem dataBean);

		void onSetRingtone(QueueItem dataBean);

		void onScrollDown(int position);

	}

	private class ViewHolder {

		ImageView songIcon;
		TextView songName;
		TextView artistName;
		LinearLayout dropDownButton;
		RelativeLayout blueMenu;
		ImageView blueTriangle;
		ImageView dropDownImage;
		ImageView editSongDetailButton;
		ImageView deleteSongButton;
		ImageView setAsRingtone;
		RelativeLayout upLayout;
		RelativeLayout mainLayout;


	}

	private class CustomFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			constraint = constraint.toString().toLowerCase();
			FilterResults result = new FilterResults();
			if (constraint != null && constraint.toString().length() > 0) {
				ArrayList<QueueItem> filterList = new ArrayList<>();
				String needle = constraint.toString().toLowerCase();

				// starts with
				ArrayList<Integer> already = new ArrayList<>();
				for (int i = 0; i < filterData.size(); i++) {

					if (filterData.get(i).title.length() > 0 && Utils.searchStringStartsWith(needle, filterData.get(i).title)) {
						filterList.add(filterData.get(i));
						already.add(i);
					} else if (filterData.get(i).artist_name.length() > 0 && Utils.searchStringStartsWith(needle, filterData.get(i).artist_name)) {
						filterList.add(filterData.get(i));
						already.add(i);
					}
				}
				// contains
				for (int i = 0; i < filterData.size(); i++) {
					for (int has : already) {
						if (i == has) {
                        }
                    }
					if (filterData.get(i).title.length() > 0 && Utils.searchStringContains(needle, filterData.get(i).title)) {
						filterList.add(filterData.get(i));
					} else if (filterData.get(i).artist_name.length() > 0 && Utils.searchStringContains(needle, filterData.get(i).artist_name)) {
						filterList.add(filterData.get(i));
					}
				}

				result.count = filterList.size();

				result.values = filterList;

			} else {
				result.count = filterData.size();

				result.values = filterData;
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {

			mListData = (ArrayList<QueueItem>) results.values;
			notifyDataSetChanged();

		}
	}

}