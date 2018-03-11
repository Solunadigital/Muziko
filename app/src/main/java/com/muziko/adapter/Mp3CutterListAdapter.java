package com.muziko.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.ImageManager;

import java.util.ArrayList;


public class Mp3CutterListAdapter extends BaseAdapter implements Filterable {
	private final Context mContext;
	private final ArrayList<QueueItem> filterData;
	private ArrayList<QueueItem> mListData;
	private CustomFilter filter;
	private ClickListenerOnElement clickListenerOnElement;


	public Mp3CutterListAdapter(Context context, ArrayList<QueueItem> listData) {
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
		convertView = LayoutInflater.from(mContext).inflate(R.layout.element_mp3_songs_list, parent, false);

        holder.songIcon = convertView.findViewById(R.id.songIcon);
        holder.songName = convertView.findViewById(R.id.songName);
        holder.artistName = convertView.findViewById(R.id.artistName);

		final QueueItem mDataList = (QueueItem) this.getItem(position);
		if (mDataList == null) {
			holder.songName.setText("");
			holder.artistName.setText("");

		} else {
			holder.songName.setText(mDataList.title);
			holder.artistName.setText(mDataList.album_name);

            ImageManager.Instance().loadImage(mDataList, holder.songIcon);

			convertView.setOnClickListener(v -> {
				if (clickListenerOnElement != null) {
					clickListenerOnElement.onClick(mDataList);
				}
			});

			convertView.setTag(holder);

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

	public void addClickListner(ClickListenerOnElement clickListenerOnElement) {
		this.clickListenerOnElement = clickListenerOnElement;
	}

	public interface ClickListenerOnElement {
		void onClick(QueueItem queueListBean);
	}

	private static class ViewHolder {

		ImageView songIcon;
		TextView songName;
		TextView artistName;

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