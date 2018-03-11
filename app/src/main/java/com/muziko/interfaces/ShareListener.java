package com.muziko.interfaces;

import android.content.Context;

public interface ShareListener {

	void onItemClicked(int position);

	void onMenuClicked(Context context, int position);

	void onBlockClicked(int position);

	boolean onItemLongClicked(int position);
}
