package com.muziko.callbacks;

import android.content.Context;

public interface ActivityCallback {
	void onFilterValue(int value);

	void onSearchQuery(String chars);

	void onReload(Context context);

	void onListingChanged();

	void onStorageChanged();

	void onLayoutChanged(Float bottomMargin);
}
