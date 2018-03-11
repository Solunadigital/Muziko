package com.muziko.dialogs;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.adapter.ManageTabsAdapter;
import com.muziko.common.models.TabModel;
import com.muziko.database.TabRealmHelper;
import com.muziko.interfaces.ManageTabsRecyclerItemListener;
import com.muziko.manager.AppController;

import java.util.ArrayList;

/**
 * Created by dev on 27/08/2016.
 */
public class ManageTabs implements ManageTabsRecyclerItemListener {

	private final ManageTabsItemTouchHelper touchCallback = new ManageTabsItemTouchHelper();
	private final ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
	private ArrayList<TabModel> tabModels = new ArrayList<>();
	private ManageTabsAdapter adapter;
	private MaterialDialog createDialog = null;
	private MaterialDialog nameDialog = null;
	private boolean isBusy = false;
	private boolean isAdding = false;
	private boolean removeExisting = false;

	public ManageTabs(ArrayList<TabModel> tabModelArrayList) {
		tabModels = tabModelArrayList;
	}

	public void open(final Context context) {
		Context context1 = context;

		adapter = new ManageTabsAdapter(context, tabModels, this);

		View view = LayoutInflater.from(context).inflate(R.layout.dialog_manage_tabs, null, false);

		LinearLayoutManager layoutList = new LinearLayoutManager(context);
        RecyclerView recyclerView = view.findViewById(R.id.itemList);
        recyclerView.setLayoutManager(layoutList);
		recyclerView.setAdapter(adapter);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setHasFixedSize(true);
		touchHelper.attachToRecyclerView(recyclerView);

		createDialog = new MaterialDialog.Builder(context)
				.theme(Theme.LIGHT).titleColorRes(R.color.normal_blue)
				.negativeColorRes(R.color.dialog_negetive_button)
				.title("Manage Tabs")
				.customView(view, false).positiveText("Save").onPositive((dialog, which) -> {

					ArrayList<TabModel> newtabmodels = new ArrayList<>();

					boolean hasOne = false;
					int order = 1;
					for (int i = 0; i < tabModels.size(); i++) {
						TabModel tabModel = tabModels.get(i);
						if (tabModel.show) {
							hasOne = true;
							tabModel.order = order;
							order++;

						} else {
							tabModel.order = 99;
						}
						newtabmodels.add(tabModel);
					}

					if (hasOne) {
						TabRealmHelper.saveTabs(newtabmodels);
						MyApplication.tabsChanged = true;
					} else {
                        AppController.toast(context, " You need to show at least one tab");
                        createDialog.show();
					}

				}).negativeText("Cancel")
				.build();

		createDialog.show();
	}

	public void close() {

		if (createDialog != null) {
			createDialog.dismiss();
		}
	}

	@Override
	public void onDragTouched(RecyclerView.ViewHolder viewHolder) {
		if (touchHelper != null) touchHelper.startDrag(viewHolder);
	}

	@Override
	public void onItemChecked(TabModel tabModel) {

		for (int i = 0; i < tabModels.size(); i++) {
			TabModel tabModel1 = tabModels.get(i);
			if (tabModel.title.equals(tabModel1.title)) {
				tabModel1.show = !tabModel1.show;
				tabModels.set(i, tabModel1);
			}
		}
	}

	private class ManageTabsItemTouchHelper extends ItemTouchHelper.Callback {
        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

		//and in your imlpementaion of
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
			int from = viewHolder.getAdapterPosition();
			int to = target.getAdapterPosition();
			if (adapter.moveTo(from, to)) {

				TabModel firstitem = adapter.getItem(from);
				firstitem.order = from;
				tabModels.set(from, firstitem);

				TabModel seconditem = adapter.getItem(to);
				seconditem.order = to;
				tabModels.set(to, seconditem);

				boolean dragged = true;
			}
			return true;
		}

		@Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

		@Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

		}
	}

}