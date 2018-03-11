package com.muziko.interfaces;

import android.support.v7.widget.RecyclerView;

public interface RecyclerItemListener {
	void onDragTouched(RecyclerView.ViewHolder view);

	void onMenuClicked(int position);

	void onItemClicked(int position);

	boolean onItemLongClicked(int position);
}
