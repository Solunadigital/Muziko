package com.muziko.callbacks;


public interface SharingCallback {
	void onFilterValue(int value, boolean reverse);

	void onSearchQuery(String chars);

	void onReload();

	void onListingChanged();

	void onStorageChanged();

	void onLayoutChanged(Float bottomMargin);

	void onDownloadProgress(String url, int progress);
}
