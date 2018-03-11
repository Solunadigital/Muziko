package com.muziko.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muziko.R;
import com.muziko.common.models.firebase.Person;
import com.muziko.controls.FastScroller.FastScrollRecyclerView;
import com.muziko.helpers.Utils;
import com.muziko.interfaces.ShareListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;

public class ContactsAdapter extends SelectableAdapter<ContactsAdapter.AdapterQueueHolder> implements Filterable, FastScrollRecyclerView.SectionedAdapter {
	private final Context mContext;
	private final List<Person> filts;
	private final ShareListener listener;
	private String tag;
	private String lastSectionName = "A";
	private int storage;
	private int gridtype = 0;
	private CustomFilter filter;
	private List<Person> items;
	private String search = "";
	private String sortType = null;
	private int mode = 0;

	public ContactsAdapter(Context context, List<Person> listData, String tag, int mode, ShareListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.filts = listData;
		this.tag = tag;
		this.mode = mode;
		this.listener = listener;
        setHasStableIds(true);
    }


	@Override
	public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_contact_item, parent, false);


		return new AdapterQueueHolder(mContext, view, listener);
	}

	@Override
	public void onBindViewHolder(final AdapterQueueHolder holder, int position) {

		holder.viewOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);
		final Person item = this.getItem(position);
		if (item != null) {

			holder.textName.setText(item.getDisplayName());
			holder.textNumber.setText(item.getPhone());

			if (item.getPhotoUrl() != null) {
				Picasso.with(mContext)
						.load(item.getPhotoUrl())
						.tag(tag)
						.placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
						.resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
						.centerCrop()
						.into(holder.imageThumb);
			}

			if (item.isConnected()) {
				holder.onlineImageView.setVisibility(View.VISIBLE);
			} else {
				holder.onlineImageView.setVisibility(View.INVISIBLE);
			}
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

	public Person getItem(int position) {
		if (position >= 0 && position < items.size())
			return items.get(position);
		else
			return null;
	}

	public ArrayList<Person> getSelectedItems() {
		ArrayList<Person> selection = new ArrayList<>();

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
		else if (sortType.equals("sortOnlineLowest"))
			sortOnlineLowest();
		else if (sortType.equals("sortOnlineHighest"))
			sortOnlineHighest();
		else
			notifyDataSetChanged();
	}

	public void sortTitleLowest() {

        Collections.sort(items, (s1, s2) -> {
            return s1.getDisplayName().compareToIgnoreCase(s2.getDisplayName());
        });

		notifyDataSetChanged();

		sortType = "sortTitleLowest";
	}

	public void sortTitleHighest() {
		Collections.sort(items, (s1, s2) -> s2.getDisplayName().compareToIgnoreCase(s1.getDisplayName()));

		notifyDataSetChanged();

		sortType = "sortTitleHighest";
	}

	public void sortOnlineLowest() {

		Collections.sort(items, (s1, s2) -> {

			Long startTimeStamp = (Long) s1.getLastOnline();
			Long endTimeStamp = (Long) s2.getLastOnline();

			return startTimeStamp.compareTo(endTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortOnlineLowest";
	}

	public void sortOnlineHighest() {
		Collections.sort(items, (s1, s2) -> {

			Long startTimeStamp = (Long) s1.getLastOnline();
			Long endTimeStamp = (Long) s2.getLastOnline();

			return endTimeStamp.compareTo(startTimeStamp);
		});

		notifyDataSetChanged();

		sortType = "sortOnlineHighest";
	}

	public List<Person> getList() {
		return items;
	}

	public void add(Collection<Person> list) {
		items.clear();
		items.addAll(list);

		sort();
	}

	public void removeAll(ArrayList<Person> del) {
		items.removeAll(del);
		notifyDataSetChanged();
	}

	public void reset() {
		items.clear();
		notifyDataSetChanged();
	}

	public void set(Person item) {
		if (item != null) {
			for (int i = 0; i < getItemCount(); i++) {
				if (items.get(i) != null && items.get(i).getDisplayName() != null && items.get(i).getDisplayName().equals(item.getDisplayName())) {
					items.set(i, item);
					notifyItemChanged(i);
				}
			}
		}
		notifyDataSetChanged();
	}

	public void put(int index, Person item) {
		items.set(index, item);
		notifyItemChanged(index);
	}

	@NonNull
	@Override
	public String getSectionName(int position) {

		String s = items.get(position).getDisplayName();
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
		final CircleImageView imageThumb;
		final TextView textName;
		final TextView textNumber;
		final CircleImageView onlineImageView;
		final ShareListener listener;
		final View viewOverlay;
		private final Context context;

		public AdapterQueueHolder(Context context, final View view, final ShareListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            layoutMain = view.findViewById(R.id.layoutMain);
            layoutMenu = view.findViewById(R.id.layoutMenu);
            viewOverlay = view.findViewById(R.id.viewOverlay);

            imageThumb = view.findViewById(R.id.imageThumb);

            textName = view.findViewById(R.id.textName);
            textNumber = view.findViewById(R.id.textNumber);

            onlineImageView = view.findViewById(R.id.onlineImageView);

			if (layoutMenu != null) {
				layoutMenu.setOnClickListener(this);
			}

			layoutMain.setOnClickListener(this);
			layoutMain.setOnLongClickListener(this);
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

				ArrayList<Person> filterList = new ArrayList<>();
				// starts with
				ArrayList<Integer> already = new ArrayList<>();
				for (int i = 0; i < filts.size(); i++) {

					if (filts.get(i).getDisplayName().length() > 0 && Utils.searchStringStartsWith(needle, filts.get(i).getDisplayName())) {
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
					if (filts.get(i).getDisplayName().length() > 0 && Utils.searchStringContains(needle, filts.get(i).getDisplayName())) {
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
			items = (ArrayList<Person>) results.values;
			notifyDataSetChanged();
		}
	}
}
