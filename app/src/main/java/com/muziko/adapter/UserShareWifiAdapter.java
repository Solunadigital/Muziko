package com.muziko.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.controls.FreshDownloadView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.ShareListener;
import com.muziko.salut.SalutDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserShareWifiAdapter extends SelectableAdapter<UserShareWifiAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
	private final Context mContext;
	private final List<SalutDevice> filts;
	private final ShareListener listener;
	private String tag;
	private String lastSectionName = "A";
	private int storage;
	private int gridtype = 0;
	private CustomFilter filter;
	private List<SalutDevice> items;
	private String search = "";
	private String sortType = null;
	private boolean mIsHost = false;

	public UserShareWifiAdapter(Context context, List<SalutDevice> listData, boolean isHost, String tag, ShareListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.filts = listData;
		mIsHost = isHost;
		this.tag = tag;
		this.listener = listener;

	}


	@Override
	public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_share_wifi_item, parent, false);


		return new AdapterQueueHolder(mContext, view, listener);
	}

	@Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

        final SalutDevice item = this.getItem(position);
        if (item != null) {
            holder.downloadProgress.setVisibility(View.GONE);
            if (mIsHost) {
                holder.textName.setText(item.deviceName);
                holder.connectedImageView.setVisibility(View.GONE);
            } else {
                holder.textName.setText(item.readableName);
                if (item.isRegistered) {
                    holder.connectedImageView.setVisibility(View.VISIBLE);
                } else {
                    holder.connectedImageView.setVisibility(View.GONE);
                }
            }

        }
    }

	@Override
	public void onBindViewHolder(final AdapterQueueHolder holder, int position, List<Object> payloads) {
		if (!payloads.isEmpty()) {

			Object progress = payloads.get(payloads.size() - 1);

			holder.downloadProgress.setVisibility(View.VISIBLE);

			if ((int) progress >= 0) {
				holder.downloadProgress.upDateProgress((int) progress);
			} else {
				holder.downloadProgress.showDownloadError();
			}

		} else {
			super.onBindViewHolder(holder, position, payloads);
		}
	}

    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

	public void updateProgress(String deviceName, int progress) {

		for (int i = 0; i < items.size(); i++) {
			SalutDevice salutDevice = items.get(i);
			if (salutDevice.deviceName.equals(deviceName)) {
				notifyItemChanged(i, new Integer(progress));
			}


		}

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

	public SalutDevice getItem(int position) {
		if (position >= 0 && position < items.size())
			return items.get(position);
		else
			return null;
	}

	public ArrayList<SalutDevice> getSelectedItems() {
		ArrayList<SalutDevice> selection = new ArrayList<>();

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

	private void sort() {
		if (sortType == null)
			notifyDataSetChanged();

		else if (sortType.equals("sortTitleLowest"))
			sortTitleLowest();
		else if (sortType.equals("sortTitleHighest"))
			sortTitleHighest();
		else
			notifyDataSetChanged();
	}

	public void sortTitleLowest() {

		Collections.sort(items, (s1, s2) -> s1.readableName.compareToIgnoreCase(s2.readableName));

		notifyDataSetChanged();

		sortType = "sortTitleLowest";
	}

	public void sortTitleHighest() {
		Collections.sort(items, (s1, s2) -> s2.readableName.compareToIgnoreCase(s1.readableName));

		notifyDataSetChanged();

		sortType = "sortTitleHighest";
	}


	public List<SalutDevice> getList() {
		return items;
	}

	public void add(Collection<SalutDevice> list) {
		items.clear();
		items.addAll(list);

		sort();
	}

	public void removeAll(ArrayList<SalutDevice> del) {
		items.removeAll(del);
		notifyDataSetChanged();
	}

	public void reset() {
		items.clear();
		notifyDataSetChanged();
	}

	public void set(SalutDevice item) {
		if (item != null) {
			for (int i = 0; i < getItemCount(); i++) {
				if (items.get(i).deviceName != null && items.get(i).deviceName != null && items.get(i).deviceName.equals(item.deviceName)) {
					items.set(i, item);
					notifyItemChanged(i);
				}
			}
		}
		notifyDataSetChanged();
	}

	public void put(int index, SalutDevice item) {
		items.set(index, item);
		notifyItemChanged(index);
	}

	@NonNull
	@Override
	public String getSectionName(int position) {

		String s = items.get(position).readableName;
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

	public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		final RelativeLayout layoutMain;
		final TextView textName;
		final ImageView connectedImageView;
		final ShareListener listener;
		final View viewOverlay;
		final FreshDownloadView downloadProgress;
		private final Context context;

		public AdapterQueueHolder(Context context, final View view, final ShareListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            layoutMain = view.findViewById(R.id.layoutMain);
            viewOverlay = view.findViewById(R.id.viewOverlay);
            downloadProgress = view.findViewById(R.id.downloadProgress);
            connectedImageView = view.findViewById(R.id.connectedImageView);

            textName = view.findViewById(R.id.textName);

			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (listener != null) {

				listener.onItemClicked(getAdapterPosition());

			}
		}

		@Override
		public boolean onLongClick(View view) {
			return listener != null && listener.onItemLongClicked(getAdapterPosition());
		}
	}

	private class CustomFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults result = new FilterResults();
			if (constraint != null && constraint.toString().length() > 0) {

				String needle = constraint.toString().toLowerCase();
				//String needles[] = constraint.toString().toLowerCase().split(" ");

				ArrayList<String> filterList = new ArrayList<>();
				// starts with
				ArrayList<Integer> already = new ArrayList<>();
				for (int i = 0; i < filts.size(); i++) {

					if (filts.get(i).readableName.length() > 0 && Utils.searchStringStartsWith(needle, filts.get(i).readableName)) {
						filterList.add(filts.get(i).readableName);
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
					if (filts.get(i).readableName.length() > 0 && Utils.searchStringContains(needle, filts.get(i).readableName)) {
						filterList.add(filts.get(i).readableName);
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
			items = (ArrayList<SalutDevice>) results.values;
			notifyDataSetChanged();
		}
	}
}
