package com.muziko.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.models.TabModel;
import com.muziko.controls.MuzikoSwitch;
import com.muziko.interfaces.ManageTabsRecyclerItemListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dev on 27/08/2016.
 */
public class ManageTabsAdapter extends SelectableAdapter<ManageTabsAdapter.AdapterQueueHolder> {

	private final Context mContext;
	private final ArrayList<TabModel> items;
	private final ManageTabsRecyclerItemListener listener;

	public ManageTabsAdapter(Context context, ArrayList<TabModel> listData, ManageTabsRecyclerItemListener listener) {
		super();
		this.mContext = context;
		this.items = listData;
		this.listener = listener;
	}


	@Override
	public AdapterQueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;

		view = LayoutInflater.from(mContext).inflate(R.layout.adapter_manage_tabs, parent, false);

		return new AdapterQueueHolder(mContext, view, listener);
	}

	@Override
	public void onBindViewHolder(final AdapterQueueHolder holder, int position) {
//        runEnterAnimation(holder.itemView, position);

		final TabModel item = this.getItem(position);
		if (item != null) {

			holder.tabModel = item;
			holder.tabName.setText(item.title);

			if (item.show) {
				holder.tabswitch.setCheckedSilent(true);
			} else {
				holder.tabswitch.setCheckedSilent(false);
			}

			holder.imageGrabber.setVisibility(View.VISIBLE);
		}
	}

    @Override
    public int getItemCount() {
        return items.size();
    }

	public TabModel getItem(int position) {
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

	public ArrayList<TabModel> getList() {
		return items;
	}

	public ArrayList<TabModel> getSelectedItems() {
		ArrayList<TabModel> selection = new ArrayList<>();

		List<Integer> indexes = getSelectedIndexes();
		for (Integer intr : indexes) {
			selection.add(getItem(intr));
		}
		indexes.clear();

		return selection;
	}

	public static class AdapterQueueHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, CompoundButton.OnCheckedChangeListener {

		final TextView tabName;
		final MuzikoSwitch tabswitch;
		final ImageView imageGrabber;
		final ManageTabsRecyclerItemListener listener;
		private final Context context;
		TabModel tabModel;

		public AdapterQueueHolder(Context context, final View view, final ManageTabsRecyclerItemListener listener) {
			super(view);

			this.context = context;
			this.listener = listener;

            tabName = view.findViewById(R.id.tabName);
            tabswitch = view.findViewById(R.id.tabswitch);
            imageGrabber = view.findViewById(R.id.imageGrabber);

			tabswitch.setOnCheckedChangeListener(this);

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

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (listener != null) {


				listener.onItemChecked(tabModel);
			}
		}
	}
}