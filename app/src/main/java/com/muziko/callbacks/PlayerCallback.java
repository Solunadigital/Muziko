package com.muziko.callbacks;

/**
 * Created by divyanshunegi on 12/10/15.
 */
public interface PlayerCallback {
	void onFilterValue(int value, boolean reverse);

	void onSearchQuery(String chars);

	void onReload();

	void onQueueChanged();

	void onListingChanged();

	void onStorageChanged();

    void onLayoutChanged(float bottomMargin);

    void onDownloadProgress(String url, int progress);

    void onTrackAdded(String data);

    void onNowPlaying();

    void onFirebaseLimitChanged();
}
