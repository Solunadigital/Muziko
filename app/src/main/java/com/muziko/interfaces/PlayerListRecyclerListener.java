package com.muziko.interfaces;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by dev on 8/09/2016.
 */
public interface PlayerListRecyclerListener {

	void onAlbumMenuClicked(RecyclerView.Adapter adapter, View view, int position);

	void onAlbumItemClicked(RecyclerView.Adapter adapter, View view, int position);

	boolean onAlbumItemLongClicked(RecyclerView.Adapter adapter, View view, int position);
}
