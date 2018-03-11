package com.muziko.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eralp.circleprogressview.CircleProgressView;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.firebase.Person;
import com.muziko.common.models.firebase.Share;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.ShareListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static br.com.zbra.androidlinq.Linq.stream;

public class SharedFilesAdapter extends SelectableAdapter<SharedFilesAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
	private final Context mContext;
	private final List<Share> filts;
	private final ShareListener listener;
	private String tag;
	private String lastSectionName = "A";
	private int storage;
	private int gridtype = 0;
	private CustomFilter filter;
	private List<Share> items;
	private List<Person> persons;
	private String search = "";
	private String sortType = null;
	private int mode = 0;

	public SharedFilesAdapter(Context context, List<Share> listData, List<Person> persons, String tag, int mode, ShareListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.persons = persons;
		this.filts = listData;
		this.tag = tag;
		this.mode = mode;
		this.listener = listener;
	}

	@Override
	public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_share_item, parent, false);


		return new AdapterQueueHolder(mContext, view, listener);
	}

	@Override
	public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

		final Share item = this.getItem(position);
		if (item != null) {

			holder.textTitle.setText(item.getTitle());


			if (mode == MyApplication.SHARING_SENT) {

				holder.layoutMain.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
				holder.downloadProgress.setVisibility(View.GONE);
				holder.imageMenu.setVisibility(View.VISIBLE);
				holder.imageDownload.setVisibility(View.GONE);

				if (item.getType() == 0) {
					holder.sourceImage.setImageResource(R.drawable.history_user);
					holder.countText.setVisibility(View.VISIBLE);
					holder.countText.setText(item.getDownloads() + "/" + item.getShareCount());

					ArrayList<Person> receiverList = item.getReceiverList();
					String name = "";
					if (receiverList != null) {
						name = receiverList.get(0).getDisplayName();
						if (receiverList.size() > 1) {
							name = name + " and " + (receiverList.size() - 1) + " others...";
							holder.onlineImageView.setVisibility(View.GONE);
							holder.offlineImageView.setVisibility(View.GONE);
						} else {
							for (Person person : persons) {
								if (person.getUid().equals(receiverList.get(0).getUid())) {
									if (person.isConnected()) {
										holder.onlineImageView.setVisibility(View.VISIBLE);
										holder.offlineImageView.setVisibility(View.GONE);
									} else {
										holder.onlineImageView.setVisibility(View.GONE);
										holder.offlineImageView.setVisibility(View.VISIBLE);
									}
								}
							}
						}
					} else {
						name = "Unknown";
						holder.onlineImageView.setVisibility(View.GONE);
						holder.offlineImageView.setVisibility(View.GONE);
					}

					holder.textSender.setText(name);

				} else {
					holder.sourceImage.setImageResource(R.drawable.history_wifi);
					holder.countText.setVisibility(View.GONE);
					holder.onlineImageView.setVisibility(View.GONE);
					holder.offlineImageView.setVisibility(View.GONE);
					holder.textSender.setText(item.getReceiverId());
				}


			} else {

				if (item.getType() == 0) {
					holder.textSender.setText(item.getSenderName());
					holder.sourceImage.setImageResource(R.drawable.history_user);

					for (Person person : persons) {
						if (person.getUid().equals(item.getSenderId())) {
							if (person.isConnected()) {
								holder.onlineImageView.setVisibility(View.VISIBLE);
								holder.offlineImageView.setVisibility(View.GONE);
							} else {
								holder.onlineImageView.setVisibility(View.GONE);
								holder.offlineImageView.setVisibility(View.VISIBLE);
							}
						}
					}

					if (item.getLocalfile() != null) {
						holder.layoutMain.setBackgroundColor(ContextCompat.getColor(mContext, R.color.share_overlay));
						holder.imageMenu.setVisibility(View.VISIBLE);
						holder.countText.setVisibility(View.GONE);
						holder.imageDownload.setVisibility(View.GONE);
						holder.downloadProgress.setVisibility(View.GONE);
					} else {
						holder.layoutMain.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
						holder.imageMenu.setVisibility(View.GONE);
						holder.countText.setVisibility(View.GONE);
						holder.imageDownload.setVisibility(View.VISIBLE);
						holder.downloadProgress.setVisibility(View.GONE);
					}
				} else {
					holder.layoutMain.setBackgroundColor(ContextCompat.getColor(mContext, R.color.share_overlay));
					holder.onlineImageView.setVisibility(View.GONE);
					holder.offlineImageView.setVisibility(View.GONE);
					holder.imageMenu.setVisibility(View.VISIBLE);
					holder.countText.setVisibility(View.GONE);
					holder.imageDownload.setVisibility(View.GONE);
					holder.downloadProgress.setVisibility(View.GONE);

					holder.sourceImage.setImageResource(R.drawable.history_wifi);
					holder.textSender.setText(item.getSenderId());
				}

			}
		}
	}

    @Override
    public void onBindViewHolder(final AdapterQueueHolder holder, int position, List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final Share item = this.getItem(position);
            Object progress = payloads.get(0);

            holder.downloadProgress.setVisibility(View.VISIBLE);
            holder.imageMenu.setVisibility(View.GONE);
            holder.imageDownload.setVisibility(View.GONE);

            if ((int) progress >= 0) {
                holder.downloadProgress.setProgress((int) progress);
            } else {
                holder.downloadProgress.setVisibility(View.GONE);
                holder.imageDownload.setVisibility(View.VISIBLE);
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

	public void updateProgress(String url, int progress) {

		for (int i = 0; i < items.size(); i++) {
			Share share = items.get(i);
			if (share.getShareUrl() != null && share.getShareUrl().equals(url)) {
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

	public Share getItem(int position) {
		if (position >= 0 && position < items.size())
			return items.get(position);
		else
			return null;
	}

	public ArrayList<Share> getSelectedItems() {
		ArrayList<Share> selection = new ArrayList<>();

		List<Integer> indexes = getSelectedIndexes();
		for (Integer intr : indexes) {
			selection.add(getItem(intr));
		}
		indexes.clear();

		return selection;
	}

	public void search(String chars) {
		search = chars;
		getFilter().filter(chars);
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
		else if (sortType.equals("sortSentLowest"))
			sortSentLowest();
		else if (sortType.equals("sortSentHighest"))
			sortSentHighest();
		else
			notifyDataSetChanged();
	}

	public void sortTitleLowest() {

		Collections.sort(items, (s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));

		notifyDataSetChanged();

		sortType = "sortTitleLowest";
	}

	public void sortTitleHighest() {
		Collections.sort(items, (s1, s2) -> s2.getTitle().compareToIgnoreCase(s1.getTitle()));

		notifyDataSetChanged();

		sortType = "sortTitleHighest";
	}

	public void sortSentLowest() {

		Collections.sort(items, (s1, s2) -> {

			Long startTimeStamp = (Long) s1.getTimestamp();
			Long endTimeStamp = (Long) s2.getTimestamp();

			return startTimeStamp.compareTo(endTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortSentLowest";
	}

	public void sortSentHighest() {

		Collections.sort(items, (s1, s2) -> {

			Long startTimeStamp = (Long) s1.getTimestamp();
			Long endTimeStamp = (Long) s2.getTimestamp();

			return endTimeStamp.compareTo(startTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortSentHighest";
	}

	public void sortSenOnlineLowest() {

		Collections.sort(items, (s1, s2) -> {

			List<Person> persons1 =
					stream(MyApplication.userList.values())
							.where(c -> c.getUid().equals(s1.getSenderId()))
							.toList();

			List<Person> persons2 =
					stream(MyApplication.userList.values())
							.where(c -> c.getUid().equals(s2.getSenderId()))
							.toList();

			Long startTimeStamp = (Long) persons1.get(0).getLastOnline();
			Long endTimeStamp = (Long) persons2.get(0).getLastOnline();

			return startTimeStamp.compareTo(endTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortSenOnlineLowest";
	}

	public void sortSentOnlineHighest() {

		Collections.sort(items, (s1, s2) -> {

			List<Person> persons1 =
					stream(MyApplication.userList.values())
							.where(c -> c.getUid().equals(s1.getSenderId()))
							.toList();

			List<Person> persons2 =
					stream(MyApplication.userList.values())
							.where(c -> c.getUid().equals(s2.getSenderId()))
							.toList();

			Long startTimeStamp = (Long) persons1.get(0).getLastOnline();
			Long endTimeStamp = (Long) persons2.get(0).getLastOnline();

			return endTimeStamp.compareTo(startTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortSentOnlineHighest";
	}

	public void sortDownloadedLowest() {

		Collections.sort(items, (s1, s2) -> {
			Long startTimeStamp = (Long) s1.getDownloaded();
			Long endTimeStamp = (Long) s2.getDownloaded();

			return startTimeStamp.compareTo(endTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortDownloadedLowest";
	}

	public void sortDownloadedHighest() {
		Collections.sort(items, (s1, s2) -> {


			Long startTimeStamp = (Long) s1.getDownloaded();
			Long endTimeStamp = (Long) s2.getDownloaded();

			return endTimeStamp.compareTo(startTimeStamp);

		});

		notifyDataSetChanged();

		sortType = "sortDownloadedLowest";
	}

	public List<Share> getList() {
		return items;
	}

	public void add(Collection<Share> list) {
		items.clear();
		items.addAll(list);

		sort();
	}

	public void removeAll(ArrayList<Share> del) {
		items.removeAll(del);
		notifyDataSetChanged();
	}

	public void reset() {
		items.clear();
		notifyDataSetChanged();
	}

	public void set(Share item) {
		if (item != null) {
			for (int i = 0; i < getItemCount(); i++) {
				if (items.get(i) != null && items.get(i).getTitle() != null && items.get(i).getTitle().equals(item.getTitle())) {
					items.set(i, item);
					notifyItemChanged(i);
				}
			}
		}
		notifyDataSetChanged();
	}

	public void put(int index, Share item) {
		items.set(index, item);
		notifyItemChanged(index);
	}

	@NonNull
	@Override
	public String getSectionName(int position) {

		String s = items.get(position).getTitle();
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
		final LinearLayout layoutMenu;
		final TextView textTitle;
		final TextView textSender;
		final TextView countText;
		final ShareListener listener;
		final ImageView sourceImage;
		final ImageView imageMenu;
		final ImageView imageDownload;
		final CircleImageView offlineImageView;
		final CircleImageView onlineImageView;
		final CircleProgressView downloadProgress;
		private final Context context;

		public AdapterQueueHolder(Context context, final View view, final ShareListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            layoutMain = view.findViewById(R.id.layoutMain);
            layoutMenu = view.findViewById(R.id.layoutMenu);

            textTitle = view.findViewById(R.id.textTitle);
            textSender = view.findViewById(R.id.textSender);
            countText = view.findViewById(R.id.countText);

            imageDownload = view.findViewById(R.id.imageDownload);
            imageMenu = view.findViewById(R.id.imageMenu);
            sourceImage = view.findViewById(R.id.sourceImage);
            offlineImageView = view.findViewById(R.id.offlineImageView);
            onlineImageView = view.findViewById(R.id.onlineImageView);
            downloadProgress = view.findViewById(R.id.downloadProgress);

			if (imageMenu != null) {
				imageMenu.setOnClickListener(this);
			}

			if (layoutMenu != null) {
				layoutMenu.setOnClickListener(this);
			}

			layoutMain.setOnClickListener(this);
			layoutMain.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View view) {
			if (listener != null) {
				if (view == imageMenu) {
					listener.onMenuClicked(context, getAdapterPosition());
				} else {
					listener.onItemClicked(getAdapterPosition());
				}
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

				ArrayList<Share> filterList = new ArrayList<>();
				// starts with
				ArrayList<Integer> already = new ArrayList<>();
				for (int i = 0; i < filts.size(); i++) {

					if (filts.get(i).getTitle().length() > 0 && Utils.searchStringStartsWith(needle, filts.get(i).getTitle())) {
						filterList.add(filts.get(i));
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
					if (filts.get(i).getTitle().length() > 0 && Utils.searchStringContains(needle, filts.get(i).getTitle())) {
						filterList.add(filts.get(i));
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
			items = (ArrayList<Share>) results.values;
			notifyDataSetChanged();
		}
	}
}
