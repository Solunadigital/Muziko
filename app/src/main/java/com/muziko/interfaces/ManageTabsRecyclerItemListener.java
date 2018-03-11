package com.muziko.interfaces;

import android.support.v7.widget.RecyclerView;

import com.muziko.common.models.TabModel;

/**
 * Created by dev on 27/08/2016.
 */
public interface ManageTabsRecyclerItemListener {
	void onDragTouched(RecyclerView.ViewHolder view);

	void onItemChecked(TabModel settingModel);
}