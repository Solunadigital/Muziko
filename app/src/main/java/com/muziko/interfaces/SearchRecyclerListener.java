package com.muziko.interfaces;

import com.muziko.common.models.QueueItem;

/**
 * Created by dev on 12/09/2016.
 */

public interface SearchRecyclerListener {

	void onItemMenuClicked(QueueItem queueItem, int position);

	void onItemClicked(QueueItem queueItem, int position);

	boolean onItemLongClicked(int position);

	void onLoaded();
}
