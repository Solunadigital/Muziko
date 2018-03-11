package com.muziko.interfaces;

import android.content.Context;

/**
 * Created by dev on 5/08/2016.
 */
public interface TrackRecyclerItemListener {

	void onMenuClicked(Context context, int position);

	void onItemClicked(int position);

	boolean onItemLongClicked(int position);
}
